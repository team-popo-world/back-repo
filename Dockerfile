
# Java 17 이미지 기반
FROM openjdk:17-jdk-slim

# JAR 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 앱 실행
ENTRYPOINT ["java","-jar","/app.jar"]