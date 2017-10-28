CREATE TABLE seeders (
  file_hash VARCHAR(64) NOT NULL PRIMARY KEY,
  file_name VARCHAR(64) NOT NULL,
  file_size VARCHAR(10) NOT NULL,
  protocol VARCHAR(3) NOT NULL,
  port SMALLINT NOT NULL,
  video_size_x SMALLINT NOT NULL,
  video_size_y SMALLINT NOT NULL,
  bitrate VARCHAR(10) NOT NULL
);

CREATE TABLE file_keywords (
  file_hash VARCHAR(64) NOT NULL REFERENCES seeders (file_hash),
  keyword VARCHAR(64) NOT NULL
);

CREATE TABLE chunk_owners (
  file_hash VARCHAR(64) NOT NULL,
  chunk_hash VARCHAR(64) NOT NULL,
  owner_ip VARCHAR(64) NOT NULL,
  owner_port SMALLINT NOT NULL,
  is_seeder BOOLEAN NOT NULL
);
