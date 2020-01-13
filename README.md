# C4C Internal Backend

Code 4 Community internal website backend to manage logins, event attendance, and requests from other organizations. Created using Vert.x in Java.

## [API Endpoints](api.md)



## Build Setup

### Java

Make sure you hava Java 8 installed, it can be downloaded [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

### Maven

Maven is used to install dependencies, build, and compile our Vert.x application into a jar file. Download it [here](https://maven.apache.org/download.cgi) or install it using homebrew/chocolatey. Maven must be added to the PATH environment variable on your computer to be used globally.

### Installation

Clone the repo into a folder.

```sh
$ git clone https://github.com/Code-4-Community/c4c-internal-backend.git
$ cd c4c-internal-backend
```

### PostgreSQL

Download PostgreSQL for your OS [here](https://www.postgresql.org/download/).

Again, make sure that there is a PATH to the /bin for PostgreSQL.

Set up a database running on localhost:5432 with a username of "postgres" and password "root".

Create the database "c4cneu-db" and import the tables from the inital migration .sql file. Where it sayse <file>, use the absolute path to the .sql file.

```sh
$ psql -U postgres
$ CREATE DATABASE c4cneu-db;
$ \q
$ psql -U postgres -d c4cneu-db -f <file> -h localhost
```

### Compiling & Running

```sh
$ cd c4c-internal-backend
$ mvn clean package
$ java -jar .\service\target\service-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The application should be running on https://localhost:8090 and https://localhost:8443
