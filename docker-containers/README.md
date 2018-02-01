# Docker builds
Requires: Docker installation. The deployment was tested on:

Docker 17.09.0-ce

Docker-Compose 1.16.1, build 6d1ac21

----------

Build docker containers with: `docker-compose build`

Run docker containers locally with: `docker-compose up`

Push docker containers to GCR with `docker-compose push`

Deploy to Google Container Clusters with `./deploy.sh`

Bring down services with `./teardown.sh`
