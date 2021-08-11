#!/usr/bin/env bash

# Tiller manual deploy https://console.cloud.google.com/kubernetes/pod/europe-west1-b/moneycol-dev/kube-system/tiller-deploy-65887d68f-2rgjx/yaml/view?project=moneycol


# The elasticsearch ip

VPC_NETWORK=""
REGION="europe-west1"
IP_RANGE="10.8.0.0/28"


gcloud compute networks vpc-access connectors create dev-moneycol-indexer-serverless-connector \
--network  \
--region REGION \
--range IP_RANGE

# https://github.com/elastic/elasticsearch-java