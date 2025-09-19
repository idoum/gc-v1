#!/usr/bin/env bash
#
# @path start.sh
# @description Script pour démarrer MySQL et le backend en Docker-compose
docker-compose down
docker-compose up --build -d
