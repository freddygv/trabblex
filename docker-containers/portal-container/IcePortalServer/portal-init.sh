#!/bin/bash
./wait-for-it.sh postgres-server:5432 --timeout=0
mvn -U clean install exec:java
