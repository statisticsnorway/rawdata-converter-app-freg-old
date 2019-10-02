# FREG Rawdata Converter

Convert folkeregister xml rawdata to [Parquet](https://en.wikipedia.org/wiki/Apache_Parquet).

It is recommended that you work with this repo via the [rawdata-converter-project](https://github.com/statisticsnorway/rawdata-converter-project).


## Prerequisites

There are certain dependencies that you will need to have installed in your local
maven repo (since they haven't been released yet):

* [transform-to-parquet](https://github.com/statisticsnorway/transform-to-parquet)


## Make targets

You can use `make` to execute common tasks:
```
build-all                      Build all and create docker image (requires rawdata-converter-project)
build-all-mvn                  Build all from parent (requires rawdata-converter-project)
build-mvn                      Build the project and install to you local maven repo
build-docker                   Build the docker image
```

