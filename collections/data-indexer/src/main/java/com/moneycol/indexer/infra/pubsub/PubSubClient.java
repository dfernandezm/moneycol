package com.moneycol.indexer.infra.pubsub;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.events.cloud.pubsub.v1.Message;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ModifyAckDeadlineRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Singleton
public class PubSubClient {

    private final FanOutConfigurationProperties fanOutProperties;

    private final JsonWriter jsonWriter = new JsonWriter();
    private final Map<String, Publisher> topicNameToPublishers = new HashMap<>();
    private final static int ACK_DEADLINE_SECONDS = 60 * 10; // 600 yseconds

    // Push batches to pubsub topic
    // subscribers read 1 batch, extract filenames, read documents
    // push to another topic (or index directly, depending on time)
    // https://stackoverflow.com/questions/17374743/how-can-i-get-the-memory-that-my-java-program-uses-via-javas-runtime-api
    public <T> void publishMessage(String topicName, T message) {
        publishMessageWithAttributes(topicName, message, ImmutableMap.of());
    }

    public <T> void publishMessageWithAttributes(String topicName, T message, Map<String, String> messageAttributes) {

        String messageJson = jsonWriter.asJsonString(message);
        PubsubMessage pubsubApiMessage = createPubsubMessage(messageJson, messageAttributes);

        try {

            Publisher publisher = publisherForTopic(topicName);

            // we do .get() to block on the returned Future and ensure the message is sent and avoid
            // premature termination killing messages being sent
            publisher.publish(pubsubApiMessage).get();
        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error("Error publishing Pub/Sub message: " + e.getMessage(), e);
        }
    }

    private PubsubMessage createPubsubMessage(String messageJson, Map<String, String> attributes) {
        ByteString byteStr = ByteString.copyFrom(messageJson, StandardCharsets.UTF_8);
        return PubsubMessage
                .newBuilder()
                .setData(byteStr)
                .putAllAttributes(attributes)
                .build();
    }

    private Publisher publisherForTopic(String topicName) throws IOException {
        Publisher publisher = topicNameToPublishers.get(topicName);
        if (publisher == null) {
            publisher = Publisher
                    .newBuilder(ProjectTopicName.of(fanOutProperties.getGcpProjectId(), topicName))
                    .build();
            topicNameToPublishers.put(topicName, publisher);
        }
        return publisher;
    }

    // sink topic, use synchronous pull https://cloud.google.com/pubsub/docs/pull#synchronous_pull
    // to get them numOfMessages by numOfMessages. Needs a single subscription created to the sink topic and 1 subscriber
    // returns boolean indicating if its done or not
    public boolean subscribeSync(String subscriptionId, Integer numOfMessages,
            Consumer<PubsubMessage> messageHandler, Supplier<Boolean> stopCondition) throws IOException {

        SubscriberStubSettings subscriberStubSettings = setupSubscriberStub();


        try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {

            String subscriptionName = ProjectSubscriptionName.format(
                    fanOutProperties.getGcpProjectId(),
                    subscriptionId);
            List<ReceivedMessage> receivedMessages;
            boolean stopProcessing = false;

            do {
                // 250 max
                receivedMessages = pullMessages(numOfMessages, subscriber, subscriptionName);

                if (receivedMessages.size() == 0) {
                    return true;
                }

                // 250 ackIDs
                List<String> remainderOfAckIds = receivedMessages.stream()
                        .map(ReceivedMessage::getAckId)
                        .collect(Collectors.toList());

                log.info("Pulling {} messages from subscription {}",
                        receivedMessages.size(), subscriptionName);
                extendAckDeadline(subscriber, subscriptionName, remainderOfAckIds, ACK_DEADLINE_SECONDS);

                for (ReceivedMessage message : receivedMessages) {

                    log.info("Received message with id {}, ackId {}",
                            message.getMessage().getMessageId(),
                            message.getAckId());

                    try {
                        // Handle received message [blocking]
                        messageHandler.accept(message.getMessage());
                    } catch (Throwable t) {
                        //TODO: this may produce duplicates in Elasticsearch
                        // some documents where inserted and others don't
                        // as id is generated in-place
                        log.warn("Error processing message with ID {} - skipping and nack",
                                message.getMessage().getMessageId());
                        List<String> nackIds = new ArrayList<>();
                        nackIds.add(message.getAckId());
                        nackMessages(subscriber, subscriptionName, nackIds);
                        continue;
                    }


                    // ack the message
                    String ackId = message.getAckId();
                    List<String> ackIds = new ArrayList<>();
                    ackIds.add(ackId);
                    acknowledgeMessages(subscriber, subscriptionName, ackIds);

                    // remove this message from ackIDs
                    remainderOfAckIds.remove(ackId);

                    // check the stop condition
                    if (stopCondition.get()) {
                        log.info("Getting close to condition for stopping, stopping now -- not nacking");
                        if (!remainderOfAckIds.isEmpty()) {
                            nackMessages(subscriber, subscriptionName, remainderOfAckIds);
                        }
                        stopProcessing = true;
                        break;
                    }
                }

            } while(!receivedMessages.isEmpty() && !stopProcessing);

            return receivedMessages.isEmpty();
        }
    }

