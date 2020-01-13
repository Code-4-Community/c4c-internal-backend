# News API

  

This API is for getting, creating, and updating user objects. All request and response bodies will be of type JSON and include an appropriate `Content-Type: application/json` header.


## `GET /news`

  

Returns a list of all of our news.

  

### Authorization Requirements

  

None
  

### Responses

  

#### `200 OK`

  

Every thing is okay.

  
  

```json
[
{
	"title": STRING,
	"description": STRING,
	"author": STRING,
	"date": DATE,
	"content": STRING
},

...

{
	"title": STRING,
	"description": STRING,
	"author": STRING,
	"date": DATE,
	"content": STRING
}
]
```
**DATE** is in the format `YYYY-MM-DD HH:mm`
  

#### `400 BAD REQUEST`

  

This happens if the client sends a request that does not conform to the standard

outlined above.

  
#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `GET /news/:id`

  

Gets information for the news with this :id.

  

### Authorization Requirements

  

None

  

### Responses

  

#### `200 OK`

  

Every thing is okay.

  
  

```json
{
	"title": STRING,
	"description": STRING,
	"author": STRING,
	"date": DATE,
	"content": STRING
}
```

**DATE** is in the format `YYYY-MM-DD HH:mm`

  

#### `400 BAD REQUEST`

  

This happens if the client sends a request that does not conform to the standard outlined above.

  
#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `POST /admin/news`

Creates a new news post in the database with the given information.

  
### Authorization Requirements

Requires a valid JWT and admin privleges

### Request Body

  

```json
{
	"title": STRING,
	"description": STRING,
	"author": STRING,
	"date": DATE,
	"content": STRING
}
```
**DATE** is in the format `YYYY-MM-DD HH:mm`

  

### Responses

  

#### `200 OK`

  

The news post was updated successfully.
  

#### `400 BAD REQUEST`

  

The request body was malformed according to the specification

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.


## `PUT /admin/news/:id`


Updates and overwrites all information for the news post with the given id
  
### Authorization Requirements

Requires a valid JWT and admin privleges

### Request Body

  

```json
{
	"title": STRING,
	"description": STRING,
	"author": STRING,
	"date": DATE,
	"content": STRING
}
```
**DATE** is in the format `YYYY-MM-DD HH:mm`

  

### Responses

  

#### `200 OK`

  

The event was updated successfully.
  

#### `400 BAD REQUEST`

The request body was malformed according to the specification

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `DELETE /admin/news/:id`

  

Deletes the event with the given id.
  
### Authorization Requirements

Requires a valid JWT and admin privleges

### Responses

  

#### `200 OK`


The user was deleted successfully

#### `400 BAD REQUEST`

The request body was malformed according to the specification

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.