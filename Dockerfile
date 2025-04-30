FROM gradle:8.6-jdk17 AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

COPY . .
RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
