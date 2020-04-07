## This is a Dockerfile TEMPLATE intended for use with https://github.com/fabric8io/docker-maven-plugin

#########
## MANUAL - Commands to prep/build (if you cannot use the maven plugin for whatever reason)
# In pom.xml - comment out the use of the maven-dependency-plugin and docker-maven-plugin
# Navigate to the /package folder
# mvn clean dependency:copy-dependencies
# docker build -t fhexec:1.0.2 .

########
#Dockerfile Contents
####

FROM alpine:3.11

# Performs the following as a single statement - only requires one layer
#
# 1) Removes ca-certs to be re-installed by the OpenJDK for the update-ca-certificates command deleted by the
#    hardened image - this ensures /etc/ssl/certs/java gets properly populated with cacerts keystore
# 2) Re-do: "cleanup of all but a handful of admin commands"

RUN apk del --no-cache ca-certificates && \
    apk add --no-cache openjdk8 && \
    find /sbin /usr/sbin ! -type d -a ! -name apk -a ! -name ln -delete

ENV JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk
ENV PATH="$JAVA_HOME/bin:${PATH}"
ENV LANG="C.UTF-8" JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF8"

ADD target/dependency /app
CMD java -classpath app/pop-handler-executor-impl-${application.version}.jar HandlerEntryPoint
