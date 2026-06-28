/**
 * @author anan.mahasintunan
 * file: JMonitorServer.java
 * date: 12/03/2022
 * 06/27/2026: Unified server — UDP, TCP, HTTP, gRPC via -P / --Proto flag.
 */
package com.jmonitor.server;

import com.google.protobuf.util.JsonFormat;
import com.jmonitor.proto.CpuEntry;
import com.jmonitor.proto.CpuList;
import com.jmonitor.proto.Disk;
import com.jmonitor.proto.Memory;
import com.jmonitor.proto.MonitorServiceGrpc;
import com.jmonitor.proto.MonitorStatsRequest;
import com.jmonitor.proto.MonitorStatsResponse;
import com.jmonitor.proto.Network;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class JMonitorServer {
    private static final Logger logger = Logger.getLogger(JMonitorServer.class.getName());
    public static final String TITLE_NAME = "JMonitorServer";
    private int port = 2019; // default port number
    private boolean echo = false; // default echo off
    private boolean exclude = false;
    private Protocol protocol = Protocol.UDP; // default UDP for backward compatibility

    static boolean isLinuxOS() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    // ---- Shared JSON builder (used by UDP, TCP, HTTP) -----------------------

    private String buildMonitorStats() throws Exception {
        JSONObject json = new JSONObject();

        CpuStat cpuStat = CpuStat.getCpuMonitorInstance();
        cpuStat.getCpuStatDelta();
        json.put("cpu", cpuStat.getCpuValue());

        String cpuList = cpuStat.getCpuListAsJson();
        JSONParser parser = new JSONParser();
        JSONObject jsonCpuList = (JSONObject) parser.parse(cpuList);
        json.put("cpuList", jsonCpuList);

        MemoryStat memStat = MemoryStat.getMemoryStatInstance();
        memStat.getMemoryStat();
        MemoryStatObject mso = memStat.getMemoryStatObject();
        JSONObject jsonMemory = new JSONObject();
        jsonMemory.put("memTotal", mso.getMemTotal());
        jsonMemory.put("memFree", mso.getMemFree());
        jsonMemory.put("memAvailable", mso.getMemAvailable());
        json.put("memory", jsonMemory);

        NetworkStat networkStat = NetworkStat.getNetworkStatInstance();
        networkStat.getNetworkStat();
        NetworkStatObject nso = networkStat.getNetworkStatObject();
        JSONObject jsonNetwork = new JSONObject();
        jsonNetwork.put("rcvBytes", nso.getRcvByte());
        jsonNetwork.put("sndBytes", nso.getSndByte());
        json.put("network", jsonNetwork);

        DiskStat diskStat = DiskStat.getDiskStatInstance();
        List<DiskStatObject> list = diskStat.getListDiskStat(exclude);
        JSONArray jsonDisks = new JSONArray();
        for (DiskStatObject dso : list) {
            JSONObject jsonDisk = new JSONObject();
            jsonDisk.put("fileSystem", dso.fileSystem);
            jsonDisk.put("type", dso.type);
            jsonDisk.put("1k-blocks", dso.blocks);
            jsonDisk.put("used", dso.used);
            jsonDisk.put("available", dso.available);
            jsonDisk.put("use%", dso.useInPercent);
            jsonDisk.put("mountedOn", dso.mountedOn);
            jsonDisks.add(jsonDisk);
        }
        json.put("disk", jsonDisks);

        return json.toJSONString();
    }

    // ---- UDP server ----------------------------------------------------------

    private void runUdpServer() {
        System.out.println(TITLE_NAME + ": ***** welcome *****");
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
            System.out.println(TITLE_NAME + ": UDP server listening on port " + port);
            while (true) {
                DatagramPacket request = new DatagramPacket(new byte[1], 1);
                socket.receive(request);
                String monitorStats = buildMonitorStats();
                byte[] buffer = monitorStats.getBytes();
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                if (echo) {
                    DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    System.out.println(sdf.format(timestamp) + " " + TITLE_NAME + ": [send " + clientAddress.getHostName()
                            + ":" + clientPort + "] " + monitorStats);
                }
                DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
                socket.send(response);
            }
        } catch (IOException ioe) {
            logger.severe(TITLE_NAME + ": IOException " + ioe.getMessage());
        } catch (Exception ex) {
            logger.severe(TITLE_NAME + ": Exception " + ex.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    // ---- TCP server ----------------------------------------------------------

    private void runTcpServer() {
        System.out.println(TITLE_NAME + ": ***** welcome *****");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println(TITLE_NAME + ": TCP server listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                try {
                    String monitorStats = buildMonitorStats();
                    if (echo) {
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        System.out.println(sdf.format(timestamp) + " " + TITLE_NAME + ": [send "
                                + clientSocket.getInetAddress().getHostName()
                                + ":" + clientSocket.getPort() + "] " + monitorStats);
                    }
                    Writer out = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
                    out.write(monitorStats);
                    out.flush();
                } finally {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        logger.fine(TITLE_NAME + ": Error closing client socket: " + e.getMessage());
                    }
                }
            }
        } catch (IOException ioe) {
            logger.severe(TITLE_NAME + ": IOException " + ioe.getMessage());
        } catch (Exception ex) {
            logger.severe(TITLE_NAME + ": Exception " + ex.getMessage());
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.fine(TITLE_NAME + ": Error closing server socket: " + e.getMessage());
                }
            }
        }
    }

    // ---- HTTP server ---------------------------------------------------------

    private void runHttpServer() throws IOException {
        System.out.println(TITLE_NAME + ": ***** welcome *****");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleRequest);
        server.createContext("/health", this::handleHealth);
        server.setExecutor(null); // use default executor
        server.start();
        System.out.println(TITLE_NAME + ": HTTP server listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(TITLE_NAME + ": shutting down...");
            server.stop(2);
        }));
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        try {
            String monitorStats = buildMonitorStats();
            if (echo) {
                DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                System.out.println(sdf.format(timestamp) + " " + TITLE_NAME + ": [send "
                        + exchange.getRemoteAddress().getHostName() + "] " + monitorStats);
            }
            byte[] response = monitorStats.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        } catch (Exception ex) {
            logger.severe(TITLE_NAME + ": Exception " + ex.getMessage());
            String err = "{\"error\":\"" + ex.getMessage() + "\"}";
            byte[] errBytes = err.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, errBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errBytes);
            }
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        String health = "{\"status\":\"ok\"}";
        byte[] response = health.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    // ---- gRPC server ---------------------------------------------------------

    private void runGrpcServer() throws IOException, InterruptedException {
        System.out.println(TITLE_NAME + ": ***** welcome *****");
        Server server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new MonitorServiceImpl(echo, exclude))
                .build()
                .start();
        System.out.println(TITLE_NAME + ": gRPC server listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(TITLE_NAME + ": shutting down...");
            try {
                if (server != null) {
                    server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        server.awaitTermination();
    }

    // ---- gRPC service implementation -----------------------------------------

    static class MonitorServiceImpl extends MonitorServiceGrpc.MonitorServiceImplBase {
        private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer()
                .includingDefaultValueFields()
                .omittingInsignificantWhitespace();
        private final boolean echo;
        private final boolean defaultExclude;

        MonitorServiceImpl(boolean echo, boolean defaultExclude) {
            this.echo = echo;
            this.defaultExclude = defaultExclude;
        }

        @Override
        public void getStats(MonitorStatsRequest request, StreamObserver<MonitorStatsResponse> responseObserver) {
            try {
                boolean exclude = request.getExcludePseudoFs() || defaultExclude;
                MonitorStatsResponse response = buildResponse(exclude);
                if (echo) {
                    DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    System.out.println(sdf.format(timestamp) + " " + TITLE_NAME + ": [send] "
                            + JSON_PRINTER.print(response));
                }
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (Exception ex) {
                logger.severe(TITLE_NAME + ": Exception " + ex.getMessage());
                responseObserver.onError(Status.INTERNAL
                        .withDescription(ex.getMessage())
                        .withCause(ex)
                        .asRuntimeException());
            }
        }

        private MonitorStatsResponse buildResponse(boolean exclude) throws Exception {
            CpuStat cpuStat = CpuStat.getCpuMonitorInstance();
            cpuStat.getCpuStatDelta();

            CpuList.Builder cpuListBuilder = CpuList.newBuilder()
                    .setEntries(cpuStat.getCpuCount());
            CpuStatObject[] deltas = cpuStat.getCpuDeltaArray();
            for (int i = 0; i < cpuStat.getCpuCount(); i++) {
                cpuListBuilder.addCpus(CpuEntry.newBuilder()
                        .setId(deltas[i].getId())
                        .setUsagePercent(CpuStat.computeUsagePercent(deltas[i]))
                        .build());
            }

            MemoryStat memStat = MemoryStat.getMemoryStatInstance();
            memStat.getMemoryStat();
            MemoryStatObject mso = memStat.getMemoryStatObject();
            Memory memory = Memory.newBuilder()
                    .setMemTotal(mso.getMemTotal())
                    .setMemFree(mso.getMemFree())
                    .setMemAvailable(mso.getMemAvailable())
                    .build();

            NetworkStat networkStat = NetworkStat.getNetworkStatInstance();
            networkStat.getNetworkStat();
            NetworkStatObject nso = networkStat.getNetworkStatObject();
            Network network = Network.newBuilder()
                    .setRcvBytes(nso.getRcvByte())
                    .setSndBytes(nso.getSndByte())
                    .build();

            DiskStat diskStat = DiskStat.getDiskStatInstance();
            List<DiskStatObject> disks = diskStat.getListDiskStat(exclude);
            MonitorStatsResponse.Builder responseBuilder = MonitorStatsResponse.newBuilder()
                    .setCpu(cpuStat.getCpuValue())
                    .setCpuList(cpuListBuilder.build())
                    .setMemory(memory)
                    .setNetwork(network);
            for (DiskStatObject dso : disks) {
                responseBuilder.addDisk(Disk.newBuilder()
                        .setFileSystem(dso.fileSystem)
                        .setType(dso.type)
                        .setBlocks(dso.blocks)
                        .setUsed(dso.used)
                        .setAvailable(dso.available)
                        .setUsePercent(dso.useInPercent)
                        .setMountedOn(dso.mountedOn)
                        .build());
            }
            return responseBuilder.build();
        }
    }

    // ---- CLI parsing ---------------------------------------------------------

    private void parseArgs(String[] args) {
        Options options = new Options();
        options.addOption("P", "Proto", true, "Transport protocol: udp, tcp, http, grpc [default udp]");
        options.addOption("p", "port", true, "Monitor port [default 2019]");
        options.addOption("e", "echo", false, "Echo the data sent over the network [default off]");
        options.addOption("x", "exclude", false, "Exclude tmpfs and devtmpfs from disk stats");
        options.addOption("?", "help", false, "Print this help and exit");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("?")) {
                usage();
                System.exit(0);
            }

            if (cmd.hasOption("P")) {
                String protoStr = cmd.getOptionValue("Proto");
                protocol = Protocol.fromString(protoStr);
                if (protocol == null) {
                    logger.severe(TITLE_NAME + ": Invalid protocol '" + protoStr
                            + "'. Valid values: udp, tcp, http, grpc");
                    usage();
                    System.exit(1);
                }
            }

            if (cmd.hasOption("p")) {
                try {
                    port = Integer.parseInt(cmd.getOptionValue("port"));
                } catch (NumberFormatException e) {
                    logger.severe(TITLE_NAME + ": Invalid port number specified " + cmd.getOptionValue("port"));
                    System.exit(1);
                }
            }

            if (cmd.hasOption("e")) {
                echo = true;
            }

            if (cmd.hasOption("x")) {
                exclude = true;
            }
        } catch (Exception ex) {
            usage();
            System.exit(1);
        }
    }

    // ---- Usage ---------------------------------------------------------------

    private static void usage() {
        System.out.println("Linux Performance Monitor Server (JMonitorServer) - Unified (06/27/2026)");
        System.out.println();
        System.out.println("Usage: java " + TITLE_NAME + " [Options]");
        System.out.println("Options:");
        System.out.println("  -P, --Proto <proto>  Transport protocol: udp, tcp, http, grpc [default udp]");
        System.out.println("  -p, --port <port>    Monitor port [default 2019]");
        System.out.println("  -e, --echo           Echo the data sent over the network [default off]");
        System.out.println("  -x, --exclude        Exclude tmpfs and devtmpfs from disk stats");
        System.out.println("  -?, --help           Print this help and exit");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java " + TITLE_NAME + " -P udp -p 2019 -e");
        System.out.println("  java " + TITLE_NAME + " -P tcp -p 2019 -e -x");
        System.out.println("  java " + TITLE_NAME + " -P http -p 2019 -e");
        System.out.println("  java " + TITLE_NAME + " -P grpc -p 2019 -e");
        System.out.println("  java " + TITLE_NAME + " --Proto=tcp --port=2019 --echo");
    }

    // ---- Main ----------------------------------------------------------------

    public static void main(String[] args) {
        if (!isLinuxOS()) {
            logger.severe(TITLE_NAME + ": The server component must be run on a Linux platform.");
            System.exit(1);
        }

        JMonitorServer server = new JMonitorServer();
        server.parseArgs(args);

        // Print banner
        String protoStr;
        switch (server.protocol) {
            case UDP:  protoStr = "UDP";  break;
            case TCP:  protoStr = "TCP";  break;
            case HTTP: protoStr = "HTTP"; break;
            case GRPC: protoStr = "gRPC"; break;
            default:   protoStr = "UDP";  break;
        }
        System.out.println("Linux Performance Monitor Server (JMonitorServer) - Unified (06/27/2026)");
        System.out.println("Protocol: " + protoStr + ", Port: " + server.port);

        if (server.echo) {
            System.out.println(TITLE_NAME + ": is running using port " + server.port
                    + " with echo " + server.echo + " exclude " + server.exclude);
        }

        // Dispatch to transport
        try {
            switch (server.protocol) {
                case UDP:
                    server.runUdpServer();
                    break;
                case TCP:
                    server.runTcpServer();
                    break;
                case HTTP:
                    server.runHttpServer();
                    break;
                case GRPC:
                    server.runGrpcServer();
                    break;
                default:
                    logger.severe(TITLE_NAME + ": Unknown protocol: " + server.protocol);
                    System.exit(1);
            }
        } catch (Exception e) {
            logger.severe(TITLE_NAME + ": Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
