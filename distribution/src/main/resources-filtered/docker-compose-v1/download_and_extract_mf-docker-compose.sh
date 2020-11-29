#!/bin/bash

# Little QnD script that i use locally;
# it downloads a zipped-up docker-compose.yml with Dockerfiles etc, extracts them and builds them

set -e
set -u

docker_compose_v1_targz_url=$1

dirname=metasfresh-$(date +%s)
mkdir $dirname
cd $dirname
wget -O docker-compose-v1.tar.gz $docker_compose_v1_targz_url
tar -xvf docker-compose-v1.tar.gz

cd docker-compose-v1
# the variables don't really matter for the build, but we need to set them to avoid syntax problems
WEBUI_PORT=1 WEBAPI_PORT=2 APP_RESTAPI_PORT=3 docker-compose build

