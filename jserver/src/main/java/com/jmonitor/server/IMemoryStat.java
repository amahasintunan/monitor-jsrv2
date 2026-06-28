/**
 * @author anan.mahasintunan
 * file: IMemoryStat.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

public interface IMemoryStat {
    void getMemoryStat() throws Exception;

    MemoryStatObject getMemoryStatObject();
}