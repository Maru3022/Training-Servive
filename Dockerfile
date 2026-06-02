# Этап 1: Сборка артефакта
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Копируем настройки и скачиваем зависимости (кешируем их)
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходный код и собираем проект
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Создание финального легкого образа
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем созданный JAR файл из этапа сборки
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8085

# Настройки для оптимизации памяти в Docker
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]