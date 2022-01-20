## Resizer function with Cloud Scheduler

This is currently deployed by hand in `moneycol` project:
- 2 Cloud Scheduler jobs
- 1 `gke-resizer` function

`Cloud Scheduler` jobs run periodically to expand / shrink the GKE cluster.
They invoke a `POST` to the HTTP function `gke-resizer` with OIDC token using `gke-resizer` service account.