/*
 * File.   NetWorkScaleEnum.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

public enum NetworkScaleEnum {
    SCALE_2MB(2 * 1024 * 1024, "network.scale.2mb"), SCALE_8MB(8 * 1024 * 1024, "network.scale.8mb"),
    SCALE_32MB(32 * 1024 * 1024, "network.scale.32mb"), SCALE_128MB(128 * 1024 * 1024, "network.scale.128mb");

    private final long bandwidth;
    private final String descriptionKey;

    NetworkScaleEnum(long bandwidth, String descriptionKey) {
        this.bandwidth = bandwidth;
        this.descriptionKey = descriptionKey;
    }

    public long getBandwidth() {
        return bandwidth;
    }

    public String getDescription() {
        return MonitorBundle.get(descriptionKey);
    }

}
