# FREG Rawdata Converter
[![Build Status](https://drone.prod-bip-ci.ssb.no/api/badges/statisticsnorway/rawdata-converter-app-freg/status.svg)](https://drone.prod-bip-ci.ssb.no/statisticsnorway/rawdata-converter-app-freg)
[![Coverage](https://sonarqube.prod-bip-ci.ssb.no/api/project_badges/measure?project=no.ssb.rawdata.converter%3Arawdata-converter-app-freg&metric=coverage)](https://sonarqube.prod-bip-ci.ssb.no/dashboard?id=no.ssb.rawdata.converter%3Arawdata-converter-app-freg)
[![Quality Gate Status](https://sonarqube.prod-bip-ci.ssb.no/api/project_badges/measure?project=no.ssb.rawdata.converter%3Arawdata-converter-app-freg&metric=alert_status)](https://sonarqube.prod-bip-ci.ssb.no/dashboard?id=no.ssb.rawdata.converter%3Arawdata-converter-app-freg)

Convert folkeregister xml rawdata to [Parquet](https://en.wikipedia.org/wiki/Apache_Parquet).

It is recommended that you work with this repo via the [rawdata-converter-project](https://github.com/statisticsnorway/rawdata-converter-project).


## Development

### Running from within an IDEA

When running the application from your IDEA, make sure to specify the `-Dmicronaut.environments=local`
VM param in order for micronaut to load overridden config from `application-local.properties`.


## Make targets

You can use `make` to execute common tasks:
```
build-all                      Build all and create docker image (requires rawdata-converter-project)
build-all-mvn                  Build all from parent (requires rawdata-converter-project)
build-mvn                      Build the project and install to you local maven repo
build-docker                   Build the docker image
run-local                      Run the app locally (without docker)
release-dryrun                 Simulate a release in order to detect any issues
release                        Release a new version. Update POMs and tag the new version in git. Drone deploys upon tag detection.
```


## Deployment

The app checks the `DEPLOYMENT_ENV` variable to determine environment specific
configuration. Non-sensitive, environment specific configuration are defined
in the `application-${DEPLOYMENT_ENV}.properties` file that are bundled into
the app jar.

In addition, the following environment variables must be defined:
```
RAWDATA_CLIENT_POSTGRES_DRIVER_USER
RAWDATA_CLIENT_POSTGRES_DRIVER_PASSWORD
RAWDATA_CLIENT_POSTGRES_DRIVER_DATABASE
```
