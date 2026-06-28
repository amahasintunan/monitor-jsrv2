/*
 * File.   DiskStat.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Server Side - Linux Performance Monitor for CPU, Memory and Network.
 */

public class DiskStat {
    String fileSystem;
    String type;
    long blocks;
    long used;
    long available;
    int useInPercent;
    String mountedOn;

    private DiskStat(DiskStatBuilder builder) {
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

    public int getUseInPercent() {
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

    public static class DiskStatBuilder {
        String fileSystem;
        String type;
        long blocks;
        long used;
        long available;
        int useInPercent;
        String mountedOn;

        public DiskStatBuilder fileSystem(String fileSystem) {
            this.fileSystem = fileSystem;
            return this;
        }

        public DiskStatBuilder type(String type) {
            this.type = type;
            return this;
        }

        public DiskStatBuilder blocks(long blocks) {
            this.blocks = blocks;
            return this;
        }

        public DiskStatBuilder used(long used) {
            this.used = used;
            return this;
        }

        public DiskStatBuilder available(long available) {
            this.available = available;
            return this;
        }

        public DiskStatBuilder useInPercent(int useInPercent) {
            this.useInPercent = useInPercent;
            return this;
        }

        public DiskStatBuilder mountedOn(String mountedOn) {
            this.mountedOn = mountedOn;
            return this;
        }

        public DiskStat build() {
            return new DiskStat(this);
        }

    }

}
