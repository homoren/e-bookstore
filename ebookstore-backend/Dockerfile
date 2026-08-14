# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 先拷 pom 拉依赖(利用 Docker 层缓存)
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn -B clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
