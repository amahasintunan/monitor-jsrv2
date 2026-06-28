/**
 * @author anan.mahasintunan
 * file: DiskStat.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/*
   "df -T -x squashfs" or "df -T -x squashfs -x tmpfs -x devtmpf"
   Filesystem             Type      1K-blocks      Used  Available Use% Mounted on
   udev                   devtmpfs   65773404         0   65773404   0% /dev
   /dev/mapper/crypt-root ext4       47741676  14518964   30765112  33% /
   /dev/mapper/crypt-home ext4      341025204 258222544   65406336  80% /home
   none                   tmpfs      65855000         0   65855000   0% /run/shm
   /dev/mapper/crypt-var  ext4       95532452  18960904   71672528  21% /var
   /dev/mapper/data_crypt ext4     1967799248 260642840 1607123896  14% /data
   /dev/sda1              ext4        1044028    503048     469764  52% /boot

*/

public class DiskStat implements IDiskStat {
    private static final Logger logger = Logger.getLogger(DiskStat.class.getName());
    private static DiskStat diskStatInstance = null;
    private static final String DF_DISK_FS = "df -T -x squashfs";
    private static final String DF_DISK_FS_EXCLUDE = "df -T -x squashfs -x tmpfs -x devtmpfs";
    private static final int MAX_DISK_ENTRIES = 50;

    public static DiskStat getDiskStatInstance() throws Exception {
        if (diskStatInstance == null) {
            diskStatInstance = new DiskStat();
        }
        return diskStatInstance;
    }

    private DiskStat() {
    }

    public String getDiskStatEntries(boolean exclude) throws Exception {
        String dfCommand = exclude ? DF_DISK_FS_EXCLUDE : DF_DISK_FS;
        String diskStat = "";
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(dfCommand);
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = null;
                while ((line = input.readLine()) != null) {
                    diskStat += line + "\n";
                }
                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    String msg = JMonitorServer.TITLE_NAME + ": getDiskStatEntries() failed to get disk from " + dfCommand
                            + " exitValue=" + exitValue;
                    logger.severe(msg);
                    throw new Exception(msg);
                }
            }
            if (diskStat.isEmpty()) {
                String msg = JMonitorServer.TITLE_NAME + ": getDiskStatEntries() diskStat is empty from " + dfCommand;
                logger.severe(msg);
                throw new Exception(msg);
            }
        } catch (Exception e) {
            String msg = JMonitorServer.TITLE_NAME + ": getDiskStatEntries() failed to get disk from " + dfCommand;
            logger.severe(msg);
            throw new Exception(msg);
        }

        return diskStat;
    }

    public synchronized List<DiskStatObject> getListDiskStat(boolean exclude) throws Exception {
        List<DiskStatObject> list = new ArrayList<>();
        String diskStat = getDiskStatEntries(exclude);
        StringTokenizer st = new StringTokenizer(diskStat, "\n");
        boolean first = true;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (first) {
                first = false;
                continue;
            }
            StringTokenizer st2 = new StringTokenizer(token, " \t");
            String fileSystem = "";
            String type = "";
            long blocks = 0;
            long used = 0;
            long available = 0;
            int useInPercent = 0;
            String mountedOn = "";
            int i = 1;
            while (st2.hasMoreTokens()) {
                String token2 = st2.nextToken();
                switch (i) {
                    case 1:
                        fileSystem = token2;
                        break;
                    case 2:
                        type = token2;
                        break;
                    case 3:
                        blocks = Long.parseLong(token2);
                        break;
                    case 4:
                        used = Long.parseLong(token2);
                        break;
                    case 5:
                        available = Long.parseLong(token2);
                        break;
                    case 6:
                        useInPercent = Integer.parseInt(token2.replace("%", ""));
                        break;
                    case 7:
                        mountedOn = token2;
                        break;
                }
                i++;
                if (list.size() >= MAX_DISK_ENTRIES) {
                    logger.warning(JMonitorServer.TITLE_NAME
                            + ": getListDiskStat() Exceeded the maximum number of disk entries of " + MAX_DISK_ENTRIES);
                    return list;
                }
            }
            DiskStatObject ds = new DiskStatObject.DiskStatObjectBuilder()
                    .fileSystem(fileSystem)
                    .type(type)
                    .blocks(blocks)
                    .used(used)
                    .available(available)
                    .useInPercent(useInPercent)
                    .mountedOn(mountedOn)
                    .build();
            list.add(ds);
        }

        return list;
    }

}