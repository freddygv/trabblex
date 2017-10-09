# Docker builds
Requires: Docker installation. The commands below were tested on UNIX.

In this directory run: `docker build .`

After the container finishes building, it will output an image_id that looks something like this: 135093e9f918

Replace [IMAGE_ID] with that id below, and run:
`docker run --name postgres-server -d -p 0.0.0.0:5432:5432 -e POSTGRES_USER=driver -e POSTGRES_PASSWORD=verylongandsecurerootpassword -e POSTGRES_DB=prod [IMAGE_ID]`

When connecting to the database locally you can use: 0.0.0.0:5432 as the host and port.

You will also need to provide the username and password provided above.
