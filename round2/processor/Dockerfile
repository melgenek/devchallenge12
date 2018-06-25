FROM node:10.3.0-alpine

WORKDIR /app

COPY src /app/src
COPY package*.json ./

ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD true

RUN npm install

CMD [ "node", "src/listener.js" ]
