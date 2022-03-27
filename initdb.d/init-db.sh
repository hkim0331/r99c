#!/bin/sh
DIR="/docker-entrypoint-initdb.d"
PSQL="psql -U postgres"
${PSQL} -c "create database r99c owner='postgres'"
${PSQL} r99c < ${DIR}/r99c-2022-03-27.dump
${PSQL} -c "alter database r99c set default_transaction_read_only = on;"
