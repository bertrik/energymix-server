FROM adoptopenjdk/openjdk14:jre-14.0.2_12-alpine

LABEL maintainer="Bertrik Sikken bertrik@gmail.com"
LABEL org.opencontainers.image.source="https://github.com/bertrik/energymix-server"
LABEL org.opencontainers.image.description="Collects energy-related data and republishes it over a simple REST API"
LABEL org.opencontainers.image.licenses="MIT"

ADD energymix-server/build/distributions/energymix-server.tar /opt/

WORKDIR /opt/energymix-server
ENTRYPOINT ["/opt/energymix-server/bin/energymix-server"]

