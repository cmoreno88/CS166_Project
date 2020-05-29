#! /bin/bash
PGPORT=9998
PGDATA=/tmp/$USER/myDB/data
pg_ctl -o "-c unix_socket_directories=$PGSOCKETS -p $PGPORT" -D $PGDATA -l $folder/logfile stop
