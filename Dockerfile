# Stage 1: Build WAR bằng Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy file pom và download dependency trước (tối ưu build)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy toàn bộ source code
COPY src ./src

# Build WAR
RUN mvn clean package -DskipTests

# Stage 2: Deploy WAR vào Tomcat
FROM tomcat:10.1-jdk17

# Xóa ứng dụng ROOT mặc định của Tomcat
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy file .war sang Tomcat và đổi tên thành ROOT.war
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
