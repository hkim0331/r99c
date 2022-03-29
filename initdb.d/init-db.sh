#!/bin/sh
# initdb.d for frozen-r99c

DIR="/docker-entrypoint-initdb.d"
PSQL="psql -U postgres"
${PSQL} -c "create database r99c owner='postgres'"
${PSQL} r99c < ${DIR}/r99c-2022-03-29.dump
${PSQL} -c "alter table r99c.users drop colum if exist sid;"
${PSQL} -c "alter table r99c.users drop colum if exist name;"
${PSQL} -c "alter database r99c set default_transaction_read_only = on;"
