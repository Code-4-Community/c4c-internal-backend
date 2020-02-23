#!/bin/bash

DIR_WORK=/c4c-backend
DIR_PERSIST=${DIR_WORK}/persist/src/main/resources
DIR_JAR=${DIR_WORK}/service/target

# Check for updated database username
if [[ -z "${CFC_DB_USERNAME}" ]]; then
    echo "ERROR: environment variable not set for database username"
    exit 1
fi

# Check for updated database password
if [[ -z "${CFC_DB_PASSWORD}" ]]; then
    echo "ERROR: environment variable not set for database password"
    exit 1
fi

# Check for existing db.properties file
if [[ ! -f "${DIR_PERSIST}/db.properties" ]]; then
    echo "ERROR: \"db.properties\" file not found"
    exit 1
fi

# Update the "db.properties" file
cd ${DIR_PERSIST}
mv db.properties input.properties

awk -F" = " -v updatedVal="= $CFC_DB_USERNAME" '/database.username =/{$2=updatedVal}1' input.properties > user.properties
awk -F" = " -v updatedVal="= $CFC_DB_PASSWORD" '/database.password =/{$2=updatedVal}1' user.properties > pass.properties
awk -F" = " -v updatedVal="= $CFC_DB_URL" '/database.url =/{$2=updatedVal}1' pass.properties > db.properties
rm user.properties
rm pass.properties
rm input.properties

echo "SUCCESS: updated database credentials"

# Perform maven install/package
cd ${DIR_WORK}
mvn -T 2C install
mvn -T 2C package

# Execute the jar file
java -jar ${DIR_JAR}/service-1.0-SNAPSHOT-jar-with-dependencies.jar
