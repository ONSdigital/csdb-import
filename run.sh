#!/bin/bash
JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8005,server=y,suspend=n"

mvn package && \
java $JAVA_OPTS \
          -DPORT=8085 \
          -Drestolino.packageprefix=com.github.onsdigital.csdbimport.api \
          -jar target/csdb-import-0.0.1-SNAPSHOT-jar-with-dependencies.jar
