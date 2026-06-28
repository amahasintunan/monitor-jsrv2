/**
 * @author anan.mahasintunan
 * file: ICpuStat.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

public interface ICpuStat {
    int getCpuCount();

    int getCpuStatDelta() throws Exception;

    String getCpuListAsJson();

    CpuStatObject[] getCpuDeltaArray();

    int getCpuValue();

}