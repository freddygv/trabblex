#!/bin/bash
./wait-for-it.sh postgres-server:5432 --timeout=0
./wait-for-it.sh seedbox:8082 --timeout=0
mvn test exec:java
