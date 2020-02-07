# Base the container image off of JDK 8
FROM openjdk:8

ENV WORK_DIR /c4c-backend

# Perform an update and upgrade to install any package updates
RUN apt-get update && apt-get upgrade -y

# Install maven
RUN apt-get install maven -y

# Change the default working directory of the image
WORKDIR ${WORK_DIR}

# Add some files
ADD api ${WORK_DIR}/api
ADD persist ${WORK_DIR}/persist
ADD service ${WORK_DIR}/service
ADD pom.xml ${WORK_DIR}/pom.xml
ADD build.sh ${WORK_DIR}/

# Expose the container ports to the host
EXPOSE 8090

# Build the software
RUN mvn -T 2C install
RUN mvn -T 2C package

# Set a default command to execute
CMD java -jar /c4c-backend/service/target/service-1.0-SNAPSHOT-jar-with-dependencies.jar
