FROM node:12-stretch

RUN useradd -c "App User" -d /home/app -m app
RUN apt-get update && apt-get install -y curl wget vim sudo
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