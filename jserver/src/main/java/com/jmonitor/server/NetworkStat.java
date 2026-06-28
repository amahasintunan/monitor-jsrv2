/**
 * @author anan.mahasintunan
 * file: NetworkStat.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class NetworkStat {
    private static final Logger logger = Logger.getLogger(NetworkStat.class.getName());
    private static final String PROC_NET_DEV = "/proc/net/dev";
    private static NetworkStat networkStatInstance = null;
    private NetworkStatObject networkStatObj;
    private long rcv_delta = 0;
    private long snd_delta = 0;
    private long p_rcv = 0;
    private long p_snd = 0;

    public static NetworkStat getNetworkStatInstance() throws Exception {
        if (networkStatInstance == null) {
            networkStatInstance = new NetworkStat();
        }
        return networkStatInstance;
    }

    private NetworkStat() {
    }

    public synchronized void getNetworkStat() throws Exception {
        long rcv = 0;
        long snd = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(PROC_NET_DEV))) {
            String memoryLine;
            int line = 0;
            while ((memoryLine = br.readLine()) != null) {
                line++;
                if (line <= 2 || memoryLine.contains("lo:")) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(memoryLine);
                int i = 0;
                int count = 0;
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    switch (i) {
                        case 1:
                            rcv += Long.parseLong(token);
                            count++;
                            break;
                        case 9:
                            snd += Long.parseLong(token);
                            count++;
                            break;
                    }
                    i++;

                }
                if (count < 2) {
                    String msg = JMonitorServer.TITLE_NAME + ": getNetworkStat() failed to get network info from " + PROC_NET_DEV;
                    logger.severe(msg);
                    throw new Exception(msg);
                }
            }

        }
        rcv_delta = (p_rcv != 0) ? rcv - p_rcv : 0;
        snd_delta = (p_snd != 0) ? snd - p_snd : 0;
        networkStatObj = new NetworkStatObject.NetworkStatBuilder()
                .rcvByte(rcv_delta)
                .sndByte(snd_delta)
                .build();
        p_rcv = rcv;
        p_snd = snd;
    }

    public NetworkStatObject getNetworkStatObject() {
        return networkStatObj;
    }

}