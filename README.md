# energymix-server
Provides a REST interface with electrical power generation data

Have a look at the [Swagger openapi](http://stofradar.nl:9001/swagger) page.

Application example config file (YAML):

~~~~
---
entsoe:
  url: "https://web-api.tp.entsoe.eu"
  timeout: 30
  apikey: "your-entsoe-key"
  area: "10YNL----------L"
  timezone: "Europe/Amsterdam"
  forecastOffset: 30
entsog:
  url: "https://transparency.entsog.eu"
  timeout: 60
eex:
  url: "https://gasandregistry.eex.com"
  timeout: 30
ice:
  url: "https://www.theice.com"
  timeout: 30
logging: !<default>
  level: "INFO"
  loggers: {}
  appenders:
  - type: file
    logFormat: "%d{yyyy-MM-dd} | %d{HH:mm:ss.SSS} | %-15.15thread | %5p | %-25.25logger{25} | %m%n"
    currentLogFilename: energymix-server.log
    archivedLogFilenamePattern: energymix-server.log.%d
    archivedFileCount: 10
  - type: console
    logFormat: "%d{yyyy-MM-dd} | %d{HH:mm:ss.SSS} | %-15.15thread | %5p | %-25.25logger{25} | %m%n"
    target: "STDOUT"
server: !<default>
  applicationConnectors:
  - type: http
    port: 9001
~~~~
