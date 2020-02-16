# Base the container image off JDK 8
FROM openjdk:8

# Set the working dir env var
ENV WORK_DIR /c4c-backend

# Expose container port to host
EXPOSE 8090

# Set default working director
WORKDIR ${WORK_DIR}

# Run necessary tasks
RUN apt-get update -y
RUN apt-get upgrade -y
RUN apt-get install maven -y

# Add some files
ADD api ${WORK_DIR}/api
ADD persist ${WORK_DIR}/persist
ADD service ${WORK_DIR}/service
ADD pom.xml ${WORK_DIR}/pom.xml
ADD scripts ${WORK_DIR}

# Set a default command to execute
CMD /c4c-backend/deploy.sh
