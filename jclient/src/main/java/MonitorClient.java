/*
 * File.   MonitorClient.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Multi-Protocol Client - Linux Performance Monitor for CPU, Memory and Network.
 *         Supports UDP, TCP, HTTP, and gRPC transports via --protocol option.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jmonitor.proto.CpuEntry;
import com.jmonitor.proto.Disk;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.awt.EventQueue.invokeLater;

public class MonitorClient extends JFrame
        implements WindowListener, ComponentListener, ActionListener, MouseListener, Runnable {
    private static final Logger logger = Logger.getLogger(MonitorClient.class.getName());
    private static final long serialVersionUID = 1L;
    private static final String IMAGE_NAME = "monitor.png";
    static final String TITLE_NAME = MonitorBundle.get("app.title");
    public static final int DEFAULT_SOCKET_TIMEOUT_SEC = 5;
    public static final int MIN_SOCKET_TIMEOUT_SEC = 1;
    public static final int MAX_SOCKET_TIMEOUT_SEC = 60;
    public static final int BUFFER_SIZE = 64 * 1024;
    public static final int NUM_RETRIES = 3;
    public static final int SCALE_ROWS = 10;
    public static final int SCALE_COLUMNS = 15;
    private static final int X = 0;
    private static final int Y = 0;
    private static final int CX = 360;
    private static final int CY = 254;
    private static final int MIN_CX = 192;
    private static final int MIN_CY = 160;
    private static final String MONITOR_INFO = ".monitorinfo.json";
    private static SpeedEnum speed = SpeedEnum.NORMAL;
    private final Color DEFAULT_FGCOLOR = Color.white;
    private final Color DEFAULT_FGCOLOR2 = Color.green;
    private final Color DEFAULT_BGCOLOR = Color.black;
    private final Color DEFAULT_SCALECOLOR = Color.gray;
    private final Color ALTERNATE_FGCOLOR = Color.black;
    private final Color ALTERNATE_BGCOLOR = Color.white;
    private final Color ALTERNATE_SCALECOLOR = Color.gray;
    private Protocol protocol = Protocol.UDP;
    private String hostName = "localhost";
    private int portNumber = Protocol.UDP.getDefaultPort();
    private boolean echo = false;
    private int socketTimeoutSec = DEFAULT_SOCKET_TIMEOUT_SEC;
    // /metrics works with both C++ HTTP server (exact match) and
    // Java HTTP server (root context "/" prefix-matches "/metrics").
    private String apiPath = "/metrics";
    private int grpcTimeoutSeconds = 1;
    final int USAGE_INTERVAL = 60;
    final Color SCALE_COLOR = Color.gray;
    PerformanceEnum performance = PerformanceEnum.CPU;
    NetworkScaleEnum network_scale = NetworkScaleEnum.SCALE_2MB;
    GraphDirectionEnum graphDirection = GraphDirectionEnum.RIGHT_TO_LEFT;
    Color fgColor = DEFAULT_FGCOLOR;
    Color fgColor2 = DEFAULT_FGCOLOR2;
    Color bgColor = DEFAULT_BGCOLOR;
    Color scaleColor = DEFAULT_SCALECOLOR;
    private JPanel graphPanel;
    private int width = 0;
    private int height = 0;
    private JSONObject jsonMemory;
    private JSONObject jsonNetwork;
    private JSONArray jsonDisks;
    private JSONObject jsonCpuList;
    private JSONArray jsonCpus;
    int cpuValue = 0;
    long memTotal = 0;
    long memFree = 0;
    long memAvailable = 0;
    int memoryValue = 0;
    int memoryAvailableValue = 0;
    long rcvByte = 0;
    long sndByte = 0;
    int networkValue = 0;
    int networkSndValue = 0;
    int networkRcvValue = 0;
    private final int[] cpuUsage = new int[USAGE_INTERVAL];
    int[] xPointsCpu = new int[USAGE_INTERVAL];
    int[] yPointsCpu = new int[USAGE_INTERVAL];
    final int[] memoryUsage = new int[USAGE_INTERVAL];
    int[] xPointsMemory = new int[USAGE_INTERVAL];
    int[] yPointsMemory = new int[USAGE_INTERVAL];
    final int[] memoryAvailable = new int[USAGE_INTERVAL];
    int[] xPointsMemoryAvailable = new int[USAGE_INTERVAL];
    int[] yPointsMemoryAvailable = new int[USAGE_INTERVAL];
    final int[] networkUsage = new int[USAGE_INTERVAL];
    final int[] networkSndUsage = new int[USAGE_INTERVAL];
    final int[] networkRcvUsage = new int[USAGE_INTERVAL];
    int[] xPointsNetwork = new int[USAGE_INTERVAL];
    int[] yPointsNetwork = new int[USAGE_INTERVAL];
    int[] xPointsNetworkSnd = new int[USAGE_INTERVAL];
    int[] yPointsNetworkSnd = new int[USAGE_INTERVAL];
    int[] xPointsNetworkRcv = new int[USAGE_INTERVAL];
    int[] yPointsNetworkRcv = new int[USAGE_INTERVAL];
    double[] xPointsScale = null;
    double[] yPointsScale = null;
    boolean showScale = true;
    boolean showToolbar = true;
    boolean showCpuValue = true;
    boolean showMemoryInfo = true;
    boolean showNetworkDetail = true;
    private JMenuBar jMenuBar1;
    private JPopupMenu jPopupMenu1;
    private JMenu jMenuFile;
    private JMenuItem jMenuItemSetup;
    private JMenuItem jMenuItemExit;
    private JMenu jMenuPerformance;
    private JRadioButtonMenuItem jRadioButtonMenuItemCPU;
    private JRadioButtonMenuItem jRadioButtonMenuItemMemory;
    private JRadioButtonMenuItem jRadioButtonMenuItemNetwork;
    private JRadioButtonMenuItem jRadioButtonMenuItemCPU2;
    private JRadioButtonMenuItem jRadioButtonMenuItemMemory2;
    private JRadioButtonMenuItem jRadioButtonMenuItemNetwork2;
    private JMenu jMenuTools;
    private JMenuItem jMenuItemDiskSpace;
    private JMenuItem jMenuItemCpuList;
    private JMenuItem jMenuItemCpuChart;
    private JMenuItem jMenuItemMemoryChart;
    private JMenu jMenuOptions;
    private JMenu jMenuNetworkBandwidth;
    private JRadioButtonMenuItem jRadioButtonMenuItem128mb;
    private JRadioButtonMenuItem jRadioButtonMenuItem32mb;
    private JRadioButtonMenuItem jRadioButtonMenuItem8mb;
    private JRadioButtonMenuItem jRadioButtonMenuItem2mb;
    private JMenu jMenuSpeed;
    private JRadioButtonMenuItem jRadioButtonMenuItemFaster;
    private JRadioButtonMenuItem jRadioButtonMenuItemNormal;
    private JRadioButtonMenuItem jRadioButtonMenuItemSlower;
    private JCheckBoxMenuItem jCheckBoxMenuItemShowScale;
    private JCheckBoxMenuItem jCheckBoxMenuItemToolbar;
    private JCheckBoxMenuItem jCheckBoxMenuItemShowCpuValue;
    private JCheckBoxMenuItem jCheckBoxMenuItemShowMemoryInfo;
    private JCheckBoxMenuItem jCheckBoxMenuItemShowNetworkDetail;
    private JCheckBoxMenuItem jCheckBoxMenuItemLeftToRight;
    private JMenu jMenuColor;
    private JMenuItem jMenuItemForeground;
    private JMenuItem jMenuItemForeground2;
    private JMenuItem jMenuItemBackground;
    private JMenuItem jMenuItemScale;
    private JMenuItem jMenuItemDefault;
    private JMenuItem jMenuItemAlternate;
    private JMenu jMenuHelp;
    private JMenuItem jMenuItemAbout;
    private JMenuItem jMenuItemAbout2;
    private JLabel jLabelUsage;
    private Thread thread;
    private volatile boolean running = true;
    private MonitorInfo monitorInfo;
    private DiskSpaceDialog diskSpaceDialog = null;
    private CpuListDialog cpuListDialog = null;
    private CpuBarChartDialog cpuChartDialog = null;
    private MemoryPieChartDialog memoryChartDialog = null;
    private JToolBar jToolBar = new JToolBar();
    private JButton jButtonSetup = new JButton();
    private JButton jButtonCpu = new JButton();
    private JButton jButtonMemory = new JButton();
    private JButton jButtonNetwork = new JButton();
    private JButton jButtonAbout = new JButton();
    private JLabel jLabelHost = new JLabel();
    static final String GOOGLE_CMD = "/opt/google/chrome/chrome";
    static final String BROWSER_PATH = "browserPath";
    static final String GLOBAL_PROPS = "globalProps";
    private static final String MONITOR_RESOURCE = "monitor_resource.xml";

    private MonitorClient() {
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getApiPath() {
        return apiPath;
    }

    public int getGrpcTimeoutSeconds() {
        return grpcTimeoutSeconds;
    }

    public int getSocketTimeoutSec() {
        return socketTimeoutSec;
    }

    public Color getFgColor() {
        return fgColor;
    }

    public Color getFgColor2() {
        return fgColor2;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public Color getScaleColor() {
        return scaleColor;
    }

    public boolean isShowCpuValue() { return showCpuValue; }

    public boolean isShowMemoryInfo() { return showMemoryInfo; }

    public boolean isShowNetworkDetail() { return showNetworkDetail; }

    public void rerun(String host, int port) {
        if (thread != null) {
            running = false;
            try {
                thread.join();
                thread = new Thread(this);
                running = true;
                hostName = host;
                portNumber = port;
                thread.start();
            } catch (InterruptedException ex) {
                logger.severe(MonitorBundle.format("log.exception", TITLE_NAME, ex.getMessage()));
            }
        }
    }

    // -----------------------------------------------------------------
    // Protocol-specific transport in run()
    // -----------------------------------------------------------------

    public void run() {
        SwingUtilities.invokeLater(() ->
                jLabelHost.setText(hostName + ":" + portNumber + " (" + protocol + ")"));
        switch (protocol) {
            case UDP:  runUdp();  break;
            case TCP:  runTcp();  break;
            case HTTP: runHttp(); break;
            case GRPC: runGrpc(); break;
        }
    }

    // -- UDP transport -------------------------------------------------
    private void runUdp() {
        DatagramSocket socket = null;
        try {
            InetAddress address = InetAddress.getByName(hostName);
            socket = new DatagramSocket();
            socket.setSoTimeout(socketTimeoutSec * 1000);
            while (running) {
                DatagramPacket request = new DatagramPacket(new byte[1], 1, address, portNumber);
                socket.send(request);
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                boolean isReceived = false;
                for (int i = 1; i <= NUM_RETRIES; i++) {
                    try {
                        socket.receive(response);
                        isReceived = true;
                        if (response.getLength() >= buffer.length) {
                            logger.severe(MonitorBundle.format("log.buffer.too.small", TITLE_NAME, response.getLength()));
                        }
                        break;
                    } catch (SocketTimeoutException ste) {
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        logger.warning(sdf.format(timestamp) + " "
                                + MonitorBundle.format("log.socket.timeout", TITLE_NAME, hostName, portNumber, i));
                    }
                }
                processJsonResponse(isReceived, buffer, response.getLength());
                int delay = speed.getSpeed();
                Thread.sleep(delay);
            }
        } catch (IOException | InterruptedException ex) {
            logger.severe(MonitorBundle.format("log.exception", TITLE_NAME, ex.getMessage()));
            running = false;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    // -- TCP transport -------------------------------------------------
    private void runTcp() {
        try {
            while (running) {
                Socket socket = null;
                boolean isReceived = false;
                byte[] buffer = {};
                int length = 0;
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(hostName, portNumber), socketTimeoutSec * 1000);
                    socket.setSoTimeout(socketTimeoutSec * 1000);

                    // Send trigger byte — required by C++ TCP server which blocks on
                    // recv() before responding. Harmless for Java TCP server which
                    // does not read from the socket before writing its response.
                    OutputStream out = socket.getOutputStream();
                    out.write(0);
                    out.flush();

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    String responseJson = in.readLine();
                    isReceived = (responseJson != null && !responseJson.isEmpty());
                    if (isReceived) {
                        buffer = responseJson.getBytes("UTF-8");
                        length = buffer.length;
                    }
                } catch (SocketTimeoutException ste) {
                    DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    logger.warning(sdf.format(timestamp) + " "
                            + MonitorBundle.format("log.socket.timeout", TITLE_NAME, hostName, portNumber, 1));
                } catch (Exception ex) {
                    logger.severe(MonitorBundle.format("log.exception", TITLE_NAME, ex.getMessage()));
                } finally {
                    if (socket != null) {
                        try { socket.close(); } catch (Exception e) {
                            logger.fine(MonitorBundle.format("log.exception", TITLE_NAME, e.getMessage()));
                        }
                    }
                }
                processJsonResponse(isReceived, buffer, length);
                int delay = speed.getSpeed();
                Thread.sleep(delay);
            }
        } catch (InterruptedException ex) {
            logger.severe(MonitorBundle.format("log.exception", TITLE_NAME, ex.getMessage()));
            running = false;
        }
    }

    // -- HTTP transport ------------------------------------------------
    private void runHttp() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(socketTimeoutSec))
                .build();
        String uri = "http://" + hostName + ":" + portNumber + apiPath;
        while (running) {
            boolean isReceived = false;
            byte[] buffer = {};
            int length = 0;
            for (int i = 1; i <= NUM_RETRIES; i++) {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(uri))
                            .timeout(Duration.ofSeconds(socketTimeoutSec))
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request,
                            HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        String responseJson = response.body();
                        isReceived = true;
                        buffer = responseJson.getBytes("UTF-8");
                        length = buffer.length;
                        break;
                    } else {
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        logger.warning(sdf.format(timestamp) + " "
                                + MonitorBundle.format("log.http.status", TITLE_NAME, hostName, portNumber,
                                response.statusCode(), i));
                    }
                } catch (HttpTimeoutException e) {
                    DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    logger.warning(sdf.format(timestamp) + " "
                            + MonitorBundle.format("log.http.timeout", TITLE_NAME, hostName, portNumber, i));
                } catch (IOException e) {
                    DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    logger.warning(sdf.format(timestamp) + " "
                            + MonitorBundle.format("log.http.error", TITLE_NAME, hostName, portNumber,
                            e.getMessage(), i));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                    break;
                }
            }
            processJsonResponse(isReceived, buffer, length);
            try {
                int delay = speed.getSpeed();
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    // -- gRPC transport ------------------------------------------------
    private void runGrpc() {
        String target = hostName + ":" + portNumber;
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        MonitorServiceGrpc.MonitorServiceBlockingStub stub = MonitorServiceGrpc.newBlockingStub(channel);
        try {
            while (running) {
                boolean isReceived = false;
                MonitorStatsResponse response = null;
                for (int i = 1; i <= NUM_RETRIES; i++) {
                    try {
                        response = stub
                                .withDeadlineAfter(grpcTimeoutSeconds * 1000L, TimeUnit.MILLISECONDS)
                                .getStats(MonitorStatsRequest.getDefaultInstance());
                        isReceived = true;
                        break;
                    } catch (StatusRuntimeException e) {
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        logger.warning(sdf.format(timestamp) + " "
                                + MonitorBundle.format("log.grpc.status", TITLE_NAME, hostName, portNumber,
                                e.getStatus().getCode().name(), i));
                    } catch (Exception e) {
                        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        logger.warning(sdf.format(timestamp) + " "
                                + MonitorBundle.format("log.grpc.error", TITLE_NAME, hostName, portNumber,
                                e.getMessage(), i));
                    }
                }
                processGrpcResponse(isReceived, response);
                try {
                    int delay = speed.getSpeed();
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
        } finally {
            try {
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // -----------------------------------------------------------------
    // Shared response processing (JSON-based protocols)
    // -----------------------------------------------------------------

    private void processJsonResponse(boolean isReceived, byte[] buffer, int length) {
        jMenuItemDiskSpace.setEnabled(isReceived);
        jMenuItemCpuList.setEnabled(isReceived);
        jMenuItemCpuChart.setEnabled(isReceived);
        jMenuItemMemoryChart.setEnabled(isReceived);
        if (isReceived && length > 0) {
            String responseJson = null;
            try {
                responseJson = new String(buffer, 0, length, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.warning(MonitorBundle.format("log.malformed.response", TITLE_NAME, e.getMessage()));
                return;
            }
            if (echo) {
                DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                System.out.println(sdf.format(timestamp) + " "
                        + MonitorBundle.format("log.receive.echo", TITLE_NAME, hostName, portNumber, responseJson));
            }
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(responseJson);
                normalizeJsonKeys(jsonObject);
                parseAndUpdate(jsonObject);
            } catch (ParseException | RuntimeException pex) {
                logger.warning(MonitorBundle.format("log.malformed.response", TITLE_NAME, pex.getMessage()));
            }
        }
    }

    // -----------------------------------------------------------------
    // Shared response processing (gRPC — convert protobuf to JSON)
    // -----------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void processGrpcResponse(boolean isReceived, MonitorStatsResponse response) {
        jMenuItemDiskSpace.setEnabled(isReceived);
        jMenuItemCpuList.setEnabled(isReceived);
        jMenuItemCpuChart.setEnabled(isReceived);
        jMenuItemMemoryChart.setEnabled(isReceived);
        if (isReceived && response != null) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("cpu", (long) response.getCpu());

                // Memory
                JSONObject jMem = new JSONObject();
                jMem.put("memTotal", response.getMemory().getMemTotal());
                jMem.put("memFree", response.getMemory().getMemFree());
                jMem.put("memAvailable", response.getMemory().getMemAvailable());
                jsonObject.put("memory", jMem);

                // Network
                JSONObject jNet = new JSONObject();
                jNet.put("interface", response.getNetwork().getInterface());
                jNet.put("rcvBytes", response.getNetwork().getRcvBytes());
                jNet.put("sndBytes", response.getNetwork().getSndBytes());
                jsonObject.put("network", jNet);

                // Disks — use same keys as server-side JSON:
                //   fileSystem, type, 1k-blocks, used, available, use%, mountedOn
                JSONArray jDisks = new JSONArray();
                for (Disk d : response.getDiskList()) {
                    JSONObject jd = new JSONObject();
                    jd.put("fileSystem", d.getFileSystem());
                    jd.put("type", d.getType());
                    jd.put("1k-blocks", d.getBlocks());
                    jd.put("used", d.getUsed());
                    jd.put("available", d.getAvailable());
                    jd.put("use%", (long) d.getUsePercent());
                    jd.put("mountedOn", d.getMountedOn());
                    jDisks.add(jd);
                }
                jsonObject.put("disk", jDisks);

                // CPU list — build same shape as server-side getCpuListAsJson():
                //   {"entries": N, "cpus": [{"cpu": agg%}, {"cpu0": core0%}, {"cpu1": core1%}, …]}
                // Each array element is a single-key {id: percent} object.
                // The gRPC CpuList already includes the "cpu" aggregate as its first entry.
                if (response.hasCpuList()) {
                    JSONObject jCpuList = new JSONObject();
                    JSONArray jCpus = new JSONArray();
                    for (CpuEntry e : response.getCpuList().getCpusList()) {
                        JSONObject jc = new JSONObject();
                        jc.put(e.getId(), (long) e.getUsagePercent());
                        jCpus.add(jc);
                    }
                    jCpuList.put("entries", (long) jCpus.size());
                    jCpuList.put("cpus", jCpus);
                    jsonObject.put("cpuList", jCpuList);
                }

                parseAndUpdate(jsonObject);
            } catch (RuntimeException pex) {
                logger.warning(MonitorBundle.format("log.malformed.response", TITLE_NAME, pex.getMessage()));
            }
        }
    }

    // -----------------------------------------------------------------
    // -----------------------------------------------------------------
    // JSON normalization — convert snake_case (C++ TCP/HTTP server) to
    // camelCase so the rest of the UI code works with either server.
    // gRPC responses already arrive in camelCase, so this is a no-op.
    // -----------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void normalizeJsonKeys(JSONObject json) {
        // Top-level: cpu_list → cpuList
        renameKeyIfPresent(json, "cpu_list", "cpuList");

        // Memory: mem_total → memTotal, etc.
        JSONObject memory = (JSONObject) json.get("memory");
        if (memory != null) {
            renameKeyIfPresent(memory, "mem_total", "memTotal");
            renameKeyIfPresent(memory, "mem_free", "memFree");
            renameKeyIfPresent(memory, "mem_available", "memAvailable");
        }

        // Network: rcv_bytes → rcvBytes, etc.
        JSONObject network = (JSONObject) json.get("network");
        if (network != null) {
            renameKeyIfPresent(network, "rcv_bytes", "rcvBytes");
            renameKeyIfPresent(network, "snd_bytes", "sndBytes");
        }

        // Disk array: file_system → fileSystem, blocks → 1k-blocks, etc.
        JSONArray disks = (JSONArray) json.get("disk");
        if (disks != null) {
            for (Object o : disks) {
                JSONObject disk = (JSONObject) o;
                renameKeyIfPresent(disk, "file_system", "fileSystem");
                renameKeyIfPresent(disk, "mounted_on", "mountedOn");
                renameKeyIfPresent(disk, "use_percent", "use%");
                if (disk.containsKey("blocks") && !disk.containsKey("1k-blocks")) {
                    disk.put("1k-blocks", disk.remove("blocks"));
                }
            }
        }

        // CPU entries: {"id":"cpu","usage_percent":N} → {"cpu":N}
        JSONObject cpuList = (JSONObject) json.get("cpuList");
        if (cpuList != null) {
            JSONArray cpus = (JSONArray) cpuList.get("cpus");
            if (cpus != null && !cpus.isEmpty()) {
                JSONObject first = (JSONObject) cpus.get(0);
                if (first.containsKey("id") && first.containsKey("usage_percent")) {
                    JSONArray normalized = new JSONArray();
                    for (Object o : cpus) {
                        JSONObject entry = (JSONObject) o;
                        String id = (String) entry.get("id");
                        long percent = (long) entry.get("usage_percent");
                        JSONObject newEntry = new JSONObject();
                        newEntry.put(id, percent);
                        normalized.add(newEntry);
                    }
                    cpuList.put("cpus", normalized);
                }
            }
        }
    }

    private void renameKeyIfPresent(JSONObject obj, String oldKey, String newKey) {
        if (obj.containsKey(oldKey) && !obj.containsKey(newKey)) {
            obj.put(newKey, obj.remove(oldKey));
        }
    }

    // Common: extract fields from JSONObject, update dialogs & graphs
    // -----------------------------------------------------------------

    private void parseAndUpdate(JSONObject jsonObject) {
        cpuValue = Math.clamp((int) (long) jsonObject.get("cpu"), 0, 100);
        jsonMemory = (JSONObject) jsonObject.get("memory");
        jsonNetwork = (JSONObject) jsonObject.get("network");
        jsonDisks = (JSONArray) jsonObject.get("disk");
        jsonCpuList = (JSONObject) jsonObject.get("cpuList");
        jsonCpus = jsonCpuList != null ? (JSONArray) jsonCpuList.get("cpus") : null;
        if (diskSpaceDialog != null && diskSpaceDialog.isVisible() && jsonDisks != null) {
            diskSpaceDialog.populateTableFromJsonDisks(jsonDisks, hostName);
        }
        if (cpuListDialog != null && cpuListDialog.isVisible() && jsonCpus != null) {
            cpuListDialog.populateTableFromJsonCpus(jsonCpus, hostName);
        }
        if (cpuChartDialog != null && cpuChartDialog.isVisible() && jsonCpus != null) {
            cpuChartDialog.populateDataFromJsonCpus(jsonCpus, hostName);
        }
        if (memoryChartDialog != null && memoryChartDialog.isVisible() && jsonMemory != null) {
            memoryChartDialog.populateDataFromJsonMemory(jsonMemory, hostName);
        }
        revalidateMonitor();
        repaint();
    }

    // -----------------------------------------------------------------
    // Data access helpers (work uniformly on JSON fields)
    // -----------------------------------------------------------------

    JLabel getLabelUsage() {
        return jLabelUsage;
    }

    private void printListDiskStat(JSONArray jsonDisks) {
        for (int i = 0; i < jsonDisks.size(); i++) {
            JSONObject disk = (JSONObject) jsonDisks.get(i);
            String fileSystem = (String) disk.get("fileSystem");
            String type = (String) disk.get("type");
            String mountedOn = (String) disk.get("mountedOn");
            long blocks = (long) disk.get("1k-blocks");
            long used = (long) disk.get("used");
            long available = (long) disk.get("available");
            long useInPercent = (long) disk.get("use%");
            System.out.print(fileSystem + "\t");
            System.out.print(type + "\t");
            System.out.print(blocks + "\t");
            System.out.print(used + "\t");
            System.out.print(available + "\t");
            System.out.print(useInPercent + "%\t");
            System.out.print(mountedOn + "\n");
        }
    }

    private void getMemoryValues() {
        if (jsonMemory != null) {
            memTotal = (long) jsonMemory.get("memTotal");
            memFree = (long) jsonMemory.get("memFree");
            memAvailable = (long) jsonMemory.get("memAvailable");
        }
    }

    public String getNetworkInterface() {
        String networkInterface = null;
        if (jsonNetwork != null) {
            networkInterface = (String) jsonNetwork.get("interface");
        }
        return (networkInterface != null) ? networkInterface : "";
    }

    private int getNetworkValue() {
        int percentUsage = 0;
        if (jsonNetwork != null) {
            rcvByte = (long) jsonNetwork.get("rcvBytes");
            sndByte = (long) jsonNetwork.get("sndBytes");
            if (network_scale.getBandwidth() > 0) {
                percentUsage = (int) ((double) ((rcvByte + sndByte) * 100 / network_scale.getBandwidth()) + 0.5);
            }
        }
        return Math.clamp(percentUsage, 0, 100);
    }

    private int getNetworkSndValue() {
        int percentUsage = 0;
        if (jsonNetwork != null) {
            sndByte = (long) jsonNetwork.get("sndBytes");
            if (network_scale.getBandwidth() > 0) {
                percentUsage = (int) ((double) (sndByte * 100 / network_scale.getBandwidth()) + 0.5);
            }
        }
        return Math.clamp(percentUsage, 0, 100);
    }

    private int getNetworkRcvValue() {
        int percentUsage = 0;
        if (jsonNetwork != null) {
            rcvByte = (long) jsonNetwork.get("rcvBytes");
            if (network_scale.getBandwidth() > 0) {
                percentUsage = (int) ((double) (rcvByte * 100 / network_scale.getBandwidth()) + 0.5);
            }
        }
        return Math.clamp(percentUsage, 0, 100);
    }

    // -----------------------------------------------------------------
    // Graph coordinate helpers
    // -----------------------------------------------------------------

    private void getWidthHeight() {
        final int VGAP = 1;
        final int HGAP = 1;
        final int MENUBAR_CY = 20;
        final int TOOLBAR_CY = 32;

        width = getContentPane().getWidth() - HGAP;
        if (showToolbar) {
            height = getContentPane().getHeight() - VGAP - MENUBAR_CY - TOOLBAR_CY;
        } else {
            height = getContentPane().getHeight() - VGAP - MENUBAR_CY;
        }
    }

    private int xPointAt(int i) {
        if (graphDirection == GraphDirectionEnum.LEFT_TO_RIGHT && i == 0) {
            return width;
        }
        int idx = (graphDirection == GraphDirectionEnum.LEFT_TO_RIGHT) ? (USAGE_INTERVAL - 1 - i) : i;
        return (int) (idx * (double) width / USAGE_INTERVAL + 0.5);
    }

    private int xPointNewest() {
        return (graphDirection == GraphDirectionEnum.LEFT_TO_RIGHT) ? 0 : width;
    }

    private synchronized void remapGraphXPoints() {
        getWidthHeight();
        for (int i = 0; i < USAGE_INTERVAL - 1; i++) {
            xPointsCpu[i] = xPointAt(i);
            xPointsMemory[i] = xPointAt(i);
            xPointsMemoryAvailable[i] = xPointAt(i);
            xPointsNetwork[i] = xPointAt(i);
            xPointsNetworkSnd[i] = xPointAt(i);
            xPointsNetworkRcv[i] = xPointAt(i);
        }
        xPointsCpu[USAGE_INTERVAL - 1] = xPointNewest();
        xPointsMemory[USAGE_INTERVAL - 1] = xPointNewest();
        xPointsMemoryAvailable[USAGE_INTERVAL - 1] = xPointNewest();
        xPointsNetwork[USAGE_INTERVAL - 1] = xPointNewest();
        xPointsNetworkSnd[USAGE_INTERVAL - 1] = xPointNewest();
        xPointsNetworkRcv[USAGE_INTERVAL - 1] = xPointNewest();
    }

    // -----------------------------------------------------------------
    // Graph data initialization and updates
    // -----------------------------------------------------------------

    private void initCpuUsage() {
        getWidthHeight();
        for (int i = 0; i < USAGE_INTERVAL; i++) {
            cpuUsage[i] = 0;
            xPointsCpu[i] = xPointAt(i);
            yPointsCpu[i] = height;
        }
    }

    private synchronized void getCpuUsage() {
        getWidthHeight();
        for (int i = 0; i < USAGE_INTERVAL - 1; i++) {
            cpuUsage[i] = cpuUsage[i + 1];
            xPointsCpu[i] = xPointAt(i);
            yPointsCpu[i] = height - (int) ((double) cpuUsage[i] * height / 100.0 + 0.5);
        }
        if (cpuValue != -1) {
            cpuUsage[USAGE_INTERVAL - 1] = cpuValue;
            xPointsCpu[USAGE_INTERVAL - 1] = xPointNewest();
            yPointsCpu[USAGE_INTERVAL - 1] = height - (int) ((double) cpuValue * height / 100.0 + 0.5);
        }
    }

    private void initMemoryUsage() {
        getWidthHeight();
        for (int i = 0; i < USAGE_INTERVAL; i++) {
            memoryUsage[i] = 0;
            xPointsMemory[i] = xPointAt(i);
            yPointsMemory[i] = height;
            memoryAvailable[i] = 0;
            xPointsMemoryAvailable[i] = xPointAt(i);
            yPointsMemoryAvailable[i] = height;
        }
    }

    private void initNetworkUsage() {
        getWidthHeight();
        for (int i = 0; i < USAGE_INTERVAL; i++) {
            networkUsage[i] = 0;
            xPointsNetwork[i] = xPointAt(i);
            yPointsNetwork[i] = height;
            networkSndUsage[i] = 0;
            xPointsNetworkSnd[i] = xPointAt(i);
            yPointsNetworkSnd[i] = height;
            networkRcvUsage[i] = 0;
            xPointsNetworkRcv[i] = xPointAt(i);
            yPointsNetworkRcv[i] = height;
        }
    }

    private void setGraphScale() {
        getWidthHeight();
        double cellWidth = (double) width / SCALE_COLUMNS;
        double cellHeight = (double) height / SCALE_ROWS;
        xPointsScale = new double[SCALE_COLUMNS + 1];
        yPointsScale = new double[SCALE_ROWS + 1];
        for (int x = 0; x < xPointsScale.length; x++) {
            xPointsScale[x] = x * cellWidth;
        }
        for (int y = 0; y < yPointsScale.length; y++) {
            yPointsScale[y] = y * cellHeight;
        }
    }

    private synchronized void getMemoryUsage() {
        getWidthHeight();
        for (int i = 0; i < USAGE_INTERVAL - 1; i++) {
            memoryUsage[i] = memoryUsage[i + 1];
            xPointsMemory[i] = xPointAt(i);
            yPointsMemory[i] = height - (int) ((double) memoryUsage[i] * height / 100.0 + 0.5);
            memoryAvailable[i] = memoryAvailable[i + 1];
            xPointsMemoryAvailable[i] = xPointAt(i);
            yPointsMemoryAvailable[i] = height - (int) ((double) memoryAvailable[i] * height / 100.0 + 0.5);
        }
        getMemoryValues();
        int memPercentUsed = 0;
        int memPercentAvailable = 0;
        if (memTotal > 0) {
            memPercentUsed = (int) ((double) ((memTotal - memFree) * 100 / memTotal) + 0.5);
            memPercentUsed = Math.clamp(memPercentUsed, 0, 100);
            memPercentAvailable = (int) ((double) ((memAvailable) * 100 / memTotal) + 0.5);
            memPercentAvailable = Math.clamp(memPercentAvailable, 0, 100);
        }
        memoryValue = memPercentUsed;
        memoryUsage[USAGE_INTERVAL - 1] = memPercentUsed;
        xPointsMemory[USAGE_INTERVAL - 1] = xPointNewest();
        yPointsMemory[USAGE_INTERVAL - 1] = height - (int) ((double) memPercentUsed * height / 100.0 + 0.5);
        memoryAvailable[USAGE_INTERVAL - 1] = memPercentAvailable;
        xPointsMemoryAvailable[USAGE_INTERVAL - 1] = xPointNewest();
        yPointsMemoryAvailable[USAGE_INTERVAL - 1] = height - (int) ((double) memPercentAvailable * height / 100.0 + 0.5);
    }

    private synchronized void getNetworkUsage() {
        getWidthHeight();
        for (int i = 0; i < USAGE_INTERVAL - 1; i++) {
            networkUsage[i] = networkUsage[i + 1];
            xPointsNetwork[i] = xPointAt(i);
            yPointsNetwork[i] = height - (int) ((double) networkUsage[i] * height / 100.0 + 0.5);

            networkSndUsage[i] = networkSndUsage[i + 1];
            xPointsNetworkSnd[i] = xPointAt(i);
            yPointsNetworkSnd[i] = height - (int) ((double) networkSndUsage[i] * height / 100.0 + 0.5);

            networkRcvUsage[i] = networkRcvUsage[i + 1];
            xPointsNetworkRcv[i] = xPointAt(i);
            yPointsNetworkRcv[i] = height - (int) ((double) networkRcvUsage[i] * height / 100.0 + 0.5);
        }
        networkValue = getNetworkValue();
        networkUsage[USAGE_INTERVAL - 1] = networkValue;
        xPointsNetwork[USAGE_INTERVAL - 1] = xPointNewest();
        yPointsNetwork[USAGE_INTERVAL - 1] = height - (int) ((double) networkValue * height / 100.0 + 0.5);

        networkSndValue = getNetworkSndValue();
        networkSndUsage[USAGE_INTERVAL - 1] = networkSndValue;
        xPointsNetworkSnd[USAGE_INTERVAL - 1] = xPointNewest();
        yPointsNetworkSnd[USAGE_INTERVAL - 1] = height - (int) ((double) networkSndValue * height / 100.0 + 0.5);

        networkRcvValue = getNetworkRcvValue();
        networkRcvUsage[USAGE_INTERVAL - 1] = networkRcvValue;
        xPointsNetworkRcv[USAGE_INTERVAL - 1] = xPointNewest();
        yPointsNetworkRcv[USAGE_INTERVAL - 1] = height - (int) ((double) networkRcvValue * height / 100.0 + 0.5);
    }

    // -----------------------------------------------------------------
    // Window state persistence
    // -----------------------------------------------------------------

    private void saveMonitorInfoToJson() {
        Dimension dimension = getSize();
        Point point = getLocation();
        RgbColor fgRgbColor = new RgbColor.RgbColorBuilder()
                .color_r(fgColor.getRed()).color_g(fgColor.getGreen()).color_b(fgColor.getBlue()).build();
        RgbColor fgRgbColor2 = new RgbColor.RgbColorBuilder()
                .color_r(fgColor2.getRed()).color_g(fgColor2.getGreen()).color_b(fgColor2.getBlue()).build();
        RgbColor bgRgbColor = new RgbColor.RgbColorBuilder()
                .color_r(bgColor.getRed()).color_g(bgColor.getGreen()).color_b(bgColor.getBlue()).build();
        RgbColor scaleRgbColor = new RgbColor.RgbColorBuilder()
                .color_r(scaleColor.getRed()).color_g(scaleColor.getGreen()).color_b(scaleColor.getBlue()).build();
        MonitorInfo newMonitorInfo = new MonitorInfo.MonitorInfoBuilder().x(point.x).y(point.y).cx(dimension.width)
                .cy(dimension.height).speed(speed).performance(performance).networkScale(network_scale)
                .graphDirection(graphDirection)
                .fgColor(fgRgbColor).fgColor2(fgRgbColor2).bgColor(bgRgbColor).scaleColor(scaleRgbColor)
                .timeoutSeconds(grpcTimeoutSeconds).build();
        String oldInfo = monitorInfo.toString();
        String newInfo = newMonitorInfo.toString();
        if (!oldInfo.equals(newInfo)) {
            String mesg = MonitorBundle.get("save.prompt.message");
            Object[] options = {MonitorBundle.get("save.prompt.yes"), MonitorBundle.get("save.prompt.no")};
            int option = JOptionPane.showOptionDialog(this, mesg, TITLE_NAME, JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            if (option == JOptionPane.YES_OPTION) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(MONITOR_INFO))) {
                    gson.toJson(newMonitorInfo, bw);
                } catch (IOException ex) {
                    logger.warning(MonitorBundle.get("log.save.failed"));
                }
            }
        }
    }

    private boolean loadMonitorInfoFromJson() {
        boolean result = false;
        try (BufferedReader br = new BufferedReader(new FileReader(MONITOR_INFO))) {
            Gson gson = new Gson();
            monitorInfo = gson.fromJson(br, MonitorInfo.class);
            result = true;
        } catch (IOException ex) {
            logger.warning(MonitorBundle.get("log.load.failed"));
        } finally {
            if (monitorInfo == null) {
                RgbColor fgRgbColor = new RgbColor.RgbColorBuilder()
                        .color_r(DEFAULT_FGCOLOR.getRed()).color_g(DEFAULT_FGCOLOR.getGreen()).color_b(DEFAULT_FGCOLOR.getBlue()).build();
                RgbColor fgRgbColor2 = new RgbColor.RgbColorBuilder()
                        .color_r(DEFAULT_FGCOLOR2.getRed()).color_g(DEFAULT_FGCOLOR2.getGreen()).color_b(DEFAULT_FGCOLOR2.getBlue()).build();
                RgbColor bgRgbColor = new RgbColor.RgbColorBuilder()
                        .color_r(DEFAULT_BGCOLOR.getRed()).color_g(DEFAULT_BGCOLOR.getGreen()).color_b(DEFAULT_BGCOLOR.getBlue()).build();
                RgbColor scaleRgbColor = new RgbColor.RgbColorBuilder()
                        .color_r(DEFAULT_SCALECOLOR.getRed()).color_g(DEFAULT_SCALECOLOR.getGreen()).color_b(DEFAULT_SCALECOLOR.getBlue()).build();
                monitorInfo = new MonitorInfo.MonitorInfoBuilder().x(X).y(Y).cx(CX).cy(CY)
                        .performance(PerformanceEnum.CPU).speed(SpeedEnum.NORMAL)
                        .networkScale(NetworkScaleEnum.SCALE_8MB).graphDirection(GraphDirectionEnum.RIGHT_TO_LEFT)
                        .fgColor(fgRgbColor).fgColor2(fgRgbColor2).bgColor(bgRgbColor)
                        .scaleColor(scaleRgbColor).timeoutSeconds(1).build();
            }
        }
        return result;
    }

    // -----------------------------------------------------------------
    // UI initialization
    // -----------------------------------------------------------------

    private void initialize() {
        initWindowState();
        initMainPanel();
        initToolbar();
        initMenuBar();
        initPopupMenu();
        initStatusBar();
        wireActionListeners();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initWindowState() {
        getContentPane().setLayout(new BorderLayout());
        boolean result = loadMonitorInfoFromJson();
        if (!result) {
            setSize(new Dimension(CX, CY));
            setMinimumSize(new Dimension(MIN_CX, MIN_CY));
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = getSize();
            if (frameSize.height > screenSize.height) {
                frameSize.height = screenSize.height;
            }
            if (frameSize.width > screenSize.width) {
                frameSize.width = screenSize.width;
            }
            setLocation(screenSize.width - frameSize.width, 0);
        } else {
            speed = monitorInfo.getSpeed();
            performance = monitorInfo.getPerformance();
            network_scale = monitorInfo.getNetworkScale();
            if (monitorInfo.getGraphDirection() != null) {
                graphDirection = monitorInfo.getGraphDirection();
            }
            RgbColor fgRgbColor = monitorInfo.getFgColor();
            RgbColor fgRgbColor2 = monitorInfo.getFgColor2();
            RgbColor bgRgbColor = monitorInfo.getBgColor();
            RgbColor scaleRgbColor = monitorInfo.getScaleColor();
            fgColor = new Color(fgRgbColor.getColorR(), fgRgbColor.getColorG(), fgRgbColor.getColorB());
            fgColor2 = new Color(fgRgbColor2.getColorR(), fgRgbColor2.getColorG(), fgRgbColor2.getColorB());
            bgColor = new Color(bgRgbColor.getColorR(), bgRgbColor.getColorG(), bgRgbColor.getColorB());
            scaleColor = new Color(scaleRgbColor.getColorR(), scaleRgbColor.getColorG(), scaleRgbColor.getColorB());
            grpcTimeoutSeconds = monitorInfo.getTimeoutSeconds() > 0
                    ? monitorInfo.getTimeoutSeconds() : grpcTimeoutSeconds;
            setSize(new Dimension(monitorInfo.getCX(), monitorInfo.getCY()));
            setLocation(monitorInfo.getX(), monitorInfo.getY());
        }
        setTitle(TITLE_NAME);
        Image imageApp = Toolkit.getDefaultToolkit().getImage(MonitorClient.class.getResource(IMAGE_NAME));
        setIconImage(imageApp);
    }

    private void initMainPanel() {
        graphPanel = new MonitorPanel(this);
        addWindowListener(this);
        addComponentListener(this);
        getContentPane().add(graphPanel, BorderLayout.CENTER);
    }

    private void initToolbar() {
        jButtonSetup.setIcon(new ImageIcon(getClass().getResource("setup.png")));
        jButtonSetup.setToolTipText(MonitorBundle.get("tooltip.setup"));
        jButtonCpu.setIcon(new ImageIcon(getClass().getResource("cpu.png")));
        jButtonCpu.setToolTipText(MonitorBundle.get("tooltip.cpu"));
        jButtonMemory.setIcon(new ImageIcon(getClass().getResource("memory.png")));
        jButtonMemory.setToolTipText(MonitorBundle.get("tooltip.memory"));
        jButtonNetwork.setIcon(new ImageIcon(getClass().getResource("network.png")));
        jButtonNetwork.setToolTipText(MonitorBundle.get("tooltip.network"));
        jButtonAbout.setIcon(new ImageIcon(getClass().getResource("about-monitor.png")));
        jButtonAbout.setToolTipText(MonitorBundle.get("tooltip.about"));
        jToolBar.add(jButtonSetup);
        jToolBar.addSeparator();
        jToolBar.add(jButtonCpu);
        jToolBar.add(jButtonMemory);
        jToolBar.add(jButtonNetwork);
        jToolBar.addSeparator();
        jToolBar.add(jButtonAbout);
        jToolBar.addSeparator();
        jLabelHost.setText(hostName + ":" + portNumber + " (" + protocol + ")");
        jToolBar.add(jLabelHost);
        jToolBar.setFloatable(false);
        jToolBar.setMinimumSize(new java.awt.Dimension(332, 32));
        jToolBar.setPreferredSize(new java.awt.Dimension(332, 32));
        jToolBar.setBackground(Color.lightGray);
        jToolBar.setVisible(showToolbar);
        add(jToolBar, BorderLayout.NORTH);
    }

    private void initMenuBar() {
        jMenuBar1 = new JMenuBar();
        jMenuFile = new JMenu(MonitorBundle.get("menu.file"));
        jMenuFile.setMnemonic('F');
        jMenuItemSetup = new JMenuItem(MonitorBundle.get("menu.file.setup"));
        jMenuItemSetup.setMnemonic('s');
        jMenuFile.add(jMenuItemSetup);
        jMenuFile.addSeparator();
        jMenuItemExit = new JMenuItem(MonitorBundle.get("menu.file.exit"));
        jMenuItemExit.setMnemonic('x');
        jMenuFile.add(jMenuItemExit);
        jMenuPerformance = new JMenu(MonitorBundle.get("menu.performance"));
        jMenuPerformance.setMnemonic('P');
        jRadioButtonMenuItemCPU = new JRadioButtonMenuItem(MonitorBundle.get("menu.performance.cpu"));
        jRadioButtonMenuItemCPU.setMnemonic('C');
        jRadioButtonMenuItemCPU.setSelected(performance == PerformanceEnum.CPU);
        jRadioButtonMenuItemMemory = new JRadioButtonMenuItem(MonitorBundle.get("menu.performance.memory"));
        jRadioButtonMenuItemMemory.setMnemonic('M');
        jRadioButtonMenuItemMemory.setSelected(performance == PerformanceEnum.MEMORY);
        jRadioButtonMenuItemNetwork = new JRadioButtonMenuItem(MonitorBundle.get("menu.performance.network"));
        jRadioButtonMenuItemNetwork.setMnemonic('N');
        jRadioButtonMenuItemNetwork.setSelected(performance == PerformanceEnum.NETWORK);
        jMenuPerformance.add(jRadioButtonMenuItemCPU);
        jMenuPerformance.add(jRadioButtonMenuItemMemory);
        jMenuPerformance.add(jRadioButtonMenuItemNetwork);
        jMenuTools = new JMenu(MonitorBundle.get("menu.tools"));
        jMenuTools.setMnemonic('T');
        jMenuItemDiskSpace = new JMenuItem(MonitorBundle.get("menu.tools.disk"));
        jMenuItemDiskSpace.setMnemonic('D');
        jMenuItemDiskSpace.setEnabled(false);
        jMenuItemCpuList = new JMenuItem(MonitorBundle.get("menu.tools.cpulist"));
        jMenuItemCpuList.setMnemonic('L');
        jMenuItemCpuList.setEnabled(false);
        jMenuItemCpuChart = new JMenuItem(MonitorBundle.get("menu.tools.cpuchart"));
        jMenuItemCpuChart.setMnemonic('C');
        jMenuItemCpuChart.setEnabled(false);
        jMenuItemMemoryChart = new JMenuItem(MonitorBundle.get("menu.tools.memorychart"));
        jMenuItemMemoryChart.setMnemonic('M');
        jMenuItemMemoryChart.setEnabled(false);
        jMenuOptions = new JMenu(MonitorBundle.get("menu.options"));
        jMenuOptions.setMnemonic('O');
        jMenuSpeed = new JMenu(MonitorBundle.get("menu.options.speed"));
        jMenuSpeed.setMnemonic('S');
        jRadioButtonMenuItemFaster = new JRadioButtonMenuItem(SpeedEnum.FASTER.getDescription());
        jRadioButtonMenuItemFaster.setMnemonic('F');
        jRadioButtonMenuItemFaster.setSelected(speed == SpeedEnum.FASTER);
        jRadioButtonMenuItemNormal = new JRadioButtonMenuItem(SpeedEnum.NORMAL.getDescription());
        jRadioButtonMenuItemNormal.setMnemonic('N');
        jRadioButtonMenuItemNormal.setSelected(speed == SpeedEnum.NORMAL);
        jRadioButtonMenuItemSlower = new JRadioButtonMenuItem(SpeedEnum.SLOWER.getDescription());
        jRadioButtonMenuItemSlower.setMnemonic('S');
        jRadioButtonMenuItemSlower.setSelected(speed == SpeedEnum.SLOWER);
        jMenuSpeed.add(jRadioButtonMenuItemFaster);
        jMenuSpeed.add(jRadioButtonMenuItemNormal);
        jMenuSpeed.add(jRadioButtonMenuItemSlower);
        jMenuColor = new JMenu(MonitorBundle.get("menu.options.color"));
        jMenuColor.setMnemonic('C');
        jMenuItemForeground = new JMenuItem(MonitorBundle.get("menu.options.color.foreground"));
        jMenuItemForeground.setMnemonic('F');
        jMenuItemForeground2 = new JMenuItem(MonitorBundle.get("menu.options.color.foreground2"));
        jMenuItemForeground2.setMnemonic('g');
        jMenuItemBackground = new JMenuItem(MonitorBundle.get("menu.options.color.background"));
        jMenuItemBackground.setMnemonic('B');
        jMenuItemScale = new JMenuItem(MonitorBundle.get("menu.options.color.scale"));
        jMenuItemScale.setMnemonic('S');
        jMenuItemDefault = new JMenuItem(MonitorBundle.get("menu.options.color.default"));
        jMenuItemDefault.setMnemonic('D');
        jMenuItemAlternate = new JMenuItem(MonitorBundle.get("menu.options.color.alternate"));
        jMenuItemAlternate.setMnemonic('A');
        jMenuColor.add(jMenuItemForeground);
        jMenuColor.add(jMenuItemForeground2);
        jMenuColor.add(jMenuItemBackground);
        jMenuColor.add(jMenuItemScale);
        jMenuColor.addSeparator();
        jMenuColor.add(jMenuItemDefault);
        jMenuColor.add(jMenuItemAlternate);
        jMenuNetworkBandwidth = new JMenu(MonitorBundle.get("menu.options.network.bandwidth"));
        jMenuNetworkBandwidth.setMnemonic('B');
        jMenuNetworkBandwidth.setEnabled(performance == PerformanceEnum.NETWORK);
        jRadioButtonMenuItem128mb = new JRadioButtonMenuItem(NetworkScaleEnum.SCALE_128MB.getDescription());
        jRadioButtonMenuItem128mb.setSelected(network_scale == NetworkScaleEnum.SCALE_128MB);
        jRadioButtonMenuItem32mb = new JRadioButtonMenuItem(NetworkScaleEnum.SCALE_32MB.getDescription());
        jRadioButtonMenuItem32mb.setSelected(network_scale == NetworkScaleEnum.SCALE_32MB);
        jRadioButtonMenuItem8mb = new JRadioButtonMenuItem(NetworkScaleEnum.SCALE_8MB.getDescription());
        jRadioButtonMenuItem8mb.setSelected(network_scale == NetworkScaleEnum.SCALE_8MB);
        jRadioButtonMenuItem2mb = new JRadioButtonMenuItem(NetworkScaleEnum.SCALE_2MB.getDescription());
        jRadioButtonMenuItem2mb.setSelected(network_scale == NetworkScaleEnum.SCALE_2MB);
        jMenuNetworkBandwidth.add(jRadioButtonMenuItem2mb);
        jMenuNetworkBandwidth.add(jRadioButtonMenuItem8mb);
        jMenuNetworkBandwidth.add(jRadioButtonMenuItem32mb);
        jMenuNetworkBandwidth.add(jRadioButtonMenuItem128mb);
        jMenuItemAbout = new JMenuItem(MonitorBundle.get("menu.help.about"));
        jMenuHelp = new JMenu(MonitorBundle.get("menu.help"));
        jMenuHelp.setMnemonic('H');
        jCheckBoxMenuItemShowScale = new JCheckBoxMenuItem(MonitorBundle.get("menu.options.showscale"), true);
        jCheckBoxMenuItemShowScale.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                showScale = jCheckBoxMenuItemShowScale.getState();
                repaint();
            }
        });
        jCheckBoxMenuItemShowScale.setMnemonic('h');
        jCheckBoxMenuItemToolbar = new JCheckBoxMenuItem(MonitorBundle.get("menu.options.toolbar"), true);
        jCheckBoxMenuItemToolbar.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                showToolbar = jCheckBoxMenuItemToolbar.getState();
                jToolBar.setVisible(showToolbar);
                revalidateMonitor();
                repaint();
            }
        });
        jCheckBoxMenuItemToolbar.setMnemonic('T');

        jCheckBoxMenuItemShowCpuValue = new JCheckBoxMenuItem(MonitorBundle.get("menu.options.showcpu"), showCpuValue);
        jCheckBoxMenuItemShowCpuValue.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                showCpuValue = jCheckBoxMenuItemShowCpuValue.getState();
                repaint();
            }
        });
        jCheckBoxMenuItemShowCpuValue.setMnemonic('V');
        jCheckBoxMenuItemShowMemoryInfo = new JCheckBoxMenuItem(MonitorBundle.get("menu.options.showmemory"), showMemoryInfo);
        jCheckBoxMenuItemShowMemoryInfo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                showMemoryInfo = jCheckBoxMenuItemShowMemoryInfo.getState();
                repaint();
            }
        });
        jCheckBoxMenuItemShowMemoryInfo.setMnemonic('M');
        jCheckBoxMenuItemShowNetworkDetail = new JCheckBoxMenuItem(MonitorBundle.get("menu.options.shownetwork"), showNetworkDetail);
        jCheckBoxMenuItemShowNetworkDetail.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                showNetworkDetail = jCheckBoxMenuItemShowNetworkDetail.getState();
                repaint();
            }
        });
        jCheckBoxMenuItemShowNetworkDetail.setMnemonic('N');
        jCheckBoxMenuItemLeftToRight = new JCheckBoxMenuItem(MonitorBundle.get("menu.options.lefttoright"),
                graphDirection == GraphDirectionEnum.LEFT_TO_RIGHT);
        jCheckBoxMenuItemLeftToRight.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                graphDirection = jCheckBoxMenuItemLeftToRight.getState()
                        ? GraphDirectionEnum.LEFT_TO_RIGHT : GraphDirectionEnum.RIGHT_TO_LEFT;
                remapGraphXPoints();
                repaint();
            }
        });
        jCheckBoxMenuItemLeftToRight.setMnemonic('L');
        jMenuItemAbout.setMnemonic('A');
        jMenuHelp.add(jMenuItemAbout);
        jMenuTools.add(jMenuItemCpuList);
        jMenuTools.add(jMenuItemCpuChart);
        jMenuTools.addSeparator();
        jMenuTools.add(jMenuItemMemoryChart);
        jMenuTools.addSeparator();
        jMenuTools.add(jMenuItemDiskSpace);
        jMenuOptions.add(jMenuSpeed);
        jMenuOptions.add(jMenuColor);
        jMenuOptions.addSeparator();
        jMenuOptions.add(jCheckBoxMenuItemShowScale);
        jMenuOptions.add(jCheckBoxMenuItemToolbar);
        jMenuOptions.addSeparator();
        jMenuOptions.add(jCheckBoxMenuItemShowCpuValue);
        jMenuOptions.add(jCheckBoxMenuItemShowMemoryInfo);
        jMenuOptions.add(jCheckBoxMenuItemShowNetworkDetail);
        jMenuOptions.add(jMenuNetworkBandwidth);
        jMenuOptions.addSeparator();
        jMenuOptions.add(jCheckBoxMenuItemLeftToRight);
        jMenuBar1.add(jMenuFile);
        jMenuBar1.add(jMenuPerformance);
        jMenuBar1.add(jMenuTools);
        jMenuBar1.add(jMenuOptions);
        jMenuBar1.add(jMenuHelp);
        setJMenuBar(jMenuBar1);
    }

    private void initPopupMenu() {
        jPopupMenu1 = new JPopupMenu();
        jRadioButtonMenuItemCPU2 = new JRadioButtonMenuItem(MonitorBundle.get("menu.performance.cpu"));
        jRadioButtonMenuItemCPU2.setSelected(true);
        jRadioButtonMenuItemMemory2 = new JRadioButtonMenuItem(MonitorBundle.get("menu.performance.memory"));
        jRadioButtonMenuItemMemory2.setSelected(false);
        jRadioButtonMenuItemNetwork2 = new JRadioButtonMenuItem(MonitorBundle.get("menu.performance.network"));
        jRadioButtonMenuItemNetwork2.setMnemonic('N');
        jPopupMenu1.add(jRadioButtonMenuItemCPU2);
        jPopupMenu1.add(jRadioButtonMenuItemMemory2);
        jPopupMenu1.add(jRadioButtonMenuItemNetwork2);
        jPopupMenu1.addSeparator();
        jMenuItemAbout2 = new JMenuItem(MonitorBundle.get("menu.help.about"));
        jPopupMenu1.add(jMenuItemAbout2);
    }

    private void initStatusBar() {
        jLabelUsage = new JLabel("", JLabel.LEFT);
        jLabelUsage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelUsage.setBackground(Color.lightGray);
        getContentPane().add(jLabelUsage, BorderLayout.SOUTH);
    }

    private void wireActionListeners() {
        jButtonSetup.addActionListener(this);
        jButtonCpu.addActionListener(this);
        jButtonMemory.addActionListener(this);
        jButtonNetwork.addActionListener(this);
        jButtonAbout.addActionListener(this);
        jMenuItemSetup.addActionListener(this);
        jMenuItemExit.addActionListener(this);
        jMenuItemDiskSpace.addActionListener(this);
        jMenuItemCpuList.addActionListener(this);
        jMenuItemCpuChart.addActionListener(this);
        jMenuItemMemoryChart.addActionListener(this);
        jMenuItemAbout.addActionListener(this);
        jMenuItemAbout2.addActionListener(this);
        jRadioButtonMenuItemCPU.addActionListener(this);
        jRadioButtonMenuItemMemory.addActionListener(this);
        jRadioButtonMenuItemNetwork.addActionListener(this);
        jRadioButtonMenuItemNormal.addActionListener(this);
        jRadioButtonMenuItemSlower.addActionListener(this);
        jRadioButtonMenuItemFaster.addActionListener(this);
        jRadioButtonMenuItem128mb.addActionListener(this);
        jRadioButtonMenuItem32mb.addActionListener(this);
        jRadioButtonMenuItem8mb.addActionListener(this);
        jRadioButtonMenuItem2mb.addActionListener(this);
        jMenuItemForeground.addActionListener(this);
        jMenuItemForeground2.addActionListener(this);
        jMenuItemBackground.addActionListener(this);
        jMenuItemScale.addActionListener(this);
        jMenuItemDefault.addActionListener(this);
        jMenuItemAlternate.addActionListener(this);
        jRadioButtonMenuItemCPU2.addActionListener(this);
        jRadioButtonMenuItemMemory2.addActionListener(this);
        jRadioButtonMenuItemNetwork2.addActionListener(this);
        addMouseListener(this);
    }

    // -----------------------------------------------------------------
    // Event handlers
    // -----------------------------------------------------------------

    @Override
    public void mousePressed(MouseEvent e) {
        showPopupMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    private void showPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            jPopupMenu1.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void centerDialog(JFrame frame, JDialog dialog) {
        Dimension dialogSize = dialog.getSize();
        Dimension frameSize = frame.getSize();
        Point loc = frame.getLocation();
        dialog.setLocation((frameSize.width - dialogSize.width) / 2 + loc.x,
                (frameSize.height - dialogSize.height) / 2 + loc.y);
        dialog.setVisible(true);
        dialog.setVisible(true);
    }

    private void centerCpuChartDialog(JFrame frame, JDialog dialog) {
        Dimension dialogSize = dialog.getSize();
        Dimension frameSize = frame.getSize();
        Point loc = frame.getLocation();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (frameSize.width - dialogSize.width) / 2 + loc.x;
        int y = (frameSize.height - dialogSize.height) / 2 + loc.y;
        int x2 = x + 16;
        if (x2 + dialogSize.width > screenSize.width) {
            x2 -= 32;
        }
        int y2 = y + 16;
        if (y2 + dialogSize.height > screenSize.height) {
            y2 -= 32;
        }
        dialog.setLocation(x2, y2);
        dialog.setVisible(true);
    }

    public void actionPerformed(ActionEvent evt) {
        Object obj = evt.getSource();
        if (obj == jMenuItemExit) {
            saveMonitorInfoToJson();
            System.exit(0);
        } else if (obj == jMenuItemSetup || obj == jButtonSetup) {
            SetupDialog testOrSetDialog = new SetupDialog(this, hostName, portNumber);
            centerDialog(this, testOrSetDialog);
        } else if (obj == jMenuItemDiskSpace) {
            if (diskSpaceDialog == null || (diskSpaceDialog != null && !diskSpaceDialog.isVisible())) {
                if (jsonDisks != null) {
                    diskSpaceDialog = new DiskSpaceDialog(this, hostName, jsonDisks);
                    centerDialog(this, diskSpaceDialog);
                }
            }
        } else if (obj == jMenuItemCpuList) {
            if (cpuListDialog == null || (cpuListDialog != null && !cpuListDialog.isVisible())) {
                if (jsonCpuList != null) {
                    cpuListDialog = new CpuListDialog(this, hostName, jsonCpus);
                    centerDialog(this, cpuListDialog);
                }
            }
        } else if (obj == jMenuItemCpuChart) {
            if (cpuChartDialog == null || (cpuChartDialog != null && !cpuChartDialog.isVisible())) {
                if (jsonCpuList != null) {
                    cpuChartDialog = new CpuBarChartDialog(this, hostName, jsonCpus);
                    centerCpuChartDialog(this, cpuChartDialog);
                }
            }
        } else if (obj == jMenuItemMemoryChart) {
            if (memoryChartDialog == null || (memoryChartDialog != null && !memoryChartDialog.isVisible())) {
                if (jsonMemory != null) {
                    memoryChartDialog = new MemoryPieChartDialog(this, hostName, jsonMemory);
                    centerDialog(this, memoryChartDialog);
                }
            }
        } else if (obj == jMenuItemAbout || obj == jMenuItemAbout2 || obj == jButtonAbout)  {
            AboutDialog aboutDialog = new AboutDialog(this);
            Image icon = new ImageIcon(getClass().getResource(IMAGE_NAME)).getImage();
            aboutDialog.setIconImage(icon);
            centerDialog(this, aboutDialog);
        } else if (obj == jRadioButtonMenuItemCPU || obj == jRadioButtonMenuItemCPU2 || obj == jButtonCpu) {
            jRadioButtonMenuItemCPU.setSelected(true);
            jRadioButtonMenuItemCPU2.setSelected(true);
            jRadioButtonMenuItemMemory.setSelected(false);
            jRadioButtonMenuItemMemory2.setSelected(false);
            jRadioButtonMenuItemNetwork.setSelected(false);
            jRadioButtonMenuItemNetwork2.setSelected(false);
            performance = PerformanceEnum.CPU;
            jMenuNetworkBandwidth.setEnabled(false);
            repaint();
        } else if (obj == jRadioButtonMenuItemMemory || obj == jRadioButtonMenuItemMemory2 || obj == jButtonMemory) {
            jRadioButtonMenuItemCPU.setSelected(false);
            jRadioButtonMenuItemCPU2.setSelected(false);
            jRadioButtonMenuItemMemory.setSelected(true);
            jRadioButtonMenuItemMemory2.setSelected(true);
            jRadioButtonMenuItemNetwork.setSelected(false);
            jRadioButtonMenuItemNetwork2.setSelected(false);
            performance = PerformanceEnum.MEMORY;
            jMenuNetworkBandwidth.setEnabled(false);
            repaint();
        } else if (obj == jRadioButtonMenuItemNetwork || obj == jRadioButtonMenuItemNetwork2 || obj == jButtonNetwork) {
            jRadioButtonMenuItemCPU.setSelected(false);
            jRadioButtonMenuItemCPU2.setSelected(false);
            jRadioButtonMenuItemMemory.setSelected(false);
            jRadioButtonMenuItemMemory2.setSelected(false);
            jRadioButtonMenuItemNetwork.setSelected(true);
            jRadioButtonMenuItemNetwork2.setSelected(true);
            performance = PerformanceEnum.NETWORK;
            jMenuNetworkBandwidth.setEnabled(true);
            repaint();
        } else if (obj == jRadioButtonMenuItemNormal) {
            jRadioButtonMenuItemNormal.setSelected(true);
            jRadioButtonMenuItemSlower.setSelected(false);
            jRadioButtonMenuItemFaster.setSelected(false);
            speed = SpeedEnum.NORMAL;
            repaint();
        } else if (obj == jRadioButtonMenuItemSlower) {
            jRadioButtonMenuItemNormal.setSelected(false);
            jRadioButtonMenuItemSlower.setSelected(true);
            jRadioButtonMenuItemFaster.setSelected(false);
            speed = SpeedEnum.SLOWER;
            repaint();
        } else if (obj == jRadioButtonMenuItemFaster) {
            jRadioButtonMenuItemNormal.setSelected(false);
            jRadioButtonMenuItemSlower.setSelected(false);
            jRadioButtonMenuItemFaster.setSelected(true);
            speed = SpeedEnum.FASTER;
            repaint();
        } else if (obj == jRadioButtonMenuItem128mb) {
            jRadioButtonMenuItem128mb.setSelected(true);
            jRadioButtonMenuItem32mb.setSelected(false);
            jRadioButtonMenuItem8mb.setSelected(false);
            jRadioButtonMenuItem2mb.setSelected(false);
            network_scale = NetworkScaleEnum.SCALE_128MB;
            repaint();
        } else if (obj == jRadioButtonMenuItem32mb) {
            jRadioButtonMenuItem128mb.setSelected(false);
            jRadioButtonMenuItem32mb.setSelected(true);
            jRadioButtonMenuItem8mb.setSelected(false);
            jRadioButtonMenuItem2mb.setSelected(false);
            network_scale = NetworkScaleEnum.SCALE_32MB;
            repaint();
        } else if (obj == jRadioButtonMenuItem8mb) {
            jRadioButtonMenuItem128mb.setSelected(false);
            jRadioButtonMenuItem32mb.setSelected(false);
            jRadioButtonMenuItem8mb.setSelected(true);
            jRadioButtonMenuItem2mb.setSelected(false);
            network_scale = NetworkScaleEnum.SCALE_8MB;
            repaint();
        } else if (obj == jRadioButtonMenuItem2mb) {
            jRadioButtonMenuItem128mb.setSelected(false);
            jRadioButtonMenuItem32mb.setSelected(false);
            jRadioButtonMenuItem8mb.setSelected(false);
            jRadioButtonMenuItem2mb.setSelected(true);
            network_scale = NetworkScaleEnum.SCALE_2MB;
            repaint();
        } else if (obj == jMenuItemForeground) {
            Color color = JColorChooser.showDialog(this, MonitorBundle.get("color.chooser.foreground"), fgColor);
            if (color != null) {
                fgColor = color;
                repaint();
            }
        } else if (obj == jMenuItemForeground2) {
            Color color = JColorChooser.showDialog(this, MonitorBundle.get("color.chooser.foreground"), fgColor);
            if (color != null) {
                fgColor2 = color;
                repaint();
            }
        } else if (obj == jMenuItemBackground) {
            Color color = JColorChooser.showDialog(this, MonitorBundle.get("color.chooser.background"), bgColor);
            if (color != null) {
                bgColor = color;
                repaint();
            }
        } else if (obj == jMenuItemScale) {
            Color color = JColorChooser.showDialog(this, MonitorBundle.get("color.chooser.scale"), scaleColor);
            if (color != null) {
                scaleColor = color;
                repaint();
            }
        } else if (obj == jMenuItemDefault) {
            fgColor = DEFAULT_FGCOLOR;
            fgColor2 = DEFAULT_FGCOLOR2;
            bgColor = DEFAULT_BGCOLOR;
            scaleColor = DEFAULT_SCALECOLOR;
            repaint();
        } else if (obj == jMenuItemAlternate) {
            fgColor = ALTERNATE_FGCOLOR;
            fgColor2 = ALTERNATE_FGCOLOR;
            bgColor = ALTERNATE_BGCOLOR;
            scaleColor = ALTERNATE_SCALECOLOR;
            repaint();
        }
    }

    private void revalidateMonitor() {
        setGraphScale();
        getCpuUsage();
        getMemoryUsage();
        getNetworkUsage();
    }

    private void monitor() {
        setGraphScale();
        initCpuUsage();
        initMemoryUsage();
        initNetworkUsage();
        thread = new Thread(this);
        thread.start();
    }

    // -----------------------------------------------------------------
    // Window / component listeners
    // -----------------------------------------------------------------

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        saveMonitorInfoToJson();
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        revalidateMonitor();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    // -----------------------------------------------------------------
    // CLI usage and main
    // -----------------------------------------------------------------

    private static void usage(int portNumber, int timeoutSec, Protocol protocol) {
        String port = Integer.toString(portNumber);
        String timeout = Integer.toString(timeoutSec);
        System.out.println(MonitorBundle.format("cli.usage.header", TITLE_NAME));
        System.out.println(MonitorBundle.get("cli.usage.options"));
        System.out.println(MonitorBundle.get("cli.usage.protocol"));
        System.out.println(MonitorBundle.get("cli.usage.host"));
        System.out.println(MonitorBundle.format("cli.usage.port", port));
        if (protocol == Protocol.HTTP) {
            System.out.println(MonitorBundle.get("cli.usage.path"));
        }
        if (protocol != Protocol.GRPC) {
            System.out.println(MonitorBundle.get("cli.usage.echo"));
        }
        System.out.println(MonitorBundle.format("cli.usage.timeout", timeout));
        System.out.println(MonitorBundle.get("cli.usage.help"));
        System.out.println(MonitorBundle.get("cli.usage.examples"));
        System.out.println(MonitorBundle.format("cli.usage.example1", TITLE_NAME, port));
        System.out.println(MonitorBundle.format("cli.usage.example2", TITLE_NAME, port));
    }

    public static void main(String[] args) {
        boolean enableNumbus = false;
        if (enableNumbus) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                     | javax.swing.UnsupportedLookAndFeelException ex) {
                logger.severe(MonitorBundle.format("log.exception", TITLE_NAME, ex.getMessage()));
            }
        }
        invokeLater(() -> {
            MonitorClient monitorClient = new MonitorClient();
            Options options = new Options();
            options.addOption("h", "host", true, MonitorBundle.get("cli.help.host"));
            options.addOption("p", "port", true, MonitorBundle.get("cli.help.port"));
            options.addOption("P", "protocol", true, MonitorBundle.get("cli.help.protocol"));
            options.addOption("a", "path", true, MonitorBundle.get("cli.help.path"));
            options.addOption("e", "echo", false, MonitorBundle.get("cli.help.echo"));
            options.addOption("t", "timeout", true, MonitorBundle.get("cli.help.timeout"));
            options.addOption("?", "help", false, MonitorBundle.get("cli.help.printhelp"));
            CommandLineParser parser = new DefaultParser();
            try {
                CommandLine cmd = parser.parse(options, args);

                if (cmd.hasOption("P") || cmd.hasOption("protocol")) {
                    String protoStr = cmd.hasOption("P") ? cmd.getOptionValue("P") : cmd.getOptionValue("protocol");
                    try {
                        monitorClient.protocol = Protocol.fromString(protoStr);
                        monitorClient.portNumber = monitorClient.protocol.getDefaultPort();
                    } catch (IllegalArgumentException e) {
                        logger.warning(MonitorBundle.format("log.invalid.protocol", TITLE_NAME, protoStr));
                        System.exit(1);
                    }
                }

                if (cmd.hasOption("h")) {
                    monitorClient.hostName = cmd.getOptionValue("host");
                }
                if (cmd.hasOption("p")) {
                    try {
                        monitorClient.portNumber = Integer.parseInt(cmd.getOptionValue("port"));
                    } catch (NumberFormatException nfe) {
                        logger.warning(MonitorBundle.format("log.invalid.port", TITLE_NAME, cmd.getOptionValue("port")));
                        System.exit(1);
                    }
                }
                if (cmd.hasOption("a")) {
                    monitorClient.apiPath = cmd.getOptionValue("path");
                }
                if (cmd.hasOption("e")) {
                    monitorClient.echo = true;
                }
                if (cmd.hasOption("t")) {
                    try {
                        int timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
                        if (timeout < 1) {
                            logger.warning(MonitorBundle.format("log.invalid.timeout", TITLE_NAME, cmd.getOptionValue("timeout")));
                            System.exit(1);
                        }
                        if (monitorClient.protocol == Protocol.GRPC) {
                            monitorClient.grpcTimeoutSeconds = timeout;
                        } else {
                            if (timeout < MIN_SOCKET_TIMEOUT_SEC || timeout > MAX_SOCKET_TIMEOUT_SEC) {
                                logger.warning(MonitorBundle.format("log.invalid.timeout", TITLE_NAME, cmd.getOptionValue("timeout")));
                                System.exit(1);
                            }
                            monitorClient.socketTimeoutSec = timeout;
                        }
                    } catch (NumberFormatException nfe) {
                        logger.warning(MonitorBundle.format("log.invalid.timeout", TITLE_NAME, cmd.getOptionValue("timeout")));
                        System.exit(1);
                    }
                }
                if (cmd.hasOption("?")) {
                    int t = (monitorClient.protocol == Protocol.GRPC)
                            ? monitorClient.grpcTimeoutSeconds : monitorClient.socketTimeoutSec;
                    usage(monitorClient.portNumber, t, monitorClient.protocol);
                    System.exit(1);
                }

                try {
                    String profile = System.getProperty("user.dir")
                            + System.getProperty("file.separator") + MONITOR_RESOURCE;
                    MonitorResource.loadProperties(profile);
                } catch (Exception ex) {
                    logger.severe(MonitorBundle.format("log.exception", TITLE_NAME, ex.getMessage()));
                }

                System.out.println(monitorClient.hostName + ":" + monitorClient.portNumber
                        + " (" + monitorClient.protocol + ")");

                monitorClient.initialize();
                monitorClient.monitor();
            } catch (org.apache.commons.cli.ParseException e) {
                int t = (monitorClient.protocol == Protocol.GRPC)
                        ? monitorClient.grpcTimeoutSeconds : monitorClient.socketTimeoutSec;
                usage(monitorClient.portNumber, t, monitorClient.protocol);
                System.exit(1);
            }
        });
    }
}

// =====================================================================
// MonitorPanel — inner class that renders the live line chart
// =====================================================================

class MonitorPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final MonitorClient frame;

    MonitorPanel(MonitorClient frame) {
        this.frame = frame;
    }

    private void drawScale(Graphics g) {
        for (int y = 1; y < frame.yPointsScale.length - 1; y++) {
            if (y % 2 == 1) {
                String percent = (frame.yPointsScale.length - y - 1) * MonitorClient.SCALE_ROWS + "%";
                g.drawString(percent, (int) frame.xPointsScale[0], (int) frame.yPointsScale[y]);
            }
        }
        if (frame.showScale) {
            for (int y = 0; y < frame.yPointsScale.length; y++) {
                for (int x = 0; x < frame.xPointsScale.length - 1; x++) {
                    g.drawLine((int) frame.xPointsScale[x], (int) frame.yPointsScale[y],
                            (int) frame.xPointsScale[x + 1], (int) frame.yPointsScale[y]);
                }
            }
            for (int x = 0; x < frame.xPointsScale.length; x++) {
                for (int y = 0; y < frame.yPointsScale.length - 1; y++) {
                    g.drawLine((int) frame.xPointsScale[x], (int) frame.yPointsScale[y],
                            (int) frame.xPointsScale[x], (int) frame.yPointsScale[y + 1]);
                }
            }
        }
    }

    private Color getMixedColor(Color c1, int c1Pct, Color c2) {
        int c2Pct = 100 - c1Pct;
        return getMixedColor(c1, c2Pct, c2, c2Pct);
    }

    private Color getMixedColor(Color c1, int c1Pct, Color c2, int c2Pct) {
        return new Color(
            (c1.getRed() * c1Pct / 100 + c2.getRed() * c2Pct / 100),
            (c1.getGreen() * c1Pct / 100 + c2.getGreen() * c2Pct / 100),
            (c1.getBlue() * c1Pct / 100 + c2.getBlue() * c2Pct / 100));
    }

    private void drawGraph(Graphics g) {
        Font font = getFont();
        FontMetrics metrics = getFontMetrics(font);
        final int MENUBAR_CY = 17;
        final int TOOLBAR_CY = 31;
        int width = frame.getWidth();
        int height;

        if (frame.showToolbar) {
            height = frame.getHeight() / 2 - MENUBAR_CY - TOOLBAR_CY;
        } else {
            height = frame.getHeight() / 2 - TOOLBAR_CY;
        }
        if (frame.performance == PerformanceEnum.CPU) {
            frame.setTitle(MonitorBundle.format("title.cpu", frame.getHostName()));
            g.drawPolyline(frame.xPointsCpu, frame.yPointsCpu, frame.USAGE_INTERVAL);
            if (frame.isShowCpuValue()) {
                String cpuValueStr = MonitorBundle.format("graph.cpu.value", frame.cpuValue);
                int cpuValueWidth = metrics.stringWidth(cpuValueStr);
                g.setColor(frame.getFgColor2());
                int x1 = (width - cpuValueWidth) / 2;
                g.drawString(cpuValueStr, x1, height);
            }
            frame.getLabelUsage().setText(MonitorBundle.format("status.cpu", frame.cpuValue));
        } else if (frame.performance == PerformanceEnum.MEMORY) {
            frame.setTitle(MonitorBundle.format("title.memory", frame.getHostName()));
            g.drawPolyline(frame.xPointsMemory, frame.yPointsMemory, frame.USAGE_INTERVAL);
            Color memAvailColor = frame.getFgColor2();
            if (Arrays.equals(frame.memoryUsage, frame.memoryAvailable)) {
                memAvailColor = getMixedColor(frame.getFgColor(), 50, frame.getFgColor2());
            }
            g.setColor(memAvailColor);
            g.drawPolyline(frame.xPointsMemoryAvailable, frame.yPointsMemoryAvailable, frame.USAGE_INTERVAL);
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMaximumFractionDigits(0);
            nf.setMinimumFractionDigits(0);
            long memTotal = frame.memTotal * MonitorConstants.ONE_KIB;
            long memAvailable = frame.memAvailable * MonitorConstants.ONE_KIB;
            int memPercentAvailable = 0;
            if (memTotal > 0) {
                memPercentAvailable = (int) ((double) ((memAvailable) * 100 / memTotal) + 0.5);
                memPercentAvailable = Math.clamp(memPercentAvailable, 0, 100);
            }
            if (memTotal >= MonitorConstants.ONE_GIB) {
                String strMemTotal = nf.format((double) memTotal / MonitorConstants.ONE_GIB);
                frame.getLabelUsage().setText(MonitorBundle.format("status.memory.gib",
                        strMemTotal, frame.memoryValue, memPercentAvailable));
            } else if (memTotal >= MonitorConstants.ONE_MIB) {
                String strMemTotal = nf.format((double) memTotal / MonitorConstants.ONE_MIB);
                frame.getLabelUsage().setText(MonitorBundle.format("status.memory.mib",
                        strMemTotal, frame.memoryValue, memPercentAvailable));
            } else {
                String strMemTotal = nf.format((double) memTotal / MonitorConstants.ONE_KIB);
                frame.getLabelUsage().setText(MonitorBundle.format("status.memory.kib",
                        strMemTotal, frame.memoryValue, memPercentAvailable));
            }
            if (frame.isShowMemoryInfo()) {
                String memUsedStr = MonitorBundle.format("graph.memory.used", frame.memoryValue);
                String memAvailableStr = MonitorBundle.format("graph.memory.available", memPercentAvailable);
                int memUsedStrWidth = metrics.stringWidth(memUsedStr);
                String memInfo = memUsedStr + memAvailableStr;
                int memInfoWidth = metrics.stringWidth(memInfo);
                g.setColor(frame.getFgColor());
                int x1 = (width - memInfoWidth) / 2;
                g.drawString(memUsedStr, x1, height);
                g.setColor(frame.getFgColor2());
                int x2 = (width - memInfoWidth) / 2 + memUsedStrWidth;
                g.drawString(memAvailableStr, x2 + 8, height);
            }
        } else if (frame.performance == PerformanceEnum.NETWORK) {
            frame.setTitle(MonitorBundle.format("title.network", frame.getHostName()));
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            String strRcvByte = nf.format(frame.rcvByte / MonitorConstants.ONE_KIB);
            String strSndByte = nf.format(frame.sndByte / MonitorConstants.ONE_KIB);
            if (frame.isShowNetworkDetail()) {
                g.drawPolyline(frame.xPointsNetworkRcv, frame.yPointsNetworkRcv, frame.USAGE_INTERVAL);
                Color sndColor = frame.getFgColor2();
                if (Arrays.equals(frame.networkRcvUsage, frame.networkSndUsage)) {
                    sndColor = getMixedColor(frame.getFgColor(), 50, frame.getFgColor2());
                }
                g.setColor(sndColor);
                g.drawPolyline(frame.xPointsNetworkSnd, frame.yPointsNetworkSnd, frame.USAGE_INTERVAL);
                String rcvByteStr = MonitorBundle.format("graph.network.rcv", strRcvByte);
                String sndByteStr = MonitorBundle.format("graph.network.snd", strSndByte);
                int rcvByteStrWidth = metrics.stringWidth(rcvByteStr);
                String byteInfo = rcvByteStr + sndByteStr;
                int byteInfoWidth = metrics.stringWidth(byteInfo);
                g.setColor(frame.getFgColor());
                int x1 = (width - byteInfoWidth) / 2;
                g.drawString(rcvByteStr, x1, height);
                g.setColor(frame.getFgColor2());
                int x2 = (width - byteInfoWidth) / 2 + rcvByteStrWidth;
                g.drawString(sndByteStr, x2 + 8, height);
            } else {
                g.drawPolyline(frame.xPointsNetwork, frame.yPointsNetwork, frame.USAGE_INTERVAL);
            }
            String scaleUsage;
            long networkBytes = frame.rcvByte + frame.sndByte;
            int percent;
            if (frame.network_scale == NetworkScaleEnum.SCALE_2MB) {
                percent = (int) ((double) (networkBytes * 100
                        / NetworkScaleEnum.SCALE_2MB.getBandwidth()) + 0.5);
            } else if (frame.network_scale == NetworkScaleEnum.SCALE_8MB) {
                percent = (int) ((double) (networkBytes * 100
                        / NetworkScaleEnum.SCALE_8MB.getBandwidth()) + 0.5);
            } else if (frame.network_scale == NetworkScaleEnum.SCALE_32MB) {
                percent = (int) ((double) (networkBytes * 100
                        / NetworkScaleEnum.SCALE_32MB.getBandwidth()) + 0.5);
            } else {
                percent = (int) ((double) (networkBytes * 100
                        / NetworkScaleEnum.SCALE_128MB.getBandwidth()) + 0.5);
            }
            scaleUsage = MonitorBundle.format("graph.network.usage", percent,
                    frame.network_scale.getDescription());
            String networkInterface = frame.getNetworkInterface();
            if (networkInterface.isEmpty()) {
                frame.getLabelUsage().setText(MonitorBundle.format("status.network",
                        scaleUsage, strRcvByte, strSndByte));
            } else {
                frame.getLabelUsage().setText(MonitorBundle.format("status.network_interface",
                        scaleUsage, strRcvByte, strSndByte, networkInterface));
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(frame.bgColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(frame.scaleColor);
        drawScale(g);
        g.setColor(frame.fgColor);
        drawGraph(g);
    }
}
