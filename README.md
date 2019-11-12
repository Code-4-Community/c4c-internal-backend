# C4C Internal Backend

Code 4 Community internal website backend to manage logins, meeting attendance, and requests from other organizations. Created using Vert.x in Java.

## API Endpoints

### Authorization
For any request to protected resources you should pass the JWT in the format of this string in the HTTP header Authorization as: 
	
	Authorization: Bearer <token>


### Endpoints
Note that routes that should modify data ought to be POST requests, this is being changed soon.

```sh
GET https://localhost:8443/signup?username=<username>&password=<password>
```
- Creates a member in the database the given username and password


```sh
GET https://localhost:8443/login?username=<username>&password=<password>
```
- Returns JWT in response header under "Authorization". 


```sh
GET https://localhost:8443/logout
```
- Given a valid JWT through the request header, will invalidate the JWT on the server-side


```sh
GET https://localhost:8443/api/v1/members/
```
- Returns a list of all of our members.


```sh
GET https://localhost:8443/protected/createmeeting?id=<id>&name=<name>&date=<yyyy-MM-dd HH-mm>&open=<open>
```
- Given a valid JWT through the request header, will create a meeting with:
- a unique id, 
- name, 
- date in yyyy-MM-dd HH:mm format (e.x 2019-12-25 12:05), 
- and a boolean representing if the meetting is still open. 

```sh
GET https://localhost:8443/https://localhost:8443/protected/attendmeeting?id=<id>
```
- Given a valid JWT through the request header, will attend the meeting with the meeting id






## Build Setup

### Java
Make sure you hava Java 8 installed, it can be downloaded [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

### Maven
Maven is used to install dependencies, build, and compile our Vert.x application into a jar file. Download it [here](https://maven.apache.org/download.cgi) or install it using homebrew/chocolatey. Maven must be added to the PATH environment variable on your computer to be used globally.

### Installation Part 1



Clone the repo into a folder.

```sh
$ git clone https://github.com/Code-4-Community/c4c-internal-backend.git
$ cd c4c-internal-backend
```

### PostgreSQL

Download PostgreSQL for your OS [here](https://www.postgresql.org/download/).

Again, make sure that there is a PATH to the /bin for PostgreSQL.

Set up a database running on localhost:5432 with a username of "postgres" and password "root".

Create the database "checkin" and import the tables from the inital migration .sql file. Where it sayse <file>, use the absolute path to the .sql file.

```sh
$ psql -U postgres
$ CREATE DATABASE checkin;
$ \q
$ psql -U postgres -d checkin -f <file> -h localhost
```

### Installation Part 2

```sh
$ cd c4c-internal-backend
$ mvn clean package
$ java -jar .\service\target\service-1.0-SNAPSHOT-jar-with-dependencies.jar
```
The application should be running on https://localhost:8090 and https://localhost:8443
