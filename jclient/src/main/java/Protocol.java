/*
 * File.   Protocol.java
 * Date.   06/23/2026
 * Author. Anan Mahasintunan
 * Description.
 *         Protocol enum for multi-protocol monitor client.
 */

public enum Protocol {
    UDP(2019, "UDP"),
    TCP(2019, "TCP"),
    HTTP(2019, "HTTP"),
    GRPC(2019, "gRPC");

    private final int defaultPort;
    private final String displayName;

    Protocol(int defaultPort, String displayName) {
        this.defaultPort = defaultPort;
        this.displayName = displayName;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Parse a protocol string (case-insensitive).
     * Accepts: udp, tcp, http, grpc (or UPPERCASE/mixed).
     */
    public static Protocol fromString(String s) {
        if (s == null || s.isEmpty()) {
            return UDP; // default
        }
        switch (s.toLowerCase()) {
            case "udp":   return UDP;
            case "tcp":   return TCP;
            case "http":  return HTTP;
            case "grpc":  return GRPC;
            default:
                throw new IllegalArgumentException("Unknown protocol: " + s
                        + ". Valid values: udp, tcp, http, grpc");
        }
    }
}
