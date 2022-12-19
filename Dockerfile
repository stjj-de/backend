FROM node:16-alpine

WORKDIR /app
COPY ./src .

RUN yarn
RUN yarn build

VOLUME /app/data
EXPOSE 8000
ENTRYPOINT ["yarn", "start"]
