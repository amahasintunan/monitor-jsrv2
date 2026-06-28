# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository structure — git-per

This directory uses the **git-per** pattern. Each subdirectory is an independent git repository with its own `.git` — work on them independently, not as a monorepo.

```
jperf-monitors/
├── jserver/       Unified Java monitor server — UDP, TCP, HTTP, gRPC via -P flag
├── jclient-cli/   Unified Java command-line monitor client — UDP, TCP, HTTP, gRPC via -P flag
└── jclient/       Java Swing GUI monitor client — UDP, TCP, HTTP, gRPC via -P flag
```

## What this is

`jperf-monitors` is the Java counterpart to `perf-monitors` (the C++ family). It consolidates the server components from:
- `jmonitor2-udp` — UDP transport
- `jmonitor2-tcp` — TCP transport
- `jmonitor2-http` — HTTP REST transport
- `jmonitor2-grpc` — gRPC transport

All four transports are compiled into a single uber-jar and selected at runtime via the `-P` / `--Proto` command-line flag. The data-collector code (CPU, Memory, Network, Disk) is shared across all transports.

**Port**: 2019 (the jmonitor2 family default). **JSON style**: camelCase keys.

## Build & Run

```bash
# Build
cd jserver/
./build.sh                    # reads jmonitor-server.ini for JAVA_HOME, runs mvn clean package
# Output: bin/jmonitor-server.jar

# Run
cd jserver/bin/
./monitor-server.sh -P udp -p 2019 -e
./monitor-server.sh -P tcp -p 2019 -e
./monitor-server.sh -P http -p 2019 -e
./monitor-server.sh -P grpc -p 2019 -e
```

## CLI Flags

| Flag | Description |
|------|-------------|
| `-P, --Proto <proto>` | Transport protocol: `udp`, `tcp`, `http`, `grpc` [default `udp`] |
| `-p, --port <port>` | Port number [default 2019] |
| `-e, --echo` | Echo JSON/protobuf to stdout |
| `-x, --exclude` | Exclude tmpfs and devtmpfs from disk stats |
| `-?, --help` | Print usage and exit |

## Architecture

```
JMonitorServer.java (main)
├── parseArgs()              — commons-cli: -P, -p, -e, -x, -?
├── buildMonitorStats()      — Shared JSON builder (UDP, TCP, HTTP)
│   ├── CpuStat.getCpuMonitorInstance()
│   ├── MemoryStat.getMemoryStatInstance()
│   ├── NetworkStat.getNetworkStatInstance()
│   └── DiskStat.getDiskStatInstance()
├── runUdpServer()           — java.net.DatagramSocket recv/send loop
├── runTcpServer()           — java.net.ServerSocket accept/write/close loop
├── runHttpServer()          — com.sun.net.httpserver.HttpServer, / + /health
├── runGrpcServer()          — io.grpc.Server + MonitorServiceImpl (inner class)
└── Protocol.java            — Enum: UDP, TCP, HTTP, GRPC
```

### Data Collectors (shared, identical to jmonitor2-udp)

| Class | Source | Metrics |
|-------|--------|---------|
| `CpuStat` | `/proc/stat` | Per-CPU % usage (delta-based), aggregate CPU % |
| `MemoryStat` | `/proc/meminfo` | MemTotal, MemFree, MemAvailable (KiB) |
| `NetworkStat` | `/proc/net/dev` | Received/sent byte delta across all interfaces (excl. lo) |
| `DiskStat` | `df -T` | Filesystem, type, blocks, used, available, use%, mountpoint |

### Transport Differences

| Aspect | UDP | TCP | HTTP | gRPC |
|--------|-----|-----|------|------|
| Java API | `DatagramSocket` | `ServerSocket` | `HttpServer` | `io.grpc.Server` |
| Request trigger | Any 1-byte datagram | Any data on socket | `GET /` | `GetStats` RPC |
| Serialization | camelCase JSON | camelCase JSON | camelCase JSON | protobuf |
| Health endpoint | N/A | N/A | `GET /health` | N/A |

### JSON Response Shape (UDP, TCP, HTTP)

```json
{
  "cpu": 25,
  "cpuList": {"entries": 4, "cpus": [{"cpu0": 10}, {"cpu1": 30}, {"cpu2": 15}, {"cpu3": 45}]},
  "memory": {"memTotal": 65855000, "memFree": 45000000, "memAvailable": 52000000},
  "network": {"rcvBytes": 12345678, "sndBytes": 87654321},
  "disk": [{"fileSystem": "/dev/sda1", "type": "ext4", "1k-blocks": 1044028, "used": 503048, "available": 469764, "use%": 52, "mountedOn": "/boot"}]
}
```

## Dependencies

| Dependency | Scope | Used By |
|------------|-------|---------|
| commons-cli 1.4 | compile | All transports (CLI parsing) |
| json-simple 1.1.1 | compile | UDP, TCP, HTTP (JSON building) |
| grpc-netty-shaded 1.62.2 | runtime | gRPC |
| grpc-protobuf 1.62.2 | compile | gRPC |
| grpc-stub 1.62.2 | compile | gRPC |
| protobuf-java 3.25.3 | compile | gRPC |
| protobuf-java-util 3.25.3 | compile | gRPC (JsonFormat for echo) |
| javax.annotation-api 1.3.2 | compile | gRPC generated code |

## Conventions

- **JAVA_HOME is read from `jmonitor-server.ini`**, never from the shell environment
- **JDK 11+** required (gRPC deps need JDK 11+)
- **No test framework** — verify by running the server
- Source files use the header convention: `/** @author anan.mahasintunan file: Xxx.java date: DD/MM/YYYY */`
- Package: `com.jmonitor.server` (shared with client code)
- Proto package: `com.jmonitor.proto`
