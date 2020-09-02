FROM docker.metasfresh.com/metasfresh/metasfresh-webui-api:${docker-tag.metasfresh.version}

COPY sources/configs/* /opt/metasfresh-webui-api/

# by default go with the start script baked into the base image
#COPY sources/start_webapi.sh /opt/metasfresh-webui-api/
#RUN chmod 700 /opt/metasfresh-webui-api/start_webapi.sh
#ENTRYPOINT ["/opt/metasfresh-webui-api/start_webapi.sh"]

EXPOSE 8080
