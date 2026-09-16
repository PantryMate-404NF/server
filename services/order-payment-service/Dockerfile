# 빌드 전 사전 준비: ./gradlew :services:order-payment-service:bootJar
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY build/libs/*.jar ./
RUN rm -f *-plain.jar && mv *.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