    private void extendAckDeadline(SubscriberStub subscriber, String subscriptionName, List<String> remainderOfAckIds, int ackDeadlineSeconds) {
        // This way we give 600 seconds to process the batch of 250 messages
        // Modify the ack deadline of each received message from the default 10 seconds to 600s.
        // This prevents the server from redelivering the message after the default 10 seconds
        // have passed.

        // The client already extends this in the background, but if it is too short (10s) it may not have
        // time across executions (one function finishing, another starting). This was happening
        // with some files, the deadline exceeded due to function finishing and restarting, hence
        // the server redelivered the message. This was observed as same id twice in logs / same file with same
        // documents being processed resulting in many more documents indexed in Elastic
        ModifyAckDeadlineRequest modifyAckDeadlineRequest =
                ModifyAckDeadlineRequest.newBuilder()
                        .setSubscription(subscriptionName)
                        .addAllAckIds(remainderOfAckIds)
                        .setAckDeadlineSeconds(ackDeadlineSeconds)
                        .build();

        subscriber.modifyAckDeadlineCallable().call(modifyAckDeadlineRequest);
    }

    private List<ReceivedMessage> pullMessages(Integer numOfMessages, SubscriberStub subscriber, String subscriptionName) {
        PullRequest pullRequest =
                PullRequest.newBuilder()
                        .setMaxMessages(numOfMessages)
                        .setSubscription(subscriptionName)
                        .build();

        PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);
        return pullResponse.getReceivedMessagesList();
    }

    public String readMessageToString(PubsubMessage message) {
        return message.getData().toStringUtf8();
    }

    public String readMessageFromEventToString(Message message) {
        return new String(
                Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }

    private SubscriberStubSettings setupSubscriberStub() throws IOException {
        return SubscriberStubSettings.newBuilder()
                .setTransportChannelProvider(
                        SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
                                .setMaxInboundMessageSize(20 * 1024 * 1024) // 20MB (maximum message size).
                                .build())
                .build();
    }

    private void acknowledgeMessages(SubscriberStub subscriber, String subscriptionName, List<String> ackIds) {
        // Acknowledge received messages.

        AcknowledgeRequest acknowledgeRequest =
                AcknowledgeRequest.newBuilder()
                        .setSubscription(subscriptionName)
                        .addAllAckIds(ackIds)
                        .build();

        // Use acknowledgeCallable().futureCall to asynchronously perform this operation.
        subscriber.acknowledgeCallable().call(acknowledgeRequest);
    }

    // Puts the ackDeadline to 0 so the messages are nack-ed and redelivered (sync)
    @VisibleForTesting
    public void nackMessages(SubscriberStub subscriber,
                              String subscriptionName, List<String> ackIds) {
        log.warn("About to nack {} messages as this execution is about to timeout", ackIds.size());
        ModifyAckDeadlineRequest modifyAckDeadlineRequest =
                ModifyAckDeadlineRequest.newBuilder()
                        .setSubscription(subscriptionName)
                        .addAllAckIds(ackIds)
                        .setAckDeadlineSeconds(0)
                        .build();
        subscriber.modifyAckDeadlineCallable().call(modifyAckDeadlineRequest);
    }


}