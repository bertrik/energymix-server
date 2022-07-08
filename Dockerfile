FROM openjdk:11.0.4-jre-slim
MAINTAINER Bertrik Sikken bertrik@gmail.com

ADD energymix-server/build/distributions/energymix-server.tar /opt/

WORKDIR /opt/energymix-server
ENTRYPOINT /opt/energymix-server/bin/energymix-server

