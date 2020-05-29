#! /bin/bash
SUFFIX='_DB'
DBNAME=$USER$SUFFIX
PORT=9998

# Example: ./run.sh
java -cp lib/*:bin/ Ticketmaster $DBNAME $PORT $USER
