FROM docker.metasfresh.com/metasfresh/metasfresh-app:${docker-tag.metasfresh.version}

COPY sources/configs/* /opt/metasfresh/

# the docker image already has a proper start script
#COPY sources/start_app.sh /opt/metasfresh/
#RUN chmod 700 /opt/metasfresh/start_app.sh

# note that the entry-point is /opt/metasfresh/start_app.sh
