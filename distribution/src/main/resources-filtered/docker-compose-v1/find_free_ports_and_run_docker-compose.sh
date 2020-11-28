
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

WEBUI_PORT=$WEBUI_PORT WEBAPI_PORT=$WEBAPI_PORT APP_RESTAPI_PORT=$APP_RESTAPI_PORT docker-compose up -d

# wait for the web-api to be up
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' http://localhost:8080/info)" != "200" ]]; do sleep 5; done
