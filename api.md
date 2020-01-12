# C4C Internal Backend API

Main document for cataloging all the API endpoints for this application including examples.

## Endpoints

### Authentication

* [Signup](apidocs/users.md#post-signup)
* [Login](apidocs/users.md#post-login)
* [Logout](apidocs/users.md#get-logout)

### Users

* [Get all users](apidocs/users.md#get-protectedusers)
* [Get a user by ID](apidocs/users.md#get-protecteduserid)
* [Update this user](apidocs/users.md#put-protecteduser)
* [Delete this user](apidocs/users.md#delete-protecteduser)


### Misc

* [Misc](apidocs/misc.md)






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
  "password": "password123",
  "currentYear": 1,
  "major": "Computer Science"
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
  "password": "password123",
  "currentYear": 1,
  "major": "Computer Science"
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
---

```sh
GET /admin/applicants
```

Returns a list of all of our applicants.

---

```sh
GET /admin/applicant/:userid
```

Gets information for the application whose user has this :id.

---

```sh
POST /protected/applicant
```

Takes JSON in request body in format:
- Base64String fileBLOB that represents the Base64 encoded binary string for this file
- String fileType, .pdf or .docx
- String[] interests
- String priorInvolvment
- String whyJoin

```json
{
	"fileBLOB": "aaaaaaaaaaaaaaaaaaa",
	"fileType": ".pdf",
	"interests": ["design", "outreach"],
	"priorInvolvement": "Msibdds bdu budsb udisbf iudsb ufbdsuifdsb dsa fdsf.",
	"whyJoin": "dsoi nfio disnio isn foisdnf id nioenion noew ifninf weion  sdf."
}
```

Creates a new applicant for the userId of the JWT.

---

```sh
PUT /protected/applicant
```

Takes JSON in request body in format:
- Base64String fileBLOB that represents the Base64 encoded binary string for this file
- String fileType, .pdf or .docx
- String[] interests
- String priorInvolvment
- String whyJoin

```json
{
	"fileBLOB": "aaaaaaaaaaaaaaaaaaa",
	"fileType": ".pdf",
	"interests": ["design", "outreach"],
	"priorInvolvement": "Msibdds bdu budsb udisbf iudsb ufbdsuifdsb dsa fdsf.",
	"whyJoin": "dsoi nfio disnio isn foisdnf id nioenion noew ifninf weion  sdf."
}
```

Updates and overwrites all information for the applicant with the userId of the JWT

---

```sh
DELETE /admin/applicant/:userid
```

Deletes the applicant with this :userid.


---

```sh
GET /news
```

Returns a list of all of our news.

---

```sh
GET /news/:id
```

Gets information for the news with this :id.

---

```sh
POST /admin/news
```

Takes JSON in request body in format:
- String title
- String description
- String author
- String date in yyyy-MM-dd HH:mm format (e.x 2019-12-25 12:05)
- String content

```json
{
	"title": "How to do Things",
	"description": "An article about doing things",
	"author": "John Doe",
	"date": "2020-01-01 12:00",
	"content": "Fake content."
}
```

Creates a new news post in the database with the given information.

---

```sh
PUT /admin/news/:id
```

Takes JSON in request body in format:
- String title
- String description
- String author
- String date in yyyy-MM-dd HH:mm format (e.x 2019-12-25 12:05)
- String content

```json
{
	"title": "How to do Things",
	"description": "An article about doing things",
	"author": "John Doe",
	"date": "2020-01-01 12:00",
	"content": "Fake content."
}
```

Updates and overwrites all information for the news with this :id

---

```sh
DELETE /admin/news/:id
```

Deletes the news with this :id.

---