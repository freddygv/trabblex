CREATE TABLE seeders (
   seeder_ip VARCHAR(45) NOT NULL,
   file_hash VARCHAR(64) NOT NULL,
   file_name VARCHAR(64) NOT NULL,
   file_size SMALLINT NOT NULL,
   protocol VARCHAR(3) NOT NULL,
   port SMALLINT NOT NULL,
   video_size_x SMALLINT NOT NULL,
   video_size_y SMALLINT NOT NULL,
   bitrate SMALLINT NOT NULL,
   PRIMARY KEY ( file_hash )
);

CREATE TABLE file_keywords (
   file_hash VARCHAR(64) NOT NULL,
   keyword VARCHAR(64) NOT NULL
);

CREATE TABLE client_downloads (
   client_ip VARCHAR(45) NOT NULL,
   file_hash VARCHAR(64) NOT NULL,
   file_name VARCHAR(64) NOT NULL,
   file_size SMALLINT NOT NULL,
   full_path VARCHAR(64) NOT NULL,
   is_downloaded BOOLEAN NOT NULL
);

CREATE TABLE peers (
   source_peer_ip VARCHAR(64) NOT NULL,
   target_peer_ip VARCHAR(64) NOT NULL
);

CREATE TABLE chunk_owners (
   chunk_hash VARCHAR(64) NOT NULL,
   owner_ip VARCHAR(64) NOT NULL
);
