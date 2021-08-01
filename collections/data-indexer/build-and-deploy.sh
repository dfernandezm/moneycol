#!/bin/bash

cd ..
./gradlew :data-indexer:clean :data-indexer:shadowJar
cd data-indexer
cd build/libs
gcloud functions deploy indexer-batcher --entry-point com.moneycol.indexer.IndexerBatcher --runtime java11 \
--trigger-topic dev.crawler.events \
--service-account indexer-batcher@moneycol.iam.gserviceaccount.com \
--timeout 540s