# 1단계: 빌드용 이미지
FROM gradle:8.5-jdk17 AS builder
WORKDIR /build
COPY --chown=gradle:gradle . .
RUN gradle build -x test

# 2단계: 실행용 이미지
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=Asia/Seoul
COPY --from=builder /build/build/libs/Popco-Client-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-Dspring.config.additional-location=file:/app/config/", "-jar", "app.jar"]