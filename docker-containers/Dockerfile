FROM maven:3.5-jdk-8-alpine
ADD wait-for-it.sh .
CMD ["chmod", "+x", "wait-for-it.sh"]
CMD ["./wait-for-it.sh", "postgres-server:5432", "--timeout=240"]

ADD pom.xml /app/
ADD src/ /app/src/
ADD src/ /app/src/
WORKDIR /app/
CMD ["mvn", "package"]
CMD ["mvn", "exec:java"]
