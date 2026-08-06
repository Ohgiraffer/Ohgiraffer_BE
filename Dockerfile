# ===== 1단계: 빌드 =====
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Gradle 캐시 최적화: 의존성 관련 파일만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew
# 의존성만 미리 받아서 레이어 캐싱 (소스 변경 시 이 단계는 재사용됨)
RUN ./gradlew dependencies --no-daemon || true

# 전체 소스 복사 후 빌드
COPY . .
RUN ./gradlew bootJar --no-daemon -x test

# plain.jar가 같이 생성되는 경우가 있어 bootJar 산출물만 명확히 찾아서 이름 고정
RUN JAR_FILE=$(find /app/build/libs -maxdepth 1 -name "*.jar" ! -name "*plain*" | head -n 1) \
    && cp "$JAR_FILE" /app/build/libs/app.jar

# ===== 2단계: 실행 =====
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# root로 실행하지 않기 위한 유저 생성
RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=build /app/build/libs/app.jar app.jar

RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]