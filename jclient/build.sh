#!/usr/bin/env bash
#
# File.   build.sh
# Date.   06/23/2026
# Description.
#         Build the multi-protocol Linux Performance Monitor Client
#

set -e

if [ -f "../monitor_client.ini" ]; then
    source "../monitor_client.ini"
fi

if [ -f "monitor_client.ini" ]; then
    source "monitor_client.ini"
fi

if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not set. Please check monitor_client.ini"
    exit 1
fi

export PATH=$JAVA_HOME/bin:$PATH

echo "Building pmon-client2-mp..."
mvn clean package

echo ""
echo "Build complete. JAR: target/monitor_client.jar"
echo "Run: ./monitor_client2.sh --help"
