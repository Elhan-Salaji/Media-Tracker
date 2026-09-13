# Stage 1: Build
# The Temurin JDK plus the Maven Wrapper, the same pair CI uses. The wrapper pins
# the Maven version, so no Maven image with its own Java tag is involved.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Sicherheit: Damit das Image nicht über root arbeitet wird ne gruppe Spring und der user Spring erstellt damit wird das Image dann gestartet
RUN addgroup -S spring && adduser -S spring -G spring
USER spring
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]