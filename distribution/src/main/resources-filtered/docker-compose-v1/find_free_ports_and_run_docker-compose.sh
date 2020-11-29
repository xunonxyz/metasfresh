
# Finds free ports for oth the webui and the webapi
# Thx to
# - https://www.linuxjournal.com/content/return-values-bash-functions
# - https://stackoverflow.com/a/45539101/1012103

BASE_PORT=16998
INCREMENT=1

function find_next_free_port()
{
  local port=$1
  local isfree=$(netstat -taln | grep $port)

  while [[ -n "$isfree" ]]; do
      port=$[port+INCREMENT]
      isfree=$(netstat -taln | grep $port)
  done
  echo $port
}

WEBAPI_PORT=$(find_next_free_port $BASE_PORT)
echo "WEBAPI_PORT=$WEBAPI_PORT"

WEBUI_PORT=$(find_next_free_port $[WEBAPI_PORT+INCREMENT])
echo "WEBUI_PORT=$WEBUI_PORT"

APP_RESTAPI_PORT=$(find_next_free_port $[WEBUI_PORT+INCREMENT])
echo "APP_RESTAPI_PORT=$APP_RESTAPI_PORT"

echo "Starting metasfresh stack"
WEBUI_PORT=$WEBUI_PORT WEBAPI_PORT=$WEBAPI_PORT APP_RESTAPI_PORT=$APP_RESTAPI_PORT docker-compose up -d

HOSTNAME=localhost

# wait for the web-api to be up
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' http://${HOSTNAME}:${APP_RESTAPI_PORT}/info)" != "200" ]]; do sleep 5; echo "Waiting for stack to be UP"; done

# run the cypress tests
docker run --ipc=host --rm\
 -e "FRONTEND_URL=http://${HOSTNAME}:${WEBUI_PORT}"\
 -e "API_URL=http://${HOSTNAME}:{APP_RESTAPI_PORT}/rest/api"\
 -e "WS_URL=http://${HOSTNAME}:${WEBUI_PORT}/stomp"\
 -e "USERNAME=dev"\
 -e "PASSWORD=password"\
 -e "CYPRESS_SPEC=NOT_SET"\
 -e "CYPRESS_RECORD_KEY=NOT_SET"\
 -e "CYPRESS_BROWSER=chrome"\
 -e "DEBUG_CYPRESS_OUTPUT=n"\
 -e "DEBUG_PRINT_BASH_CMDS=n"\
 -e "DEBUG_SLEEP_AFTER_FAIL=n"\
 nexus.metasfresh.com:6001/metasfresh/metasfresh-e2e:gh6205_2nd_docker_compose_5.160.2_88_gh62052nddockercompose
