#!/bin/bash
#
# File. monitor_client2.sh
# Date. 06/23/2026
# Description.
#       Multi-Protocol Client for Linux Performance Monitor.
#       Supports UDP, TCP, HTTP, and gRPC transports.
#       Examples:
#         ./monitor_client2.sh --host=localhost --port=2019 --protocol=udp
#         ./monitor_client2.sh --host=localhost --port=2019 --protocol=tcp
#         ./monitor_client2.sh --host=localhost --port=2019 --protocol=http
#         ./monitor_client2.sh --host=localhost --port=2019 --protocol=grpc
#

cd "`dirname $0`"
INI_FILE="monitor_client.ini"
while IFS='=' read -r f1 f2
do
   if [ "$f1" == "JAVA_HOME" ] ;
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
if [ ! -d "$JAVA_HOME" ] ;
then
   echo "ERROR: \"$JAVA_HOME\" directory does not exist!"
   exit
fi

OS_NAME=`uname`
PATH=$JAVA_HOME/bin:$PATH
LOOK_AND_FEEL=-Dswing.defaultlaf=javax.swing.plaf.nimbus.NimbusLookAndFeel
printf  "%s\n`java -fullversion`"
JAR_FILE="monitor_client.jar"
if [ ! -f "$JAR_FILE" ] ; then
   JAR_FILE="target/$JAR_FILE"
fi
if [ ! -f "$JAR_FILE" ] ; then
   echo "ERROR: Cannot find monitor_client.jar (looked in ./ and target/)"
   echo "Run ./build.sh first to compile the project."
   exit 1
fi
CP="$JAR_FILE"
for jar in lib/*.jar; do
    CP="$CP:$jar"
done
java -Xms128m -Xmx128m $LOOK_AND_FEEL -cp "$CP" MonitorClient $@
if [ $? -eq 127 ]
then
  # exit code 127 - command not found
  echo "The specified JAVA path does not exist. Check the setting of JAVA_HOME in the script."
  exit 1
fi
