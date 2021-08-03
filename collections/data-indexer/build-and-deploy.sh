#!/bin/bash

FUNCTION_NAME=$1
MAIN_CLASS=$2

SERVICE_ACCOUNT="indexer-batcher@moneycol.iam.gserviceaccount.com"
echo "Building and deploying function $FUNCTION_NAME with main class $MAIN_CLASS"

cd ..
./gradlew :data-indexer:clean :data-indexer:shadowJar -PfunctionName=$FUNCTION_NAME -PmainClass=$MAIN_CLASS
cd data-indexer
cd build/libs

# Indexer batcher - publisher
gcloud functions deploy $FUNCTION_NAME --entry-point $MAIN_CLASS --runtime java11 \
--trigger-topic dev.crawler.events \
--service-account $SERVICE_ACCOUNT \
--timeout 540s

# worker - subscriber
# ./gradlew :data-indexer:clean :data-indexer:shadowJar -PmainClass=com.moneycol.indexer.worker.WorkerFunction -PfunctionName=indexer-worker

# collect/index - sink