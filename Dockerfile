# Используем базовый образ с JDK для сборки
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Копируем gradle wrapper и файлы конфигурации
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# Делаем gradlew исполняемым
RUN chmod +x gradlew

# Копируем исходный код
COPY src src

# Собираем приложение
RUN ./gradlew clean build -x test --no-daemon

# Используем runtime образ для запуска
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Устанавливаем зависимости для runtime
RUN apk add --no-cache curl

# Копируем собранное приложение
COPY --from=build /app/build/libs/*-all.jar app.jar

# Создаем директорию для логов
RUN mkdir -p /app/logs

# Открываем порт
EXPOSE 8080

# Добавляем проверку здоровья
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
