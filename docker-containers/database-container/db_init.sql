CREATE TABLE videos (
  file_hash VARCHAR(64) NOT NULL PRIMARY KEY,
  file_name VARCHAR(64) NOT NULL,
  file_size VARCHAR(10) NOT NULL,
  protocol VARCHAR(3) NOT NULL,
  video_size_x SMALLINT NOT NULL,
  video_size_y SMALLINT NOT NULL,
  bitrate VARCHAR(10) NOT NULL,
  seeder_is_active BOOLEAN NOT NULL
);

CREATE TABLE file_keywords (
  file_hash VARCHAR(64) NOT NULL REFERENCES videos (file_hash),
  keyword VARCHAR(64) NOT NULL
);

CREATE TABLE chunk_owners (
  file_hash VARCHAR(64) NOT NULL,
  chunk_hash VARCHAR(64) NOT NULL,
  chunk_id SMALLINT NOT NULL,
  owner_ip VARCHAR(64) NOT NULL,
  owner_port SMALLINT NOT NULL,
  is_seeder BOOLEAN NOT NULL
);


INSERT INTO videos (file_hash, file_name, file_size, protocol, port, video_size_x, video_size_y, bitrate, seeder_is_active)
VALUES('C52057A4A2555D007A6B2D13FE2BBCA813AF936EBC3E26ACFF13CFAD5C54CED5', 'tl_512kb.mp4', '27546', 'TCP', '30880', '320', '240', '800', 'f');

INSERT INTO videos (file_hash, file_name, file_size, protocol, port, video_size_x, video_size_y, bitrate, seeder_is_active)
VALUES('0ED4C30A5625D538DEC9F1CF39BE643439CADF42AA27CEC17935DC0E7B073B25', 'PopeyeAliBaba_512kb.mp4', '75674', 'TCP', '30240', '320', '240', '700', 'f');

INSERT INTO videos (file_hash, file_name, file_size, protocol, port, video_size_x, video_size_y, bitrate, seeder_is_active)
VALUES('B07E7827BAAC4009C173DF2DFEDEC186CFDDE1AAB6CEBBF02C9D7C52D04A26EE', 'CC_1916_07_10_TheVagabond.mp4', '153600', 'TCP', '30000', '640', '480', '1024', 'f');
