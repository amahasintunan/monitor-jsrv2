/**
 * @author anan.mahasintunan
 * file: DiskStatObject.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

public class DiskStatObject {
    String fileSystem;
    String type;
    long blocks;
    long used;
    long available;
    int useInPercent;
    String mountedOn;

    private DiskStatObject(DiskStatObjectBuilder builder) {
        this.fileSystem = builder.fileSystem;
        this.type = builder.type;
        this.blocks = builder.blocks;
        this.used = builder.used;
        this.available = builder.available;
        this.useInPercent = builder.useInPercent;
        this.mountedOn = builder.mountedOn;
    }

    public String getFileSystem() {
        return fileSystem;
    }

    public String getType() {
        return type;
    }

    public long getBlocks() {
        return blocks;
    }

    public long getUsed() {
        return used;
    }

    public long getAvailable() {
        return available;
    }

    public long getUseInPercent() {
        return useInPercent;
    }

    public String getMountedOn() {
        return mountedOn;
    }

    @Override
    public String toString() {
        return "DiskStat: fileSystem=" + fileSystem + ", type=" + type + ", blocks=" + blocks + ", used=" + used
                + ", available=" + available + ", useInPercent=" + useInPercent + ", mountedOn=" + mountedOn;
    }

    public static class DiskStatObjectBuilder {
        String fileSystem;
        String type;
        long blocks;
        long used;
        long available;
        int useInPercent;
        String mountedOn;

        public DiskStatObjectBuilder fileSystem(String fileSystem) {
            this.fileSystem = fileSystem;
            return this;
        }

        public DiskStatObjectBuilder type(String type) {
            this.type = type;
            return this;
        }

        public DiskStatObjectBuilder blocks(long blocks) {
            this.blocks = blocks;
            return this;
        }

        public DiskStatObjectBuilder used(long used) {
            this.used = used;
            return this;
        }

        public DiskStatObjectBuilder available(long available) {
            this.available = available;
            return this;
        }

        public DiskStatObjectBuilder useInPercent(int useInPercent) {
            this.useInPercent = useInPercent;
            return this;
        }

        public DiskStatObjectBuilder mountedOn(String mountedOn) {
            this.mountedOn = mountedOn;
            return this;
        }

        public DiskStatObject build() {
            return new DiskStatObject(this);
        }

    }

}