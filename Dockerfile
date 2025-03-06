# Use official Google Cloud SDK image
FROM google/cloud-sdk:latest

# Set working directory
WORKDIR /app

# Install Java & Maven
RUN apt-get update && apt-get install -y openjdk-17-jdk maven

# Copy test project
COPY . .

# Set Google credentials for authentication
ENV GOOGLE_APPLICATION_CREDENTIALS="/gcloud/service-account.json"

# Install dependencies
RUN mvn clean install

# Set default command (run tests in parallel)
CMD ["mvn", "test"]
