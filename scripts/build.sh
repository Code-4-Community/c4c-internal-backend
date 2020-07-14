#!/bin/bash

mvn clean install
java -jar "./service/target/service-1.0-SNAPSHOT-jar-with-dependencies.jar"
