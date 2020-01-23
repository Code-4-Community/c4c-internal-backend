#!bin/bash

echo "building and running server, may take around 20-40 seconds"

mvn clean package

java -jar .\\service\\target\\service-1.0-SNAPSHOT-jar-with-dependencies.jar