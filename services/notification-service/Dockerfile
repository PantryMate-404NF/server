# 빌드 컨텍스트는 레포 루트여야 함: docker build -f services/notification-service/Dockerfile .
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY modules modules
COPY platform platform
COPY services services

RUN ./gradlew :services:notification-service:bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /workspace/services/notification-service/build/libs/*.jar ./
RUN rm -f *-plain.jar && mv *.jar app.jar && chown app:app app.jar

USER app

EXPOSE 8085

ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
