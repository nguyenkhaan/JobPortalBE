FROM gradle:8.7-jdk17 AS build
WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean build

FROM eclipse-temurin:17-jre AS release
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]