FROM node:10-alpine
RUN apk add bash bash-doc bash-completion
RUN npm install -g firebase-tools
COPY firebase.json .
COPY firestore.indexes.json .
COPY firestore.rules .
RUN apk add openjdk8-jre
RUN apk add curl
EXPOSE 8080
RUN firebase setup:emulators:firestore
CMD ["/bin/sh", "-c", "firebase emulators:start --only firestore"]