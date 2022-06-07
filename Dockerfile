FROM node:16-alpine

WORKDIR /app
COPY ./src ./src
COPY ./keystone.ts ./keystone.ts
COPY ./package.json ./package.json
COPY ./schema.graphql ./schema.graphql
COPY ./schema.prisma ./schema.prisma
COPY ./yarn.lock ./yarn.lock

RUN yarn
RUN yarn build

VOLUME /app/data
EXPOSE 8000
ENTRYPOINT ["yarn", "start"]
