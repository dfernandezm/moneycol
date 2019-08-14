# https://cloud.google.com/kubernetes-engine/docs/how-to/small-cluster-tuning
# https://cloud.google.com/blog/products/containers-kubernetes/cutting-costs-with-google-kubernetes-engine-using-the-cluster-autoscaler-and-preemptible-vms

# https://cloud.google.com/kubernetes-engine/docs/how-to/small-cluster-tuning
# https://cloud.google.com/kubernetes-engine/docs/how-to/preemptible-vms
# https://medium.com/google-cloud/using-preemptible-vms-to-cut-kubernetes-engine-bills-in-half-de2481b8e814


# https://docs.bitnami.com/google/get-started-gke/

gcloud container clusters create moneycol-dev --enable-autoscaling --min-nodes 1 --max-nodes 2 --no-enable-cloud-logging --no-enable-cloud-monitoring --machine-type=g1-small --zone europe-west1-b
gcloud container clusters get-credentials moneycol-dev --region=europe-west1-b
gcloud container clusters resize moneycol-dev --num-nodes 2 --region=europe-west1-b
kubectl cluster-info

# Dashboard is deprecated for GKE

kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v1.10.1/src/deploy/recommended/kubernetes-dashboard.yaml
kubectl proxy

# Get account.json for terraform
# https://console.cloud.google.com/iam-admin/serviceaccounts/details/101087981028380213494?folder=&organizationId=&project=moneycol
gcloud iam service-accounts keys create ~/account.json --iam-account moneycol1@moneycol.iam.gserviceaccount.com