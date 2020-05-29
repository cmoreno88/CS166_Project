#!/bin/bash
SUFFIX='_DB'
DB_NAME=$USER$SUFFIX
PGPORT=9998
psql -h /tmp/$USER/myDB/sockets -p $PGPORT $DB_NAME
