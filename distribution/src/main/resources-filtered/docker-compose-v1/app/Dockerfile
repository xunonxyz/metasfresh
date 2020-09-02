FROM docker.metasfresh.com/metasfresh/metasfresh-app:${docker-tag.metasfresh.version}

COPY sources/configs/* /opt/metasfresh/
COPY sources/start_app.sh /opt/metasfresh/
RUN chmod 700 /opt/metasfresh/start_app.sh

#ENTRYPOINT ["/opt/metasfresh/start_app.sh"]
