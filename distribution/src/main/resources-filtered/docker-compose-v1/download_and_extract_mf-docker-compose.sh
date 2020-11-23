#!/bin/bash

# Little QnD script that i use locally;
# it downloads a zipped-up docker-compose.yml with Dockerfiles etc, extracts them and builds them

set -e

docker_compose_v1_zip_url=$1

dirname=metasfresh-$(date +%s)
mkdir $dirname
cd $dirname
wget -O docker-compose-v1.zip $docker_compose_v1_zip_url
unzip docker-compose-v1.zip
cd docker-compose-v1
docker-compose build

