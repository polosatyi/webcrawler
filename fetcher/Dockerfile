FROM mcr.microsoft.com/playwright:focal
COPY package.json /srv/
WORKDIR /srv/
RUN npm install
COPY app/ /srv/app/
CMD ["node", "app/server.js"]
