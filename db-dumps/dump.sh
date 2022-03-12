#!/bin/sh
pg_dump -h localhost -U postgres -W r99c > `date +%F.dump`

