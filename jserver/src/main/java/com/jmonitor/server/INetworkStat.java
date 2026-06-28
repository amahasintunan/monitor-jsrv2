/**
 * @author anan.mahasintunan
 * file: INetworkStat.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

public interface INetworkStat {
    void getNetworkStat() throws Exception;

    NetworkStatObject getNetworkStatObject();
}