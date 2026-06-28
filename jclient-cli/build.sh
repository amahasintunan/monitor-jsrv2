#!/bin/bash

function exit_if_notfound() {
  if [ $? -eq 127 ]
  then
     # exit code 127 - command not found
     echo "The specified JAVA path does not exist. Check the setting of JAVA_HOME in the script."
     exit 1
  fi
}

OS_NAME=`uname`
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
printf "%s\n`java -fullversion`"

mvn clean package
if [ $? -ne 0 ] ; then
   echo "Maven build failed"
   exit 1
fi

echo "Build complete — output in bin/"
