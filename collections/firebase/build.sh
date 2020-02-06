#!/bin/bash

docker build . -t firebaseemu-0.1.0:latest
docker run -p 8080:8080 -ti firebaseemu-0.1.0:latest