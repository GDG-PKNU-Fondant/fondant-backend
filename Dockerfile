FROM gradle:8.6-jdk17 AS build
WORKDIR /home/gradle/src
COPY . .

RUN ./gradlew clean bootJar --no-daemon

FROM gcr.io/distroless/java17-debian12 AS runtime
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080
USER nonroot
ENTRYPOINT ["java","-jar","/app/app.jar"]