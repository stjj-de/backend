FROM node:16-alpine3.15

WORKDIR /app
COPY . .

ENV NODE_ENV production
RUN npm install
RUN npm run build

VOLUME /app/public/uploads
EXPOSE 1337
ENTRYPOINT ["npm", "run", "start"]
