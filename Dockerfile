# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from your host to the container
COPY target/lm.api.registration-0.1.jar /app/app.jar

# Expose the port your application runs on
EXPOSE 8200

# Command to run your application
CMD ["java", "-jar", "app.jar"]
