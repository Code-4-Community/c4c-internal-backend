CREATE TABLE
IF NOT EXISTS users
(
  id SERIAL
PRIMARY KEY,
  email VARCHAR 
(255) UNIQUE,
  first_name VARCHAR
(255),
  last_name VARCHAR
(255),
  hashed_password VARCHAR
(255),
  graduation_year INT,
  major VARCHAR
(255),
  privilege_level INT
);

CREATE TABLE
IF NOT EXISTS events
(
  id SERIAL PRIMARY KEY,
  name VARCHAR
(255),
  date TIMESTAMP,
  open BOOLEAN,
  code VARCHAR
(255) UNIQUE
);

CREATE TABLE
IF NOT EXISTS event_check_ins
(
  id SERIAL PRIMARY KEY,
  user_id INT,
  event_id INT,

  CONSTRAINT fk_attended_user FOREIGN KEY
(user_id) REFERENCES users
(id),
  CONSTRAINT fk_attended_event FOREIGN KEY
(event_id) REFERENCES events
(id)
);

CREATE TABLE
IF NOT EXISTS blacklisted_tokens
(
  id VARCHAR(255) NOT NULL PRIMARY KEY,
  time_milliseconds BIGINT
);

CREATE TABLE
IF NOT EXISTS news
(
  id SERIAL
PRIMARY KEY,
  title VARCHAR
(255),
  description VARCHAR
(255),
  author VARCHAR
(255),
  date TIMESTAMP,
  content VARCHAR
(255)
);