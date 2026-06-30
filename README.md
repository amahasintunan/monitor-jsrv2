# jperf-monitors

Java Linux performance monitors — the Java counterpart to [`perf-monitors`](https://github.com/amahasintunan/perf-monitors) (C++). Collects **CPU**, **Memory**, **Network**, and **Disk** metrics from `/proc` and `df`, served over four transport protocols selectable at runtime.

## Structure

```
jperf-monitors/
├── jserver/        Unified Java monitor server — UDP, TCP, HTTP, gRPC via -P flag
├── jclient-cli/    Unified Java command-line client — UDP, TCP, HTTP, gRPC via -P flag
└── jclient/        Java Swing GUI client — UDP, TCP, HTTP, gRPC with real-time graphs
```

All three components support all four transports. **Port**: 2019. **JSON style**: camelCase keys.

## Quick Start

### 1. Edit the `.ini` file

Each component reads `JAVA_HOME` from its `.ini` file — **never** from the shell environment. Edit it before building:

```bash
# jserver/jmonitor-server.ini         → JAVA_HOME=/path/to/jdk11+
# jclient-cli/jmonitor-client-cli.ini → JAVA_HOME=/path/to/jdk11+
# jclient/monitor_client.ini          → JAVA_HOME=/path/to/jdk17+
```

### 2. Build

```bash
cd jserver/      && ./build.sh   # → bin/jmonitor-server.jar
cd jclient-cli/  && ./build.sh   # → bin/jmonitor-client-cli.jar
cd jclient/      && ./build.sh   # → target/monitor_client.jar
```

### 3. Run the server

```bash
cd jserver/bin/
./monitor-server.sh -P udp -p 2019 -e    # UDP (default)
./monitor-server.sh -P tcp -p 2019 -e    # TCP
./monitor-server.sh -P http -p 2019 -e   # HTTP REST
./monitor-server.sh -P grpc -p 2019 -e   # gRPC
```

### 4. Run a client

**CLI client** (text output to stdout):
```bash
cd jclient-cli/bin/
./monitor-client-cli.sh -P udp -h localhost -p 2019
./monitor-client-cli.sh -P tcp -h localhost -p 2019 -d 5
./monitor-client-cli.sh -P http -h localhost -p 2019
./monitor-client-cli.sh -P grpc -h localhost -p 2019 -t 2000
```

**Swing GUI client** (real-time graphs):
```bash
cd jclient/bin/
./monitor-client.sh --host=localhost --port=2019 --protocol=udp
./monitor-client.sh --host=localhost --port=2019 --protocol=tcp
./monitor-client.sh --host=localhost --port=2019 --protocol=http
./monitor-client.sh --host=localhost --port=2019 --protocol=grpc
```

### Quick test without a client

```bash
echo -n x | nc -u -w1 localhost 2019    # UDP
echo -n x | nc localhost 2019            # TCP
curl http://localhost:2019/              # HTTP
curl http://localhost:2019/health         # HTTP health
```

## Metrics Collected

| Metric   | Source            | Fields                                                    |
|----------|-------------------|-----------------------------------------------------------|
| CPU      | `/proc/stat`      | Per-core and aggregate usage % (delta-based)              |
| Memory   | `/proc/meminfo`   | MemTotal, MemFree, MemAvailable (KiB)                     |
| Network  | `/proc/net/dev`   | Received/sent byte deltas across all interfaces (excl. lo)|
| Disk     | `df -T`           | Filesystem, type, blocks, used, available, use%, mount    |

## CLI Flags — Server

| Flag | Description |
|------|-------------|
| `-P, --Proto <proto>` | Transport: `udp`, `tcp`, `http`, `grpc` [default `udp`] |
| `-p, --port <port>`   | Port number [default 2019] |
| `-e, --echo`          | Echo JSON/protobuf to stdout |
| `-x, --exclude`       | Exclude tmpfs/devtmpfs from disk stats |
| `-?, --help`          | Print usage and exit |

## CLI Flags — CLI Client

| Flag | Description |
|------|-------------|
| `-P, --protocol <proto>` | Transport: `udp`, `tcp`, `http`, `grpc` [default `udp`] |
| `-h, --host <host>`      | Server host [default localhost] |
| `-p, --port <port>`      | Port [default 2019] |
| `-d, --delay <sec>`      | Poll interval in seconds [default 3] |
| `-t, --timeout <ms>`     | Socket/HTTP/gRPC timeout in ms [default 1000] |
| `-?, --help`             | Print usage and exit |

## Transport Comparison

| Aspect              | UDP                     | TCP                  | HTTP                       | gRPC                          |
|---------------------|-------------------------|----------------------|----------------------------|-------------------------------|
| Server API          | `DatagramSocket`        | `ServerSocket`       | `HttpServer`               | `io.grpc.Server`              |
| Client API (CLI)    | `DatagramSocket`        | `Socket`             | `HttpURLConnection`        | `ManagedChannel`              |
| Client API (GUI)    | `DatagramSocket`        | `Socket`             | `java.net.http.HttpClient` | `ManagedChannel`              |
| Request trigger     | Any 1-byte datagram     | Any data on socket   | `GET /`                    | `GetStats` RPC                |
| Serialization       | camelCase JSON          | camelCase JSON       | camelCase JSON             | protobuf                      |
| Health endpoint     | —                       | —                    | `GET /health`              | —                             |

## Architecture — Server

```
JMonitorServer.java (main)
├── parseArgs()              — commons-cli: -P, -p, -e, -x, -?
├── buildMonitorStats()      — Shared JSON builder (UDP, TCP, HTTP)
│   ├── CpuStat.getCpuMonitorInstance()
│   ├── MemoryStat.getMemoryStatInstance()
│   ├── NetworkStat.getNetworkStatInstance()
│   └── DiskStat.getDiskStatInstance()
├── runUdpServer()           — DatagramSocket recv/send loop
├── runTcpServer()           — ServerSocket accept/write/close loop
├── runHttpServer()          — HttpServer, / + /health endpoints
└── runGrpcServer()          — gRPC Server + MonitorServiceImpl
```

## Architecture — GUI Client

```
MonitorClient.java (JFrame + Runnable)
├── run()                    — Polling loop, dispatches by protocol
│   ├── runUdp()             — DatagramSocket
│   ├── runTcp()             — Socket
│   ├── runHttp()            — java.net.http.HttpClient
│   └── runGrpc()            — gRPC ManagedChannel + protobuf stub
├── parseAndUpdate()         — Shared JSON → UI pipeline (all transports feed here)
├── MonitorPanel             — Inner JPanel class, custom paintComponent() for graphs
├── Menu bar                 — File, Performance, Tools, Options
└── Supporting dialogs       — Setup, Disk Table, CPU Table, CPU Chart, Memory Pie Chart
```

All four protocols unify into `parseAndUpdate(JSONObject)` — gRPC responses are converted to JSON first, so the UI layer is transport-agnostic.

## Dependencies (Shared)

| Dependency              | Scope   | Used By              |
|-------------------------|---------|----------------------|
| commons-cli 1.4         | compile | Server + CLI client  |
| json-simple 1.1.1       | compile | Server (JSON)        |
| grpc-netty-shaded 1.62.2| runtime | All (gRPC transport) |
| grpc-protobuf 1.62.2    | compile | All (gRPC transport) |
| grpc-stub 1.62.2        | compile | All (gRPC transport) |
| protobuf-java 3.25.3    | compile | All (gRPC transport) |
| protobuf-java-util 3.25.3| compile| All (JsonFormat)     |
| javax.annotation-api    | compile | All (gRPC stubs)     |

## JSON Response Shape

```json
{
  "cpu": 25,
  "cpuList": {
    "entries": 4,
    "cpus": [{"cpu0": 10}, {"cpu1": 30}, {"cpu2": 15}, {"cpu3": 45}]
  },
  "memory": {
    "memTotal": 65855000,
    "memFree": 45000000,
    "memAvailable": 52000000
  },
  "network": {
    "rcvBytes": 12345678,
    "sndBytes": 87654321
  },
  "disk": [
    {
      "fileSystem": "/dev/sda1",
      "type": "ext4",
      "1k-blocks": 1044028,
      "used": 503048,
      "available": 469764,
      "use%": 52,
      "mountedOn": "/boot"
    }
  ]
}
```

## Conventions

- **`JAVA_HOME` is always read from a project `.ini` file**, never from the shell environment
- **JDK 11+** required (JDK 17+ recommended for the Swing GUI client)
- **No test framework** — verify by running the application
- **Port 2019** is the default for all transports
- Source files use the header: `/** @author anan.mahasintunan file: Xxx.java date: DD/MM/YYYY */`
