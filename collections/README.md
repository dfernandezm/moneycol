# Collections

Service to create/delete/update collections of items


## Firebase emulator

Can be built from the Dockerfile inside `firebase`. The file `firebase.json` contains a binding to allow the emulator to be
started and listening in all IP addresses of the container:

```
"emulators": {
    "firestore": {
      "host": "0.0.0.0",
      "webchannel-port": 8080
    }
  }
```

This is necessary so that `docker -p` binds the port correctly to the host. If not set, firestore emulator will only listen to
`localhost:8080` (hence, cannot be connected from outside the container).

Run tests with emulator:

```
$ docker pull eu.gcr.io/moneycol/firebase-emu:0.1.0
$ docker run -p 8080:8080 -ti eu.gcr.io/moneycol/firebase-emu:0.1.0
$ export GOOGLE_APPLICATION_CREDENTIALS=/Users/david/moneycol-firestore-collections-api.json
$ ./gradlew clean test
```