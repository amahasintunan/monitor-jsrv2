# pmon-client2-mp — Multi-Protocol Performance Monitor Client

A Java Swing desktop application for monitoring Linux system performance (CPU, Memory, Network, Disk). This client connects to a [pmon-cpp2](https://github.com/amahasintunan/pmon-cpp2) server and supports **four transport protocols** selectable via command-line.

## Supported Protocols

| Protocol | Default Port | Description |
|----------|-------------|-------------|
| **UDP** | 2019 | UDP datagram (original protocol) |
| **TCP** | 2019 | Raw TCP socket |
| **HTTP** | 2019 | HTTP GET request |
| **gRPC** | 2019 | gRPC with protobuf |

## Quick Start

```bash
# Build
./build.sh

# Run with UDP (default)
./monitor-client.sh --host=localhost --port=2019 --protocol=udp

# Run with TCP
./monitor-client.sh --host=localhost --port=2019 --protocol=tcp

# Run with HTTP
./monitor-client.sh --host=localhost --port=2019 --protocol=http

# Run with gRPC
./monitor-client.sh --host=localhost --port=2019 --protocol=grpc
```

## CLI Options

| Option | Description |
|--------|-------------|
| `-h`, `--host=HOST` | Server hostname (default: localhost) |
| `-p`, `--port=PORT` | Server port (default: protocol-dependent) |
| `-P`, `--protocol=PROTO` | Transport: udp, tcp, http, grpc (default: udp) |
| `-a`, `--path=PATH` | API path for HTTP (default: /) |
| `-t`, `--timeout=SEC` | Timeout in seconds |
| `-e`, `--echo` | Echo received data to stdout |
| `-?`, `--help` | Print help |

## Requirements

- JDK 17+ (JDK 21 recommended)
- Maven 3.6+
- Running pmon-cpp2 server (or compatible server)

## Build

```bash
./build.sh    # Linux/macOS
build.bat     # Windows
```

Output: `target/monitor_client.jar` (fat JAR with all dependencies).

## Project Structure

```
pmon-client2-mp/
├── src/main/java/         # Java source files
├── src/main/resources/    # PNG icons + monitor_bundle.properties
├── src/main/proto/        # gRPC protobuf definition
├── pom.xml                # Maven build
├── build.sh/build.bat     # Build scripts
├── monitor-client.sh    # Launcher (Linux/macOS)
├── monitor-client.bat   # Launcher (Windows)
└── monitor_client.ini   # JAVA_HOME configuration
```
