FROM gradle:jdk17-alpine AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle /app/

COPY gradle /app/gradle

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src /app/src

RUN ./gradlew build --no-daemon -x test

FROM openjdk:17-jdk-slim

LABEL maintainer="nozimjon_nabiev@epam.com"

WORKDIR /app

COPY --from=build /app/build/libs/*.jar resource-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "resource-service.jar"]