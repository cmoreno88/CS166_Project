#! /bin/bash
folder=/tmp/$USER
PGDATA=$folder/myDB/data
PGSOCKETS=$folder/myDB/sockets
export PGPORT=9998

echo $folder

#Clear folder
rm -rf $folder/myDB

#Initialize folders
mkdir $folder/myDB
mkdir $folder/myDB/data
mkdir $folder/myDB/sockets
sleep 1
#cp ../data/*.csv $folder/myDB/data

#Initialize DB
initdb

sleep 1
#Start folder
pg_ctl -o "-c unix_socket_directories=$PGSOCKETS -p $PGPORT" -D $PGDATA -l $folder/logfile start

