FROM node:12-alpine

RUN addgroup -S app && adduser -S app -G app -h /home/app
RUN apk add curl wget vim sudo
RUN npm install -g serve

USER app
RUN mkdir -p /home/app

COPY --chown=app:app . /home/app
RUN chmod -R 700 /home/app/

WORKDIR /home/app
RUN npm install
RUN npm run build
RUN cd web-server && npm install

EXPOSE 5080

ENTRYPOINT ["node", "web-server/server.js"]