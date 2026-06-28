/**
 * @author anan.mahasintunan
 * file: CpuStatObject.java
 * date: 12/03/2022
 */
package com.jmonitor.server;

public class CpuStatObject {
    private String id;
    private long user;
    private long nice;
    private long system;
    private long idle;
    private long iowait;
    private long irq;
    private long softirq;

    public CpuStatObject() {
        this.id = "";
        this.user = 0;
        this.nice = 0;
        this.system = 0;
        this.idle = 0;
        this.iowait = 0;
        this.irq = 0;
        this.softirq = 0;
    }

    public String getId() {
        return id;
    }

    public long getUser() {
        return user;
    }

    public long getNice() {
        return nice;
    }

    public long getSystem() {
        return system;
    }

    public long getIdle() {
        return idle;
    }

    public long getIOWait() {
        return iowait;
    }

    public long getIrq() {
        return irq;
    }

    public long getSoftIrq() {
        return softirq;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public void setNice(long nice) {
        this.nice = nice;
    }

    public void setSystem(long system) {
        this.system = system;
    }

    public void setIdle(long idle) {
        this.idle = idle;
    }

    public void setIOWait(long iowait) {
        this.iowait = iowait;
    }

    public void setIrq(long irq) {
        this.irq = irq;
    }

    public void setSoftIrq(long softirq) {
        this.softirq = softirq;
    }

    @Override
    public String toString() {
        return "CpuInfo: id=" + id + ", user=" + user + ", nice=" + nice + ", system=" + system +
                ", idle=" + idle + ", iowait=" + iowait + ", irq=" + irq + ", softirq=" + softirq;
    }

}