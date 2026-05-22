# --- Этап 1: Сборка проекта ---
# Используем образ Maven с нужной версией JDK для сборки
FROM maven:3.9-eclipse-temurin-17 AS builder

# Устанавливаем рабочую директорию
WORKDIR /build

# Копируем только pom.xml для кэширования зависимостей
COPY backend/pom.xml ./

# Скачиваем все зависимости. Этот слой будет кэшироваться, если pom.xml не менялся.
RUN mvn dependency:go-offline

# Копируем весь исходный код бэкенда
COPY backend/src/ ./src

# Собираем .jar файл, пропуская тесты
RUN mvn clean package -DskipTests


# --- Этап 2: Создание финального образа ---
# Используем легкий образ JRE для запуска
FROM eclipse-temurin:17-jre-alpine

# Устанавливаем рабочую директорию в контейнере
WORKDIR /app

# Копируем ТОЛЬКО собранный .jar файл из сборочного этапа
COPY --from=builder /build/target/scrum-poker-backend-1.0.0.jar app.jar

# Открываем порт, на котором работает приложение
EXPOSE 8080

# Команда для запуска приложения при старте контейнера
ENTRYPOINT ["java", "-jar", "app.jar"]
