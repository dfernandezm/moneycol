#!/bin/bash

BATCHER_FUNCTION_NAME="indexer-batcher"
BATCHER_MAIN_CLASS="com.moneycol.indexer.batcher.IndexerBatcher"
BATCHER_TRIGGER_TOPIC="dev.crawler.events"

WORKER_FUNCTION_NAME="indexer-worker"
WORKER_MAIN_CLASS="com.moneycol.indexer.worker.WorkerFunction"
WORKER_TRIGGER_TOPIC="dev.moneycol.indexer.batches"

SERVICE_ACCOUNT="indexer-batcher@moneycol.iam.gserviceaccount.com"

#

cd ..
echo "Building and deploying function $BATCHER_FUNCTION_NAME with main class $BATCHER_MAIN_CLASS from $PWD"
./gradlew :data-indexer:clean :data-indexer:shadowJar \
-PfunctionName=$BATCHER_FUNCTION_NAME \
-PmainClass=$BATCHER_MAIN_CLASS

cd data-indexer
cd build/libs

# Indexer batcher - publisher
echo "Deploying Indexer Batcher function..."
gcloud functions deploy $BATCHER_FUNCTION_NAME --entry-point $BATCHER_MAIN_CLASS --runtime java11 \
--trigger-topic $BATCHER_TRIGGER_TOPIC \
--service-account $SERVICE_ACCOUNT \
--timeout 540s

# ########### Indexer Worker - subscriber
cd ../../..

echo "Building and deploying function $WORKER_FUNCTION_NAME with main class $WORKER_MAIN_CLASS from $PWD"
./gradlew :data-indexer:clean :data-indexer:shadowJar \
-PmainClass=$WORKER_MAIN_CLASS -PfunctionName=$WORKER_FUNCTION_NAME

gcloud functions deploy $BATCHER_FUNCTION_NAME --entry-point $WORKER_MAIN_CLASS --runtime java11 \
--trigger-topic $WORKER_TRIGGER_TOPIC \
--service-account $SERVICE_ACCOUNT \
--timeout 540s

# Logs: gcloud functions logs read --limit 50

# collect results, fan-in - sink