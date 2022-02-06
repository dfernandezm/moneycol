## Resizer function with Cloud Scheduler

This is currently manually deployed in `moneycol` GCP project:
- 2 Cloud Scheduler jobs
- 1 `gke-resizer` HTTP Cloud Function

`Cloud Scheduler` jobs run periodically to expand / shrink the GKE cluster.
They invoke a `POST` to the HTTP function `gke-resizer` with OIDC token using `gke-resizer` service account (also manually created).

This code is also used by the background Cloud Function `resize-down-cluster`, deployed automatically via Terraform (see top level `data-collector/crawler/infra/terraform`).