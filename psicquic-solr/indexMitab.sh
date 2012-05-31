#!/bin/sh

MITAB_FILE=$1;
MITAB_VERSION=$2;
mvn clean install -Pexec -Dmitab.file=${MITAB_FILE} -Dmitab.version=${MITAB_VERSION} -Dsolr.home.path=src/main/resources/solr-home -Dmaven.test.skip