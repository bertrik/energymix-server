FROM adoptopenjdk/openjdk14:jre-14.0.2_12-alpine
MAINTAINER Bertrik Sikken bertrik@gmail.com

ADD energymix-server/build/distributions/energymix-server.tar /opt/

WORKDIR /opt/energymix-server
ENTRYPOINT /opt/energymix-server/bin/energymix-server

