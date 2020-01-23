# Applicant API

  

This API is for getting, creating, and updating user objects. All request and response bodies will be of type JSON and include an appropriate `Content-Type: application/json` header.


## `GET /admin/applicants`

  

Returns a list of all of our applicants.
  

### Authorization Requirements

  

Requires a valid JWT and admin privleges

  

### Responses

  

#### `200 OK`

  

Every thing is okay.

  
  

```json
[
{
  "userId": INTEGER,
  "fileBLOB": STRING,
	"fileType": FILETYPE,
	"interests": STRING[],
	"priorInvolvement": STRING,
	"whyJoin": STRING
},

...

{
  "userId": INTEGER,
  "fileBLOB": STRING,
	"fileType": FILETYPE,
	"interests": STRING[],
	"priorInvolvement": STRING,
	"whyJoin": STRING
}
]
```
**FILETYPE** is a **STRING** in the format ".pdf" or another file extension.
  

#### `400 BAD REQUEST`

  

This happens if the client sends a request that does not conform to the standard outlined above.

  
#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `GET /admin/applicant/:userid`

  

Gets information for the application whose user has the given id.

  

### Authorization Requirements

  

Requires a valid JWT and admin privleges

  

### Responses

  

#### `200 OK`

  

Every thing is okay.

  
  

```json
{
  "userId": INTEGER,
  "fileBLOB": STRING,
	"fileType": FILETYPE,
	"interests": STRING[],
	"priorInvolvement": STRING,
	"whyJoin": STRING
}
```

**FILETYPE** is a **STRING** in the format ".pdf" or another file extension.

  

#### `400 BAD REQUEST`

  

This happens if the client sends a request that does not conform to the standard outlined above.

  
#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `POST /protected/applicant`

Creates a new applicant for the userId of the JWT.

  
### Authorization Requirements

Requires a valid JWT

### Request Body

  

```json
{
  "fileBLOB": STRING,
	"fileType": FILETYPE,
	"interests": STRING[],
	"priorInvolvement": STRING,
	"whyJoin": STRING
}
```
**FILETYPE** is a **STRING** in the format ".pdf" or another file extension.

  

### Responses

  

#### `200 OK`

  

The event was updated successfully.
  

#### `400 BAD REQUEST`

  

The request body was malformed according to the specification

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.


## `PUT /protected/applicant`


Updates and overwrites all information for the applicant with the userId of the JWT
  
### Authorization Requirements

Requires a valid JWT

### Request Body


```json
{
  "fileBLOB": STRING,
	"fileType": FILETYPE,
	"interests": STRING[],
	"priorInvolvement": STRING,
	"whyJoin": STRING
}
```
**FILETYPE** is a **STRING** in the format ".pdf" or another file extension.

  

### Responses

  

#### `200 OK`

  

The event was updated successfully.
  

#### `400 BAD REQUEST`

The request body was malformed according to the specification

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.

## `DELETE /admin/applicant/:userid`

Deletes the applicant with the given userid.

  
### Authorization Requirements

Requires a valid JWT and admin privleges

### Responses

  

#### `200 OK`


The user was deleted successfully

#### `400 BAD REQUEST`

The request body was malformed according to the specification

#### `401 UNAUTHORIZED`

Not sufficent authorization, either the JWT is invalid or does not have sufficent prvileges.