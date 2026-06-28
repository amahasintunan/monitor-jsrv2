# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

pmon-client2-mp is a **multi-protocol** Java Swing desktop client for monitoring Linux system performance (CPU, Memory, Network, Disk). It connects to a [pmon-cpp2](https://github.com/amahasintunan/pmon-cpp2) server and supports **four transport protocols**: UDP, TCP, HTTP, and gRPC — selectable via `--protocol` CLI flag.

## Build & Run

```bash
# Edit JAVA_HOME in pmon-client2-mp.ini first (JDK 17+ required, JDK 21 recommended)
./build.sh          # runs `mvn clean package` — outputs fat JAR at target/monitor_client.jar
./pmon-client2-mp.sh --host=localhost --port=2019 --protocol=udp
./pmon-client2-mp.sh --host=localhost --port=2019 --protocol=tcp
./pmon-client2-mp.sh --host=localhost --port=2019 --protocol=http
./pmon-client2-mp.sh --host=localhost --port=2019 --protocol=grpc
```

`build.sh` reads `JAVA_HOME` from `pmon-client2-mp.ini` (never the shell environment). Maven runs protobuf code generation, compiles, then shades a fat JAR. The launcher script similarly reads JAVA_HOME from the ini file and runs the JAR with `-Xms128m -Xmx128m` and Nimbus LAF.

## Architecture

### Transport layer — four protocols, one UI pipeline

`MonitorClient` is the single main class (`JFrame` + `Runnable`). Its `run()` method dispatches by protocol:

| Method | Transport | Key classes |
|--------|-----------|-------------|
| `runUdp()` | `java.net.DatagramSocket` | UDP datagrams, port 2019 default |
| `runTcp()` | `java.net.Socket` | Raw TCP bytes, port 2019 default |
| `runHttp()` | `java.net.http.HttpClient` | HTTP GET, port 2019 default |
| `runGrpc()` | gRPC `ManagedChannel` + protobuf stub | gRPC, port 2019 default, `monitor.proto` |

All four transport loops poll the server on a background thread, sleep according to `SpeedEnum` (1/3/5 seconds), and feed data into the UI on the EDT via `SwingUtilities.invokeLater`.

### Protocol unification strategy

UDP/TCP/HTTP responses are JSON strings parsed by `org.json.simple` into `JSONObject`. **gRPC responses are converted to JSONObject** inside `processGrpcResponse()` — this means all four protocols flow through the same `parseAndUpdate(JSONObject)` method. To add a new protocol, implement a `run*()` method that produces a `JSONObject` matching the expected field names (`cpu`, `memory`, `network`, `disk`, `cpuList`).

### UI structure

`MonitorClient` extends `JFrame` and contains:
- **`MonitorPanel`** — inner class extending `JPanel`, handles all custom painting (line graphs, scales, overlays) via `paintComponent()`. Renders whichever `PerformanceEnum` view is active (CPU/Memory/Network).
- **Menu bar** — File (Setup, Exit), Performance (CPU/Memory/Network radio), Tools (Disk Table, CPU Table, CPU Chart, Memory Chart), Options (Speed, Colors, Scale, Toolbar, Network Bandwidth, Graph Direction).
- **Toolbar** — quick-access buttons for Setup, CPU, Memory, Network, About.
- **Status bar** — `jLabelUsage` showing live metric text.

### Supporting dialogs

- `SetupDialog` — protocol-aware host/port config with per-protocol connectivity test (4 test methods, one per transport)
- `DiskSpaceDialog` — JTable of disk partitions from live JSON data
- `CpuListDialog` — JTable of per-CPU usage
- `CpuBarChartDialog` — bar chart of per-CPU usage
- `MemoryPieChartDialog` — pie chart of memory used/free/available
- `AboutDialog` — version/author info with clickable link (opens via browser path from `monitor_resource.xml`)

### Data classes and enums

- **Builder pattern** used for: `MonitorInfo`, `RgbColor`, `DiskStat`
- **Enums**: `Protocol` (UDP/TCP/HTTP/GRPC with default ports), `PerformanceEnum` (CPU/MEMORY/NETWORK/DISK), `SpeedEnum` (FASTER/NORMAL/SLOWER with millisecond delays), `NetworkScaleEnum` (2/8/32/128 MiB bandwidth), `GraphDirectionEnum` (LEFT_TO_RIGHT/RIGHT_TO_LEFT)
- **`MonitorConstants`** — KiB/MiB/GiB multipliers and warning color thresholds

### Window state persistence

On close, window position/size, active view, speed, colors, network scale, graph direction, and timeout are serialized via Gson to `~/.monitorinfo.json` (`MonitorInfo` class). On startup, this file is deserialized to restore state. A prompt asks before saving if settings changed.

### Internationalization

All user-facing strings live in `src/main/resources/monitor_bundle.properties` and are accessed via `MonitorBundle.get(key)` / `MonitorBundle.format(key, args...)`. Never hardcode English strings in the UI.

### gRPC / Protobuf

- Proto definition: `src/main/proto/monitor.proto` — generated Java package is `com.jmonitor.proto`
- Generated sources land in `target/generated-sources/protobuf/`
- The gRPC stub uses plaintext (`InsecureChannelCredentials`) — no TLS

## Conventions

- Source header: `File. / Date. / Author. / Description.` comment block at top of each `.java` file
- Flat package: all `.java` files in default package (no package declaration) — gRPC-generated code is the exception (`com.jmonitor.proto`)
- No test framework exists; verify changes by running the application
- `bin/` directory is the deployable artifact: contains the fat JAR + launcher scripts + `lib/` (runtime deps)
- The `.gitignore` excludes `bin/lib/` and `bin/monitor_client.jar` (built artifacts) but tracks launcher scripts in `bin/`
- Default port is **2019** for all protocols.
