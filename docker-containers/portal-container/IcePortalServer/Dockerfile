FROM openjdk:8-jdk-alpine

# ----

RUN apk add --no-cache curl tar bash
ARG MAVEN_VERSION=3.5.2
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven && \
curl -fsSL http://mirrors.up.pt/pub/apache/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 && \
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# ----

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY src                /usr/src/app/src
COPY pom.xml            /usr/src/app
COPY db.properties      /usr/src/app
COPY repo               /usr/src/app/repo

COPY wait-for-it.sh   /usr/src/app
COPY portal-init.sh  /usr/src/app

RUN chmod +x wait-for-it.sh
RUN chmod +x portal-init.sh

CMD ["./portal-init.sh"]
