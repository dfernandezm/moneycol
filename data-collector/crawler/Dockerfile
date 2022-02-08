FROM node:14-alpine

RUN addgroup -S app && adduser -S app -G app && \
    apk update && apk add curl wget vim sudo
USER app
RUN mkdir -p /home/app

COPY --chown=app:app . /home/app
RUN chmod -R 700 /home/app/

WORKDIR /home/app

RUN npm install

ENTRYPOINT ["npm", "start"]