# 빌드 컨텍스트는 레포 루트여야 함: docker build -f services/user-service/Dockerfile .
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY modules modules
COPY platform platform
COPY services services

RUN ./gradlew :services:user-service:bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /workspace/services/user-service/build/libs/*.jar ./
RUN rm -f *-plain.jar && mv *.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
