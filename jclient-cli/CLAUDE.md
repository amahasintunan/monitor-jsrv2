# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`jclient-cli` is the consolidated Java command-line client for `jperf-monitors`. It combines four transport protocols into a single CLI application, analogous to how `jserver/` consolidates the server side.

```
jperf-monitors/
├── jserver/       Unified Java monitor server — UDP, TCP, HTTP, gRPC via -P flag
└── jclient-cli/   Unified Java command-line monitor client — UDP, TCP, HTTP, gRPC via -P flag
```

**Port**: 2019 (the jmonitor2 family default). **JSON style**: camelCase keys.

## Build & Run

```bash
# Build
cd jclient-cli/
./build.sh                    # reads jmonitor-client-cli.ini for JAVA_HOME, runs mvn clean package
# Output: bin/jmonitor-client-cli.jar

# Run
cd jclient-cli/bin/
./monitor-client-cli.sh -P udp -h localhost -p 2019
./monitor-client-cli.sh -P tcp -h localhost -p 2019 -d 5
./monitor-client-cli.sh -P http -h localhost -p 2019
./monitor-client-cli.sh -P grpc -h 192.168.1.100 -p 2019 -t 2000

# Or directly:
java -cp jmonitor-client-cli.jar -Xms128m -Xmx128m com.jmonitor.client.JMonitorClient [Options]
```

## CLI Flags

| Flag | Description |
|------|-------------|
| `-P, --protocol <proto>` | Transport protocol: `udp` (default), `tcp`, `http`, `grpc` |
| `-h, --host <host>` | Monitor server host [default localhost] |
| `-p, --port <port>` | Monitor port [default 2019] |
| `-d, --delay <sec>` | Delay in seconds between polls [default 3] |
| `-t, --timeout <ms>` | Socket/HTTP/gRPC timeout in milliseconds [default 1000] |
| `-?, --help` | Print usage and exit |

## Architecture

```
JMonitorClient.java (main)
├── parseArgs()              — commons-cli: -P, -h, -p, -d, -t, -?
├── run()                    — Dispatch to transport-specific method
│   ├── runUdp()             — DatagramSocket send/receive with 3 retries
│   ├── runTcp()             — Socket connect/read each loop iteration
│   ├── runHttp()            — HttpURLConnection GET with 3 retries
│   └── runGrpc()            — gRPC ManagedChannel + JsonFormat serialization
└── Protocol.java            — Enum: UDP, TCP, HTTP, GRPC
```

### Transport Differences

| Aspect | UDP | TCP | HTTP | gRPC |
|--------|-----|-----|------|------|
| Java API | `DatagramSocket` | `Socket` | `HttpURLConnection` | `ManagedChannel` |
| Retries | Yes (3), on timeout | No (reconnects each loop) | Yes (3), on connection failure | Yes (3), on StatusRuntimeException |
| Output format | Raw JSON | Raw JSON | Raw JSON | protobuf→JSON via JsonFormat |

## Dependencies

| Dependency | Scope | Used By |
|------------|-------|---------|
| commons-cli 1.4 | compile | All transports (CLI parsing) |
| grpc-netty-shaded 1.62.2 | runtime | gRPC |
| grpc-protobuf 1.62.2 | compile | gRPC |
| grpc-stub 1.62.2 | compile | gRPC |
| protobuf-java 3.25.3 | compile | gRPC |
| protobuf-java-util 3.25.3 | compile | gRPC (JsonFormat) |
| javax.annotation-api 1.3.2 | compile | gRPC generated code |

## Proto File

`src/main/proto/monitor.proto` — Identical to the one in `jserver/`. Defines `MonitorService` with `GetStats` RPC. Generated code goes to `com.jmonitor.proto` package.

## Conventions

- **JAVA_HOME is read from `jmonitor-client-cli.ini`**, never from the shell environment
- **JDK 11+** required (gRPC deps need JDK 11+)
- **No test framework** — verify by running against a running jserver instance
- Source files use the header convention: `/** @author anan.mahasintunan file: Xxx.java date: DD/MM/YYYY */`
- Package: `com.jmonitor.client`
- Proto package: `com.jmonitor.proto`
- Port: **2019** (jmonitor2 family default)

## Relationship to Other Projects

- **Server**: `jserver/` (same port 2019, same proto file, same four transports)
- **Original clients**: `jmonitor2-{udp,tcp,http,grpc}/client/` — single-protocol CLI clients that were consolidated here
- **Swing GUI client**: `jclient/` — Swing-based GUI with graphing, not CLI
