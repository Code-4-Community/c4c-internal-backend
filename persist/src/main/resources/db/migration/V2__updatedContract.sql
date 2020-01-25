ALTER TABLE events 
ADD COLUMN subtitle TEXT;

ALTER TABLE events 
ADD COLUMN description TEXT;

ALTER TABLE events 
ADD COLUMN image_url VARCHAR
(255);

ALTER TABLE news 
ADD COLUMN image_url VARCHAR
(255);

