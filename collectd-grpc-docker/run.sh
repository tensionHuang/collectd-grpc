#!/bin/bash

RUN_IMG=centos/collectd-grpc-run:latest

if [[ ! -z $(docker images -q $RUN_IMG) ]]; then
  docker rmi $RUN_IMG
fi
docker build -t $RUN_IMG .

docker-compose stop && docker-compose rm -f
docker-compose up -d
