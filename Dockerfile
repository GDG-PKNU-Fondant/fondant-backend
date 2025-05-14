FROM gradle:8.6-jdk17 AS build
WORKDIR /home/gradle/src
COPY . .

RUN ./gradlew clean bootJar --no-daemon --info

FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]