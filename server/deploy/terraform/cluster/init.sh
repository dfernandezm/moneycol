#!/bin/bash

export TF_VAR_cluster_state_bucket="moneycol-tf-state-dev"
export TF_VAR_state_bucket_prefix="terraform/state/cluster"
terraform init -backend-config "bucket=$TF_VAR_cluster_state_bucket" \
-backend-config "prefix=$TF_VAR_state_bucket_prefix"
