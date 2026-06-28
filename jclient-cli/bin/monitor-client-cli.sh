#!/bin/bash
# File. jmonitor-client-cli.sh
# Date. 28/06/2026
# Description.
#       Client Side - Java Linux Performance Monitor CLI Client (Unified).
#       Supports UDP, TCP, HTTP, gRPC via -P flag.
#       Example:
#       ./jmonitor-client-cli.sh -P udp -h localhost -p 2019
#       ./jmonitor-client-cli.sh --protocol=tcp --host=192.168.1.100 --port=2019
#

cd "$(dirname "$0")"
INI_FILE="jmonitor-client-cli.ini"
while IFS='=' read -r f1 f2
do
   if [ "$f1" == "JAVA_HOME" ];
   then
      JAVA_HOME=$f2
      break
   fi
done <"$INI_FILE"

if [ -z "$JAVA_HOME" ] || [ "$JAVA_HOME" == "" ] ;
then
   echo "ERROR: JAVA_HOME is not set. Check $INI_FILE"
   exit
fi
if [ ! -d "$JAVA_HOME" ];
then
   echo "ERROR: \"$JAVA_HOME\" directory does not exist!"
   exit
fi

PATH=$JAVA_HOME/bin:$PATH
CLASSPATH=jmonitor-client-cli.jar

printf "%s\n$(java -fullversion)"
java -cp $CLASSPATH -Xms128m -Xmx128m com.jmonitor.client.JMonitorClient $@

if [ $? -eq 127 ]
then
  # exit code 127 - command not found
  echo "The specified JAVA path does not exist. Check the setting of JAVA_HOME in the script."
  exit 1
fi
