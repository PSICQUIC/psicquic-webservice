#!/bin/sh

MITAB_FILE=$1;
SOLR_URL=$2;
mvn clean install -Pexec -Dmitab.file=${MITAB_FILE} -Dmitab.version=MITAB27 -Dsolr.url=${SOLR_URL} -Dmaven.test.skip