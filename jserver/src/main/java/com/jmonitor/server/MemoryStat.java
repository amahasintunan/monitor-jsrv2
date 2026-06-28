/**
 * @author anan.mahasintunan
 * file: MemoryStat.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class MemoryStat implements IMemoryStat {
    private static final Logger logger = Logger.getLogger(MemoryStat.class.getName());
    private static final String PROC_MEMINFO = "/proc/meminfo";
    private static MemoryStat memoryStatInstance = null;
    private MemoryStatObject memoryStatObj;

    public static MemoryStat getMemoryStatInstance() throws Exception {
        if (memoryStatInstance == null) {
            memoryStatInstance = new MemoryStat();
        }
        return memoryStatInstance;
    }

    private MemoryStat() {
    }

    public synchronized void getMemoryStat() throws Exception {
        long memTotal = 0;
        long memFree = 0;
        long memAvailable = 0;
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(PROC_MEMINFO))) {
            String memoryLine;
            while ((memoryLine = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(memoryLine);
                if (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.equals("MemTotal:")) {
                        if (st.hasMoreTokens()) {
                            String token2 = st.nextToken();
                            memTotal = Long.parseLong(token2);
                            count++;
                        }
                    } else if (token.equals("MemFree:")) {
                        if (st.hasMoreTokens()) {
                            String token2 = st.nextToken();
                            memFree = Long.parseLong(token2);
                            count++;
                        }
                    } else if (token.equals("MemAvailable:")) {
                        if (st.hasMoreTokens()) {
                            String token2 = st.nextToken();
                            memAvailable = Long.parseLong(token2);
                            count++;
                            break;
                        }
                    }
                }
            }
            if (count < 3) {
                String msg = JMonitorServer.TITLE_NAME + ": getMemoryStat() failed to get memory info from " + PROC_MEMINFO;
                logger.severe(msg);
                throw new Exception(msg);
            }

        }
        memoryStatObj = new MemoryStatObject.MemoryStatObjectBuilder()
                .memTotal(memTotal)
                .memFree(memFree)
                .memAvailable(memAvailable)
                .build();
    }

    public MemoryStatObject getMemoryStatObject() {
        return memoryStatObj;
    }
}