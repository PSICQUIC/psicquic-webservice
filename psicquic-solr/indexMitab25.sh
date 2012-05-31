#!/bin/sh

MITAB_FILE=$1;
mvn clean install -Pexec -Dmitab.file=${MITAB_FILE} -Dmitab.version=MITAB25 -Dsolr.home.path=src/main/resources/solr-home -Dmaven.test.skip