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

Set up a database named c4cneu-db running on localhost:5432 with a username of "postgres" and password "root".

Create the database "c4cneu-db", tables will be generated automatically during `mvn clean install` by flyway.

```sh
$ psql -U postgres
$ CREATE DATABASE c4cneu-db;
```

### Compiling & Running

Update the properties file in `/persist/src/main/resources/db.properties` to contain your database connection information

```
database.driver = org.postgresql.Driver
database.url = jdbc:postgresql://localhost:5432/c4cneu-db?autoreconnect=true
database.username = postgres
database.password = root
jwt.secret = SUPER_SECRET_KEY
jwt.duration = 3600000

```

Update the properties file in `/persist/src/main/resources/server.properties` to contain the port the server should run on

```
server.port = 8090
```

To compile and run:

```sh
$ cd c4c-internal-backend
$ mvn clean install
$ sh build.sh
```

The application should be running on https://localhost:8090

### Testing

API testing is done with newman, the cli for Postman. The collection JSON (including API tests) should be `/apitest` folder. Before you star testing, make sure you have an admin user already in your user table. To do so, run the following commands:

```sh
$ psql -U postgres
$ \c c4cneu-db;
$ insert into users (email, first_name, last_name, hashed_password, current_year, major, privilege_level) values ('admin@husky.neu.edu', 'admin', 'admin',  '$2a$12$TsV9egpd1IXx013lVLpo5.OPbxI0w3EuObh8..gD4mR7YqCV7Md1W', 5, 'Computer Science', 1);
```

This will create a user with admin privileges with the password expected in the test collection.

Now navigate to the `/apitest` folder and run

```sh
$ newman "Local Testing.postman_collection.json"
```

If you need to add more tests, simply go to the Postman website or download their desktop app and import `Local Testing.postman_collection.json`, then add your new tests and replace the test.json in `/newman`.
