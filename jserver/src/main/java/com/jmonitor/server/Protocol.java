/**
 * @author anan.mahasintunan
 * file: Protocol.java
 * date: 27/06/2026
 */
package com.jmonitor.server;

/**
 * Transport protocol enumeration for the unified JMonitor server.
 * Mirrors the Protocol enum in perf-monitors/cserver/smon.h.
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
