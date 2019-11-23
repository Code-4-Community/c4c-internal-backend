# C4C Internal Backend

Code 4 Community internal website backend to manage logins, event attendance, and requests from other organizations. Created using Vert.x in Java.

## API Endpoints

### Authorization

For any request to protected resources (/protected/_ or /admin/_) you should pass the JWT in the format of this string in the HTTP header Authorization as:
Authorization: Bearer <token>

### Endpoints

```sh
GET /
```

Gets the public "home" page.

---

```sh
POST /signup
```

Takes JSON in request body in format:
 - String email
 - String firstName
 - String lastName
 - String password

```json
{
  "email": "john.doe@email.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123"
}
```

Creates a user in the database with the given username and password.

---

```sh
POST /login
```

Takes JSON in request body in format:
 - String email
 - String password

```json
{
  "email": "john.doe@email.com",
  "password": "password123"
}
```

Returns a JWT in response header under "Authorization".

---

```sh
GET /logout
```

Given a valid JWT through the request header, will invalidate the JWT on the server-side.

---

```sh
GET /protected/users
```

Returns a list of all of our users.

---

```sh
GET /protected/user/:id
```

Returns the information for the user with this :id.

---

```sh
PUT /protected/user
```

Takes JSON in request body in format:
 - String email
 - String firstName
 - String lastName
 - String password
```json
{
  "email": "john.doe@email.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123"
}
```

Updates and overwrites all information for the user with the id in the JWT.

---

```sh
DELETE /protected/user
```

Deletes the user with the id in the JWT.

---

```sh
GET /protected/events
```

Returns a list of all of our events.

---

```sh
GET /protected/event/:id
```

Gets information for the event with this :id.

---

```sh
POST /admin/event
```

Takes JSON in request body in format:
- String name
- String date in yyyy-MM-dd HH:mm format (e.x 2019-12-25 12:05)
- boolean open
- String code

```json
{
  "name": "Introduction to Vert.x",
  "date": "2019-12-25 12:05",
  "open": true,
  "code": "Meeting Code"
}
```

Creates a new event in the database with the given information.

---

```sh
PUT /admin/event/:id
```

Takes JSON in request body in format:
- String name
- String date in yyyy-MM-dd HH:mm format (e.x 2019-12-25 12:05)
- boolean open
- String code

```json
{
  "name": "Introduction to Vert.x",
  "date": "2019-12-25 12:05",
  "open": true,
  "code": "Meeting Code"
}
```

Updates and overwrites all information for the meeting with this :id

---

```sh
DELETE /admin/event/:id
```

Deletes the event with this :id.

---

```sh
GET /protected/eventcheckin/:id
```

Gets all the users in an event with this :id

---

```sh
POST /protected/eventcheckin/:code
```

Check in the user with id in the given JWT to the event with this :code

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
