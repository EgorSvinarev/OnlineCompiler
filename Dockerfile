FROM ubuntu:latest  
  
MAINTAINER Egor Svinarev "egorsvinarevvv@gmail.com"  
  
# update source  
RUN apt-get update  
RUN apt install -y software-properties-common
  
# Install JDK-11 
RUN apt-get -y install openjdk-11-jdk

#Install curl
RUN apt-get -y install curl

#Install vim
RUN apt-get update
RUN apt-get -y install vim

#Install iptables
RUN  apt-get -y install iptables

# Install Tomcat9 
RUN cd /tmp && curl -L 'http://archive.apache.org/dist/tomcat/tomcat-9/v9.0.53/bin/apache-tomcat-9.0.53.tar.gz' | tar -xz  
RUN mv /tmp/apache-tomcat-9.0.53/ /opt/tomcat9/   

# Install pip3
RUN apt-get update && apt-get install -y python3-pip

# Install packages
RUN pip3 install numpy pandas pythonwhat

ENV CATALINA_HOME /opt/tomcat9  
ENV PATH $PATH:$CATALINA_HOME/bin  
 
# Expose ports.  
EXPOSE 8080  
 
ARG JAR_FILE=target/online-compiler-0.0.1-SNAPSHOT.jar
ARG COMPILE_DIST_FOLDER=programs
ARG CONFIG_FOLDER=config
ARG WHITELIST_FOLDER=whitelist

COPY ${JAR_FILE} app.jar
ADD ${COMPILE_DIST_FOLDER} programs
ADD ${CONFIG_FOLDER} config
ADD ${WHITELIST_FOLDER} whitelist

ENTRYPOINT ["java", "-jar", "app.jar"]