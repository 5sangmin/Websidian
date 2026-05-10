# =====================================================
# Stage 1: Build
# =====================================================
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /build

# Gradle wrapper와 의존성 정의 파일만 먼저 복사 (캐싱 최적화)
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# 의존성 다운로드 (이 레이어는 build.gradle 변경 시에만 재실행)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src/ src/

# JAR 빌드 (테스트 제외)
RUN ./gradlew clean build -x test --no-daemon

# =====================================================
# Stage 2: Runtime
# =====================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR만 복사
COPY --from=builder /build/build/libs/backend-0.0.1-SNAPSHOT.jar /app/backend.jar

# 비root 사용자 생성 (보안)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# JVM 메모리 설정 (필요시 환경 변수로 오버라이드 가능)
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar backend.jar"]