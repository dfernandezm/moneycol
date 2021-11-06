#!/usr/bin/env bash

# to be completed when a VPC-native GKE cluster is done

VPC_NETWORK="default"
REGION="europe-west1"
IP_RANGE="10.8.0.0/28"


gcloud compute networks vpc-access connectors create dev-moneycol-indexer-serverless-connector \
--network $VPC_NETWORK \
--region $REGION \
--range $IP_RANGE
