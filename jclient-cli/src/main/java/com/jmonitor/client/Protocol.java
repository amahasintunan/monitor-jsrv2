/**
 * @author anan.mahasintunan
 * file: Protocol.java
 * date: 28/06/2026
 */
package com.jmonitor.client;

/**
 * Transport protocol enumeration for the consolidated JMonitor client.
 */
public enum Protocol {
    UDP,
    TCP,
    HTTP,
    GRPC;

    /**
     * Parse a protocol string (case-insensitive).
     * @param s the protocol name: "udp", "tcp", "http", or "grpc"
     * @return the matching Protocol, or null if no match
     */
    public static Protocol fromString(String s) {
        if (s == null) {
            return null;
        }
        String lower = s.toLowerCase();
        switch (lower) {
            case "udp":  return UDP;
            case "tcp":  return TCP;
            case "http": return HTTP;
            case "grpc": return GRPC;
            default:     return null;
        }
    }
}
