FROM openjdk:8

# Install maven
RUN apt-get -y update && apt-get install -y maven

WORKDIR /code

# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
ADD server.xml /code/server.xml

# Adding source, compile and package into a fat jar
ADD src /code/src
RUN ["mvn", "package"]

EXPOSE 8088
CMD ["mvn", "tomcat7:run"]
