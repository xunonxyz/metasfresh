## Run it

The docker compose file uses variables for the ports it's supposed to run on:
* `WEBUI_PORT`: note that metasfresh's CORS-thingie probably expects this to be 3000 (on localhost)
* `WEBAPI_PORT`

See https://stackoverflow.com/questions/28989069/how-to-find-a-free-tcp-port on finding free ports

you can then run it with

```bash
WEBUI_PORT=3000 WEBAPI_PORT=8081 docker-compose up -d
```

or

```bash
export WEBUI_PORT=3000 
export WEBAPI_PORT=8081 

docker-compose up -d
```
 

