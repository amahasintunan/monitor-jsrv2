/**
 * @author anan.mahasintunan
 * file: CpuStat.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class CpuStat implements ICpuStat {
    private static final Logger logger = Logger.getLogger(CpuStat.class.getName());
    private static final String PROC_STAT = "/proc/stat";
    private int cpuCount = 0;
    private CpuStatObject[] cpuInfoCurrent;
    private CpuStatObject[] cpuInfoPrevious;
    private CpuStatObject[] cpuInfoDelta;
    private boolean initialDelta = true;
    private static CpuStat cpuMonitorInstance = null;

    public static CpuStat getCpuMonitorInstance() throws Exception {
        if (cpuMonitorInstance == null) {
            cpuMonitorInstance = new CpuStat();
        }
        return cpuMonitorInstance;
    }

    private CpuStat() throws Exception {
        getCpuStat();
    }

    private synchronized void getCpuStat() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(PROC_STAT))) {
            String cpuLine;
            while ((cpuLine = br.readLine()) != null) {
                if (!cpuLine.startsWith("cpu")) {
                    break;
                }
                cpuCount++;
            }
            if (cpuCount == 0) {
                String msg = JMonitorServer.TITLE_NAME + ": CpuStat() failed to get CPU info from " + PROC_STAT;
                logger.severe(msg);
                throw new Exception(msg);
            }
            cpuInfoCurrent = new CpuStatObject[cpuCount];
            cpuInfoPrevious = new CpuStatObject[cpuCount];
            cpuInfoDelta = new CpuStatObject[cpuCount];
            for (int i = 0; i < cpuCount; i++) {
                cpuInfoCurrent[i] = new CpuStatObject();
                cpuInfoPrevious[i] = new CpuStatObject();
                cpuInfoDelta[i] = new CpuStatObject();
            }
        }
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public synchronized int getCpuStatDelta() throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(PROC_STAT))) {
            String cpuLine;
            int entry = 0;
            while ((cpuLine = br.readLine()) != null) {
                if (!cpuLine.startsWith("cpu")) {
                    break;
                }
                int i = 0;
                StringTokenizer st = new StringTokenizer(cpuLine);
                boolean done = false;
                cpuInfoCurrent[entry] = new CpuStatObject();
                while (st.hasMoreTokens() && !done) {
                    i++;
                    String token = st.nextToken();
                    switch (i) {
                        case 1:
                            String id = token;
                            cpuInfoCurrent[entry].setId(id);
                            cpuInfoDelta[entry].setId(id);
                            cpuInfoPrevious[entry].setId(id);
                            break;
                        case 2:
                            long user = Long.parseLong(token);
                            cpuInfoCurrent[entry].setUser(user);
                            cpuInfoDelta[entry].setUser(initialDelta ? 0 : user - cpuInfoPrevious[entry].getUser());
                            cpuInfoPrevious[entry].setUser(user);
                            break;
                        case 3:
                            long nice = Long.parseLong(token);
                            cpuInfoCurrent[entry].setNice(nice);
                            cpuInfoDelta[entry].setNice(initialDelta ? 0 : nice - cpuInfoPrevious[entry].getNice());
                            cpuInfoPrevious[entry].setNice(nice);
                            break;
                        case 4:
                            long system = Long.parseLong(token);
                            cpuInfoCurrent[entry].setSystem(system);
                            cpuInfoDelta[entry]
                                    .setSystem(initialDelta ? 0 : system - cpuInfoPrevious[entry].getSystem());
                            cpuInfoPrevious[entry].setSystem(system);
                            break;
                        case 5:
                            long idle = Long.parseLong(token);
                            cpuInfoCurrent[entry].setIdle(idle);
                            cpuInfoDelta[entry].setIdle(initialDelta ? 0 : idle - cpuInfoPrevious[entry].getIdle());
                            cpuInfoPrevious[entry].setIdle(idle);
                            break;
                        case 6:
                            long iowait = Long.parseLong(token);
                            cpuInfoCurrent[entry].setIOWait(iowait);
                            cpuInfoDelta[entry]
                                    .setIOWait(initialDelta ? 0 : iowait - cpuInfoPrevious[entry].getIOWait());
                            cpuInfoPrevious[entry].setIOWait(iowait);
                            break;
                        case 7:
                            long irq = Long.parseLong(token);
                            cpuInfoCurrent[entry].setIrq(irq);
                            cpuInfoDelta[entry].setIrq(initialDelta ? 0 : irq - cpuInfoPrevious[entry].getIrq());
                            cpuInfoPrevious[entry].setIrq(irq);
                            break;
                        case 8:
                            long softirq = Long.parseLong(token);
                            cpuInfoCurrent[entry].setSoftIrq(softirq);
                            cpuInfoDelta[entry]
                                    .setSoftIrq(initialDelta ? 0 : softirq - cpuInfoPrevious[entry].getSoftIrq());
                            cpuInfoPrevious[entry].setSoftIrq(softirq);
                            done = true;
                            break;
                    }
                }
                if (entry++ >= cpuCount - 1) {
                    break;
                }
            }
            if (entry == 0) {
                String msg = JMonitorServer.TITLE_NAME + ": getCpuStatDelta() failed to get CPU info from " + PROC_STAT;
                logger.severe(msg);
                throw new Exception(msg);
            }
        }
        initialDelta = false;
        return 0;
    }

    public String getCpuListAsJson() {
        String cpuData = "{";
        cpuData += String.format("\"entries\":%d,", cpuCount);
        cpuData += "\"cpus\":[";
        for (int entry = 0; entry < cpuCount; entry++) {
            int percentUsage = computeUsagePercent(cpuInfoDelta[entry]);
            if (entry < cpuCount - 1) {
                cpuData += String.format("{\"%s\":%d},", cpuInfoDelta[entry].getId(), percentUsage);
            } else {
                cpuData += String.format("{\"%s\":%d}", cpuInfoDelta[entry].getId(), percentUsage);
            }

        }
        cpuData += "]}";
        return cpuData;
    }

    public synchronized CpuStatObject[] getCpuDeltaArray() {
        return cpuInfoDelta;
    }

    public static int computeUsagePercent(CpuStatObject delta) {
        long total = delta.getUser() + delta.getNice() + delta.getSystem() +
                delta.getIdle() + delta.getIOWait() + delta.getIrq() + delta.getSoftIrq();
        if (total <= 0) {
            return 0;
        }
        long usage = total - delta.getIdle();
        int percentUsage = (int) (((double) usage / total) * 100.0 + 0.5);
        return Math.max(Math.min(percentUsage, 100), 0);
    }

    public int getCpuValue() {
        return computeUsagePercent(cpuInfoDelta[0]);
    }
}