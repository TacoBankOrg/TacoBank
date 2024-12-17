# ./backend/authentication_server/Dockerfile
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar auth-server.jar
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "auth-server.jar"]
