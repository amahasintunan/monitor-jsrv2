/**
 * @author anan.mahasintunan
 * file: JMonitorClient.java
 * date: 12/03/2022
 * 28/06/2026: Consolidated client — UDP, TCP, HTTP, gRPC via -P / --protocol flag.
 */
package com.jmonitor.client;

import com.google.protobuf.util.JsonFormat;
import com.jmonitor.proto.MonitorServiceGrpc;
import com.jmonitor.proto.MonitorStatsRequest;
import com.jmonitor.proto.MonitorStatsResponse;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class JMonitorClient implements Runnable {
    private static final Logger logger = Logger.getLogger(JMonitorClient.class.getName());
    private static final String TITLE_NAME = "JMonitorClient";
    private static final int NUM_RETRIES = 3;
    private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer()
            .includingDefaultValueFields()
            .omittingInsignificantWhitespace();
    private Protocol protocol = Protocol.UDP;   // default protocol
    private String hostName = "localhost";       // default host name
    private int portNumber = 2019;               // default port number
    private int delay = 3;                       // default delay in seconds
    private int timeout = 1000;                  // default timeout in ms
    private Thread thread;
    private volatile boolean running = true;

    private void monitor() {
        thread = new Thread(this);
        thread.start();
    }

    // ---- Dispatch --------------------------------------------------------------

    public void run() {
        switch (protocol) {
            case UDP:
                runUdp();
                break;
            case TCP:
                runTcp();
                break;
            case HTTP:
                runHttp();
                break;
            case GRPC:
                runGrpc();
                break;
            default:
                logger.severe(TITLE_NAME + ": Unknown protocol: " + protocol);
                break;
        }
    }

    // ---- UDP transport ---------------------------------------------------------

    private void runUdp() {
        DatagramSocket socket = null;
        try {
            InetAddress address = InetAddress.getByName(hostName);
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
            while (running) {
                DatagramPacket request = new DatagramPacket(new byte[1], 1, address, portNumber);
                socket.send(request);
                byte[] buffer = new byte[65536]; // 64K buffer
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                boolean isReceived = false;
                for (int i = 1; i <= NUM_RETRIES; i++) {
                    try {
                        socket.receive(response);
                        isReceived = true;
                        if (response.getLength() >= buffer.length) {
                            logger.severe(TITLE_NAME + ": Buffer size too small package length=" + response.getLength());
                        }
                        break;
                    } catch (SocketTimeoutException ste) {
                        // receive call timed out, retry NUM_RETRIES
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        System.out.println(sdf.format(timestamp) + " " + TITLE_NAME
                                + ": socket.receive() timed out (" + i + ")");
                    }
                }
                if (isReceived) {
                    String responseJson = new String(buffer, 0, response.getLength());
                    DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    System.out.println(sdf.format(timestamp) + " " + TITLE_NAME + ": [receive " + hostName + ":"
                            + portNumber + "] " + responseJson);
                }
                Thread.sleep(delay * 1000L);
            }
        } catch (Exception ex) {
            logger.severe(TITLE_NAME + ": Exception " + ex.getMessage());
            running = false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    // ---- TCP transport ---------------------------------------------------------

    private void runTcp() {
        while (running) {
            Socket socket = null;
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(hostName, portNumber), timeout);
                socket.setSoTimeout(timeout);

                // Send trigger byte — required by C++ TCP server which blocks on
                // recv() before responding. Harmless for Java TCP server which
                // does not read from the socket before writing its response.
                OutputStream out = socket.getOutputStream();
                out.write(0);
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                String responseJson = in.readLine();
                if (responseJson != null) {
                    DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    System.out.println(sdf.format(timestamp) + " " + TITLE_NAME + ": [receive " + hostName + ":"
                            + portNumber + "] " + responseJson);
                }
            } catch (SocketTimeoutException ste) {
                DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                System.out.println(sdf.format(timestamp) + " " + TITLE_NAME
                        + ": socket timed out");
            } catch (Exception ex) {
                logger.severe(TITLE_NAME + ": Exception " + ex.getMessage());
                running = false;
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        logger.fine(TITLE_NAME + ": Error closing socket: " + e.getMessage());
                    }
                }
            }
            try {
                Thread.sleep(delay * 1000L);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    // ---- HTTP transport --------------------------------------------------------

    private void runHttp() {
        // /metrics works with both C++ HTTP server (exact match) and
        // Java HTTP server (root context "/" prefix-matches "/metrics").
        String monitorUrl = "http://" + hostName + ":" + portNumber + "/metrics";
        while (running) {
            boolean isReceived = false;
            for (int i = 1; i <= NUM_RETRIES; i++) {
                try {
                    URL url = new URL(monitorUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(timeout);
                    conn.setReadTimeout(timeout);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        StringBuilder response = new StringBuilder();
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                response.append(line);
                            }
                        }
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        System.out.println(sdf.format(timestamp) + " " + TITLE_NAME + ": [receive " + hostName + ":"
                                + portNumber + "] " + response.toString());
                        isReceived = true;
                    } else {
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        System.out.println(sdf.format(timestamp) + " " + TITLE_NAME
                                + ": HTTP error " + responseCode + " (" + i + ")");
                    }
                    conn.disconnect();
                    break;
                } catch (Exception ex) {
                    DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    System.out.println(sdf.format(timestamp) + " " + TITLE_NAME
                            + ": connection failed (" + i + ") " + ex.getMessage());
                }
            }
            if (!isReceived && !running) {
                break;
            }
            try {
                Thread.sleep(delay * 1000L);
            } catch (InterruptedException ie) {
                running = false;
                break;
            }
        }
    }

    // ---- gRPC transport --------------------------------------------------------

    private void runGrpc() {
        String target = hostName + ":" + portNumber;
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        MonitorServiceGrpc.MonitorServiceBlockingStub stub = MonitorServiceGrpc.newBlockingStub(channel);
        try {
            while (running) {
                for (int i = 1; i <= NUM_RETRIES; i++) {
                    try {
                        MonitorStatsResponse response = stub
                                .withDeadlineAfter(timeout, TimeUnit.MILLISECONDS)
                                .getStats(MonitorStatsRequest.getDefaultInstance());
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        System.out.println(sdf.format(timestamp) + " " + TITLE_NAME + ": [receive " + target + "] "
                                + JSON_PRINTER.print(response));
                        break;
                    } catch (StatusRuntimeException ex) {
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        System.out.println(sdf.format(timestamp) + " " + TITLE_NAME
                                + ": RPC failed (" + i + ") " + ex.getStatus());
                    } catch (Exception ex) {
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        System.out.println(sdf.format(timestamp) + " " + TITLE_NAME
                                + ": connection failed (" + i + ") " + ex.getMessage());
                    }
                }
                try {
                    Thread.sleep(delay * 1000L);
                } catch (InterruptedException ie) {
                    running = false;
                    break;
                }
            }
        } finally {
            try {
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ---- CLI parsing -----------------------------------------------------------

    private void parseArgs(String[] args) {
        Options options = new Options();
        options.addOption("P", "protocol", true, "Transport protocol: udp (default), tcp, http, grpc");
        options.addOption("h", "host", true, "Monitor server host [default localhost]");
        options.addOption("p", "port", true, "Monitor port [default 2019]");
        options.addOption("d", "delay", true, "Delay in second(s) [default 3]");
        options.addOption("t", "timeout", true, "Socket/HTTP/gRPC timeout in ms [default 1000]");
        options.addOption("?", "help", false, "Print this help and exit");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("?")) {
                usage();
                System.exit(0);
            }

            if (cmd.hasOption("P")) {
                String protoStr = cmd.getOptionValue("protocol");
                protocol = Protocol.fromString(protoStr);
                if (protocol == null) {
                    logger.severe(TITLE_NAME + ": Invalid protocol '" + protoStr
                            + "'. Valid values: udp, tcp, http, grpc");
                    usage();
                    System.exit(1);
                }
            }

            if (cmd.hasOption("h")) {
                hostName = cmd.getOptionValue("host");
            }

            if (cmd.hasOption("p")) {
                try {
                    portNumber = Integer.parseInt(cmd.getOptionValue("port"));
                } catch (NumberFormatException nfe) {
                    logger.warning(TITLE_NAME + ": Invalid port number specified " + cmd.getOptionValue("port"));
                    System.exit(1);
                }
            }

            if (cmd.hasOption("d")) {
                try {
                    delay = Integer.parseInt(cmd.getOptionValue("delay"));
                } catch (NumberFormatException nfe) {
                    logger.warning(TITLE_NAME + ": Invalid delay value specified " + cmd.getOptionValue("delay"));
                    System.exit(1);
                }
            }

            if (cmd.hasOption("t")) {
                try {
                    timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
                } catch (NumberFormatException nfe) {
                    logger.warning(TITLE_NAME + ": Invalid timeout value specified " + cmd.getOptionValue("timeout"));
                    System.exit(1);
                }
            }
        } catch (org.apache.commons.cli.ParseException e) {
            usage();
            System.exit(1);
        }
    }

    // ---- Usage -----------------------------------------------------------------

    private static void usage() {
        System.out.println("Java Linux Performance Monitor Client (JMonitorClient) - Unified (28/06/2026)");
        System.out.println();
        System.out.println("Usage: java " + TITLE_NAME + " [Options]");
        System.out.println("Options:");
        System.out.println("  -P, --protocol <proto>  Transport protocol: udp (default), tcp, http, grpc");
        System.out.println("  -h, --host <host>       Monitor server host [default localhost]");
        System.out.println("  -p, --port <port>       Monitor port [default 2019]");
        System.out.println("  -d, --delay <sec>       Delay in second(s) [default 3]");
        System.out.println("  -t, --timeout <ms>      Socket/HTTP/gRPC timeout in ms [default 1000]");
        System.out.println("  -?, --help              Print this help and exit");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java " + TITLE_NAME + " -P udp -h localhost -p 2019");
        System.out.println("  java " + TITLE_NAME + " -P tcp -h localhost -p 2019 -d 5");
        System.out.println("  java " + TITLE_NAME + " --protocol=http --host=localhost --port=2019");
        System.out.println("  java " + TITLE_NAME + " -P grpc -h localhost -p 2019 -t 2000");
    }

    // ---- Main ------------------------------------------------------------------

    public static void main(String[] args) {
        JMonitorClient monitorClient = new JMonitorClient();
        monitorClient.parseArgs(args);

        // Print banner
        String protoStr;
        switch (monitorClient.protocol) {
            case UDP:  protoStr = "UDP";  break;
            case TCP:  protoStr = "TCP";  break;
            case HTTP: protoStr = "HTTP"; break;
            case GRPC: protoStr = "gRPC"; break;
            default:   protoStr = "UDP";  break;
        }
        System.out.println("Java Linux Performance Monitor Client (JMonitorClient) - Unified (28/06/2026)");
        System.out.println("Protocol: " + protoStr + ", Host: " + monitorClient.hostName
                + ", Port: " + monitorClient.portNumber + ", Delay: " + monitorClient.delay + "s"
                + ", Timeout: " + monitorClient.timeout + "ms");

        monitorClient.monitor();
    }
}
