#!/bin/bash

BATCHER_FUNCTION_NAME="indexer-batcher"
BATCHER_MAIN_CLASS="com.moneycol.indexer.batcher.BatcherFunction"
BATCHER_TRIGGER_TOPIC="dev.crawler.events"

WORKER_FUNCTION_NAME="indexer-worker"
WORKER_MAIN_CLASS="com.moneycol.indexer.worker.WorkerFunction"
WORKER_TRIGGER_TOPIC="dev.moneycol.indexer.batches"

INDEXING_FUNCTION_NAME="indexer-indexing"
INDEXING_MAIN_CLASS="com.moneycol.indexer.indexing.IndexerFunction"

PROCESSING_DONE_TOPIC="dev.moneycol.indexer.batching.done"

SERVICE_ACCOUNT="indexer-batcher@moneycol.iam.gserviceaccount.com"

########################## Batcher ##########################

#cd ..
#echo "Building and deploying function $BATCHER_FUNCTION_NAME with main class $BATCHER_MAIN_CLASS from $PWD"
#./gradlew :data-indexer:clean :data-indexer:shadowJar \
#-PfunctionName=$BATCHER_FUNCTION_NAME \
#-PmainClass=$BATCHER_MAIN_CLASS
#
#cd data-indexer
#cd build/libs
#
## Indexer batcher - publisher
#echo "Deploying Indexer Batcher function from $PWD"
#gcloud functions deploy $BATCHER_FUNCTION_NAME --entry-point $BATCHER_MAIN_CLASS --runtime java11 \
#--trigger-topic $BATCHER_TRIGGER_TOPIC \
#--service-account $SERVICE_ACCOUNT \
#--env-vars-file ../../.env.yaml \
#--memory 2048MB \
#--timeout 540s
##
############## Indexer Worker - subscriber #############
#cd ../../..
#cd ..
#echo "Building and deploying function $WORKER_FUNCTION_NAME with main class $WORKER_MAIN_CLASS from $PWD"
#./gradlew :data-indexer:clean :data-indexer:shadowJar \
#-PmainClass=$WORKER_MAIN_CLASS -PfunctionName=$WORKER_FUNCTION_NAME
#
#cd data-indexer
#cd build/libs
#
#echo "Deploying Indexer Worker function from $PWD"
#gcloud functions deploy $WORKER_FUNCTION_NAME --entry-point $WORKER_MAIN_CLASS --runtime java11 \
#--trigger-topic $WORKER_TRIGGER_TOPIC \
#--memory 2048MB \
#--service-account $SERVICE_ACCOUNT \
#--env-vars-file ../../.env.yaml \
#--timeout 540s

########### Indexing Function - subscriber #############
# indexing function reacts to PROCESSING_DONE_TOPIC with synchronous pull from
# the sink topic

#cd ../../..
cd ..
echo "Building and deploying function $INDEXING_FUNCTION_NAME with main class $INDEXING_MAIN_CLASS from $PWD"
./gradlew :data-indexer:clean :data-indexer:shadowJar \
-PmainClass=$INDEXING_MAIN_CLASS -PfunctionName=$INDEXING_FUNCTION_NAME

cd data-indexer
cd build/libs

echo "Deploying Indexer Indexing function from $PWD"
gcloud functions deploy $INDEXING_FUNCTION_NAME --entry-point $INDEXING_MAIN_CLASS --runtime java11 \
--trigger-topic $PROCESSING_DONE_TOPIC \
--memory 2048MB \
--service-account $SERVICE_ACCOUNT \
--env-vars-file ../../.env.yaml \
--timeout 540s

# Logs: gcloud functions logs read --limit 50
# collect results, fan-in - sink
# gcloud pubsub subscriptions pull dev.moneycol.indexer.sink --auto-ack --limit 1000