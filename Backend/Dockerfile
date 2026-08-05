FROM eclipse-temurin:24-jdk AS build

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:24-jre

WORKDIR /app

RUN useradd --system --create-home --home-dir /app appuser \
    && mkdir -p /app/uploads \
    && chown -R appuser:appuser /app

COPY --from=build --chown=appuser:appuser /workspace/build/libs/*.jar /app/app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
