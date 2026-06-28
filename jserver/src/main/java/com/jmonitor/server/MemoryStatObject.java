/**
 * @author anan.mahasintunan
 * file: MemoryStatObject.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

public class MemoryStatObject {
    private final long memTotal;
    private final long memFree;
    private final long memAvailable;

    private MemoryStatObject(MemoryStatObjectBuilder builder) {
        this.memTotal = builder.memTotal;
        this.memFree = builder.memFree;
        this.memAvailable = builder.memAvailable;
    }

    public long getMemTotal() {
        return memTotal;
    }

    public long getMemFree() {
        return memFree;
    }

    public long getMemAvailable() {
        return memAvailable;
    }

    @Override
    public String toString() {
        return "MemoryStat: memTotal=" + memTotal + ", memFree=" + memFree;
    }

    public static class MemoryStatObjectBuilder {
        private long memTotal;
        private long memFree;
        private long memAvailable;

        public MemoryStatObjectBuilder memTotal(long memTotal) {
            this.memTotal = memTotal;
            return this;
        }

        public MemoryStatObjectBuilder memFree(long memFree) {
            this.memFree = memFree;
            return this;
        }

        public MemoryStatObjectBuilder memAvailable(long memAvailable) {
            this.memAvailable = memAvailable;
            return this;
        }

        public MemoryStatObject build() {
            return new MemoryStatObject(this);
        }

    }

}
