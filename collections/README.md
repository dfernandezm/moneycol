# Collections

Service to create/delete/update collections of items


## Running tests

TestContainers project is used to run integration tests against an emulated Firestore, therefore Docker should be installed in the host or CI server.
Apart from that the following environment variables are required:

```
export FIREBASE_API_KEY=
export GOOGLE_APPLICATION_CREDENTIALS=
```

These values are already set in GCS and picked up by the CloudBuild pipeline.

To download test reports:

```
gsutil cp -r gs://moneycol-ci/collections-test-reports .
```