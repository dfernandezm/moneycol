## Collections API module

This is the API that allows CRUD operations on Collections.

In order to invoke it a JWT Token must be issued against the majority of the endpoints (under the class 
`CollectionsController`). `TokenController` exists for testing purposes mostly, and should be deleted from the 
production code.

### Running tests

To run the tests, at the time of writing, it's needed to define the following environment
variables:

- `FIREBASE_API_KEY`, pointing to a valid Firebase API key (it can be visualised or regenerated [here](https://console.cloud.google.com/apis/credentials?project=moneycol))
- `GOOGLE_APPLICATION_CREDENTIALS` pointing to a Service Account key in GCP with the following roles (majority unused)
  - `Cloud Datastore User`
  - `Firebase Authentication Admin`
  - `Firebase Authentication Viewer`
  - `Service Account Token Creator`
  - `Storage Admin`
  - `Storage Object Admin`
- A service account key with those roles exists under `/Users/david/moneycol-collections-api.json`

These variables can be set in the command line or in IntelliJ IDEA (depending where the tests are run from).


## Upgrading Micronaut

In order to get an accurate up-to-date `build.gradle` for Micronaut newer version, an approach is generating a brand
new project using the CLI:

```bash
mn create-app example.micronaut.micronautguide --build=gradle --lang=java
```

the file `micronautguide/build.gradle` can be used as a reference to migrate existing one.

Follow-up migration guides can be used from here.
