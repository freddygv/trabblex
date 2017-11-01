#!/bin/bash
./wait-for-it.sh postgres-server:5432 --timeout=240
mvn -U clean install exec:java
