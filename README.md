# jperf-monitors

Java Linux performance monitor — the Java counterpart to `perf-monitors` (C++). Supports **UDP**, **TCP**, **HTTP REST**, and **gRPC** transports via the `-P` / `--Proto` command-line flag.

## Structure

```
jperf-monitors/
├── jserver/     Unified Java monitor server (this repo)
└── (future: jclient/)
```

## Quick Start

```bash
# Build (reads JAVA_HOME from jmonitor-server.ini)
cd jserver/
./build.sh

# Run (default: UDP on port 2019)
cd jserver/bin/
./monitor-server.sh -P udp -p 2019 -e

# Use other transports
./monitor-server.sh -P tcp -p 2019 -e
./monitor-server.sh -P http -p 2019 -e
./monitor-server.sh -P grpc -p 2019 -e

# Test
echo -n x | nc -u -w1 localhost 2019    # UDP
echo -n x | nc localhost 2019            # TCP
curl http://localhost:2019/              # HTTP
curl http://localhost:2019/health         # HTTP health
```

## Metrics Collected

- **CPU**: Per-core and aggregate usage % from `/proc/stat`
- **Memory**: Total, free, available from `/proc/meminfo`
- **Network**: Received/sent byte deltas from `/proc/net/dev`
- **Disk**: Filesystem usage from `df -T`
