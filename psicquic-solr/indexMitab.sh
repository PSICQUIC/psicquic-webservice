#!/bin/sh

MITAB_FILE=$1;
MITAB_VERSION=$2;
SOLR_URL=$3;
mvn clean install -Pexec -Dmitab.file=${MITAB_FILE} -Dmitab.version=${MITAB_VERSION} -Dsolr.url=${SOLR_URL} -Dmaven.test.skip