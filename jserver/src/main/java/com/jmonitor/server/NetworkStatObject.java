/**
 * @author anan.mahasintunan
 * file: NetworkStatObject.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

public class NetworkStatObject {
    private final long rcvByte;
    private final long sndByte;

    private NetworkStatObject(NetworkStatBuilder builder) {
        this.rcvByte = builder.rcvByte;
        this.sndByte = builder.sndByte;
    }

    public long getRcvByte() {
        return rcvByte;
    }

    public long getSndByte() {
        return sndByte;
    }

    @Override
    public String toString() {
        return "NetworkStat: rcvByte=" + rcvByte + ", sndByte=" + sndByte;
    }

    public static class NetworkStatBuilder {
        private long rcvByte;
        private long sndByte;

        public NetworkStatBuilder rcvByte(long rcvByte) {
            this.rcvByte = rcvByte;
            return this;
        }

        public NetworkStatBuilder sndByte(long sndByte) {
            this.sndByte = sndByte;
            return this;
        }

        public NetworkStatObject build() {
            return new NetworkStatObject(this);
        }

    }

}
