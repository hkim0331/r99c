#!/bin/sh

#lein uberjar

if [ -z "$1" ]; then
	echo usage: $0 target/file.jar
	exit 1
fi

scp target/uberjar/r99c.jar app.melt:r99c/
ssh app.melt 'sudo systemctl restart r99c'
