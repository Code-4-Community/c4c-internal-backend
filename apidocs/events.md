# Event API

  

This API is for getting, creating, and updating user objects. All request and response bodies will be of type JSON and include an appropriate `Content-Type: application/json` header.


## `GET /protected/events`

  

Returns a list of all of our events.

  

### Authorization Requirements

  

Requires a valid JWT

  

### Responses

  

#### `200 OK`

  

Every thing is okay.

  
  

```json
[
{
  "name": STRING,
  "date": DATE,
  "open": BOOLEAN,
  "code": STRING
},

...

{
  "name": STRING,
  "date": DATE,
  "open": BOOLEAN,
  "code": STRING
}
]
```
**DATE** is in the format `YYYY-MM-DD HH:mm`
  

#### `400 BAD REQUEST`

  

This happens if the client sends a request that does not conform to the standard

outlined above.

  
#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `GET /protected/event/:id`

  

Gets information for the event with the given id.

  

### Authorization Requirements

  

Requires a valid JWT

  

### Responses

  

#### `200 OK`

  

Every thing is okay.

  
  

```json
{
  "name": STRING,
  "date": DATE,
  "open": BOOLEAN,
  "code": STRING
}
```

**DATE** is in the format `YYYY-MM-DD HH:mm`

  

#### `400 BAD REQUEST`

  

This happens if the client sends a request that does not conform to the standard outlined above.

  
#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `POST /admin/event`

  

Creates a new event in the database with the given information.
  
### Authorization Requirements

Requires a valid JWT and admin privleges

### Request Body

  

```json
{
  "name": STRING,
  "date": STRING,
  "open": BOOLEAN,
  "code": STRING
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


## `PUT /admin/event/:id`


Updates and overwrites all information for the meeting with the given id
  
### Authorization Requirements

Requires a valid JWT and admin privleges

### Request Body

  

```json
{
  "name": STRING,
  "date": DATE,
  "open": BOOLEAN,
  "code": STRING
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

## `DELETE /admin/event/:id`

  

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

## `GET /protected/eventcheckin/:id`

Gets a list of users that are attending the meeting with the given id.
  
### Authorization Requirements

Requires a valid JWT

### Responses

#### `200 OK`

Every thing is okay.

  
```json
[
{
"email": STRING,
"firstName": STRING,
"lastName": STRING,
"currentYear": INTEGER,
"major": STRING
},

...

{
"email": STRING,
"firstName": STRING,
"lastName": STRING,
"currentYear": INTEGER,
"major": STRING
}
]
```

#### `400 BAD REQUEST`

The request body was malformed according to the specification

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `POST /protected/eventcheckin/:code`

Adds the user associated with this JWT to the members attending the meeting with the given code.

### Authorization Requirements

Requires a valid JWT

### Responses

#### `201 OK`

The given event updated with this user id.

#### `400 BAD REQUEST`

The request body was malformed according to the specification

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

