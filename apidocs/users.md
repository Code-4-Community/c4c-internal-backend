
# User API

  

This API is for getting, creating, and updating user objects. All request and response bodies will be of type JSON and include an appropriate `Content-Type: application/json` header.

## `POST /signup`

  

Used for creating a user to be stored in the database.

  

### Authorization Requirements

  

None.

  

### Request Body

  

```json
{
"email": STRING,
"firstName": STRING,
"lastName": STRING,
"password": STRING,
"currentYear": INTEGER,
"major": STRING
}
```

  

### Responses

  

#### `201 OK`

  

User was successfully added to the database.

  

#### `400 BAD REQUEST`

  

The request body was malformed according to the specification.

  
## `POST /login`

  

Used for creating a user to be stored in the database.

  

### Authorization Requirements

  

None.

  

### Request Body

  

```json
{
"email": "john.doe@email.com",
"password": "password123"
}
```

  

### Responses

  

#### `201 OK`

  

Returns a JWT in response header under "Authorization" that expires in 60 minutes.
  

#### `400 BAD REQUEST`

  

The request body was malformed according to the specification.



## `GET /logout`

  

Given a valid JWT through the request header, will invalidate the JWT on the server-side.
  

### Authorization Requirements

  

Requires a valid JWT token.

  


### Responses

  

#### `201 OK`

  User logged out, token successfully blacklisted.
  

#### `400 BAD REQUEST`

  

The request body was malformed according to the specification.

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `GET /protected/users`

  

Used for getting a list of all users in the database.

  

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

  

This happens if the client sends a request that does not conform to the standard

outlined above.

  
#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `GET /protected/user/:id`

  

Used for getting a single user with the given id in the database.

  

### Authorization Requirements

  

Requires a valid JWT

  

### Responses

  

#### `200 OK`

  

Every thing is okay.

  
  

```json
{
"email": STRING,
"firstName": STRING,
"lastName": STRING,
"currentYear": INTEGER,
"major": STRING
}
```

  
  

#### `400 BAD REQUEST`

  

This happens if the client sends a request that does not conform to the standard outlined above.

  
#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `PUT /protected/user`

  

Updates and overwrites all information for the user with the id in the JWT.
  
### Authorization Requirements

Requires a valid JWT

### Request Body

  

```json
{
"email": STRING,
"firstName": STRING,
"lastName": STRING,
"password": STRING,
"currentYear": INTEGER,
"major": STRING
}
```

  

### Responses

  

#### `200 OK`

  

The user was updated successfully.
  

#### `400 BAD REQUEST`

  

The request body was malformed according to the specification

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `DELETE /protected/user`

  

Deletes the user with the id in the JWT.
  
### Authorization Requirements

Requires a valid JWT

### Responses

  

#### `200 OK`


The user was deleted successfully

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.