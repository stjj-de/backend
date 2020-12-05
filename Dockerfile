FROM adoptopenjdk/openjdk14:alpine-jre
COPY ./build/libs/stjj-backend.jar /app/stjj-backend.jar
WORKDIR /app

ENV DATA_DIR=/app/data
VOLUME /app/data

EXPOSE 8000
ENTRYPOINT ["java", "-jar", "stjj-backend.jar"]
