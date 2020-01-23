# C4C Internal Backend API

Main document for cataloging all the API endpoints for this application including examples.

### Authorization

For any request to protected resources (/protected/_ or /admin/_) you should pass the JWT in the format of this string in the HTTP header Authorization as:
`Authorization: Bearer <token>`

## Endpoints

### Users & Authentication

- [Signup](apidocs/users.md#post-signup)
- [Login](apidocs/users.md#post-login)
- [Logout](apidocs/users.md#get-logout)

- [Get all users](apidocs/users.md#get-protectedusers)
- [Get a user by ID](apidocs/users.md#get-protecteduserid)
- [Update this user](apidocs/users.md#put-protecteduser)
- [Delete this user](apidocs/users.md#delete-protecteduser)

### Events & Checkins

- [Get all events](apidocs/events.md#get-protectedevents)
- [Get an event by ID](apidocs/events.md#get-protectedeventid)
- [Create an event](apidocs/events.md#post-adminevent)
- [Update an event by ID](apidocs/events.md#put-admineventid)
- [Delete an event by ID](apidocs/events.md#delete-admineventid)

- [Get the list of users attending an event](apidocs/events.md#get-protectedeventcheckinid)
- [Attend an event by event code](apidocs/events.md#post-protectedeventcheckincode)

### Applicants

- [Get all applicants](apidocs/applicants.md#get-adminapplicants)
- [Get an applicant by ID](apidocs/applicants.md#get-adminapplicantuserid)
- [Create an applicant](apidocs/applicants.md#post-protectedapplicant)
- [Update this applicant](apidocs/applicants.md#put-protectedapplicant)
- [Delete applicant by ID](apidocs/applicants.md#delete-adminapplicantuserid)

### News

- [Get all news posts](apidocs/news.md#get-news)
- [Get a news post by ID](apidocs/news.md#get-newsid)
- [Create a news post](apidocs/news.md#post-adminnews)
- [Update a news post by ID](apidocs/news.md#put-adminnewsid)
- [Delete a news post by ID](apidocs/news.md#delete-adminnewsid)

### Misc

- [Misc](apidocs/misc.md#get-home)
