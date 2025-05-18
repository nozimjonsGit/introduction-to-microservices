FROM gradle:jdk17-alpine AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle /app/

COPY gradle /app/gradle

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src /app/src

RUN ./gradlew assemble --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="nozimjon_nabiev@epam.com"

WORKDIR /app

COPY --from=build /app/build/libs/*.jar resource-service.jar

EXPOSE 8082

CMD ["java", "-jar", "resource-service.jar"]