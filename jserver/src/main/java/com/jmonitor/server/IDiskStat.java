/**
 * @author anan.mahasintunan
 * file: IDiskStat.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

import java.util.List;

public interface IDiskStat {
    List<DiskStatObject> getListDiskStat(boolean exclude) throws Exception;

    String getDiskStatEntries(boolean exclude) throws Exception;
}