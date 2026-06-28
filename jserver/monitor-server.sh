#!/bin/bash
# File. jmonitor-server.sh
# Date. 06/30/2011
# Description.
#       Server Side - Linux Performance Monitor for CPU, Memory and Network.
#       Example:
#       java -cp $CLASSPATH -Xms128m -Xmx128m JMonitorServer
#

cd "$(dirname "$0")"
INI_FILE="jmonitor-server.ini"
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
CLASSPATH=jmonitor-server.jar

printf "%s\n$(java -fullversion)"
java -cp $CLASSPATH -Xms128m -Xmx128m com.jmonitor.server.JMonitorServer $@

if [ $? -eq 127 ]
then
  # exit code 127 - command not found
  echo "The specified JAVA path does not exist. Check the setting of JAVA_HOME in the script."
  exit 1
fi
