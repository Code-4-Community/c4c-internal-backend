CREATE TABLE IF NOT EXISTS member (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  email VARCHAR(36),
  first_name VARCHAR(36),
  last_name VARCHAR(36),
  graduation_year INT,
  major VARCHAR(36),
  privilege_level INT
);

CREATE TABLE IF NOT EXISTS meeting (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(36),
  date TIMESTAMP,
  open BOOLEAN
);

CREATE TABLE IF NOT EXISTS member_attended_meeting (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  member_id VARCHAR(36),
  meeting_id VARCHAR(36),

  CONSTRAINT fk_attended_member FOREIGN KEY (member_id) REFERENCES member (id),
  CONSTRAINT fk_attended_meeting FOREIGN KEY (meeting_id) REFERENCES meeting (id)
);