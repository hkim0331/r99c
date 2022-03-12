#!/bin/sh
if [ -z "$1" ]; then
    echo usage: $0 yyyy-mm-dd.dumo
    exit 1
fi

PSQL="psql -h localhost -U postgres"
${PSQL} -c "drop database r99c"
${PSQL} -c "create database r99c owner='postgres'"
${PSQL} r99c < $1

