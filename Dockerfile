from carboni/java-component

# Consul
WORKDIR /etc/consul.d
RUN echo '{"service": {"name": "csdb-import", "tags": ["blue"], "port": 8080, "check": {"script": "curl http://localhost:8080 >/dev/null 2>&1", "interval": "10s"}}}' > csdb-import.json

# Add the built artifact
WORKDIR /usr/src
ADD git_commit_id /usr/src/
ADD ./target/*-jar-with-dependencies.jar /usr/src/target/

# Update the entry point script
RUN mv /usr/entrypoint/container.sh /usr/src/
ENV PACKAGE_PREFIX=com.github.onsdigital.csdbimport.api
RUN echo "java -Xmx4094m \
          -Drestolino.packageprefix=$PACKAGE_PREFIX \
          -jar target/*-jar-with-dependencies.jar" >> container.sh
