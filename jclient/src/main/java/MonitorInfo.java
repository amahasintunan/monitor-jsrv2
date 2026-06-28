/*
 * File.   MonitorInfo.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

public class MonitorInfo {
    final private int x;
    final private int y;
    final private int cx;
    final private int cy;
    final private SpeedEnum speed;
    final private PerformanceEnum performance;
    final private NetworkScaleEnum networkScale;
    final private GraphDirectionEnum graphDirection;
    final private RgbColor fgColor;
    final private RgbColor fgColor2;
    final private RgbColor bgColor;
    final private RgbColor scaleColor;
    final private int timeoutSeconds;

    private MonitorInfo(MonitorInfoBuilder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.cx = builder.cx;
        this.cy = builder.cy;
        this.speed = builder.speed;
        this.performance = builder.performance;
        this.networkScale = builder.networkScale;
        this.graphDirection = builder.graphDirection;
        this.fgColor = builder.fgColor;
        this.fgColor2 = builder.fgColor2;
        this.bgColor = builder.bgColor;
        this.scaleColor = builder.scaleColor;
        this.timeoutSeconds = builder.timeoutSeconds;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCX() {
        return cx;
    }

    public int getCY() {
        return cy;
    }

    public SpeedEnum getSpeed() {
        return speed;
    }

    public PerformanceEnum getPerformance() {
        return performance;
    }

    public NetworkScaleEnum getNetworkScale() {
        return networkScale;
    }

    public GraphDirectionEnum getGraphDirection() {
        return graphDirection;
    }

    public RgbColor getBgColor() {
        return bgColor;
    }

    public RgbColor getFgColor() {
        return fgColor;
    }
    public RgbColor getFgColor2() {
        return fgColor2;
    }

    public RgbColor getScaleColor() {
        return scaleColor;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    @Override
    public String toString() {
        return "MonitorInfo: " + "x=" + x + ", y=" + y + ", cx=" + cx + ", cy=" + cy + ", performance=" + performance
                + ", speed=" + speed + ", networkScale=" + networkScale + ", graphDirection=" + graphDirection
                + ", fgColor=" + fgColor + ", fgColor2=" + fgColor2 + ", bgColor=" + bgColor + ", scaleColor=" + scaleColor
                + ", timeoutSeconds=" + timeoutSeconds;
    }

    public static class MonitorInfoBuilder {
        private int x;
        private int y;
        private int cx;
        private int cy;
        private SpeedEnum speed;
        private PerformanceEnum performance;
        private NetworkScaleEnum networkScale;
        private GraphDirectionEnum graphDirection;
        private RgbColor fgColor;
        private RgbColor fgColor2;
        private RgbColor bgColor;
        private RgbColor scaleColor;
        private int timeoutSeconds;

        public MonitorInfoBuilder x(int x) {
            this.x = x;
            return this;
        }

        public MonitorInfoBuilder y(int y) {
            this.y = y;
            return this;
        }

        public MonitorInfoBuilder cx(int cx) {
            this.cx = cx;
            return this;
        }

        public MonitorInfoBuilder cy(int cy) {
            this.cy = cy;
            return this;
        }

        public MonitorInfoBuilder speed(SpeedEnum speed) {
            this.speed = speed;
            return this;
        }

        public MonitorInfoBuilder performance(PerformanceEnum performance) {
            this.performance = performance;
            return this;
        }

        public MonitorInfoBuilder networkScale(NetworkScaleEnum networkScale) {
            this.networkScale = networkScale;
            return this;
        }

        public MonitorInfoBuilder graphDirection(GraphDirectionEnum graphDirection) {
            this.graphDirection = graphDirection;
            return this;
        }

        public MonitorInfoBuilder fgColor(RgbColor fgColor) {
            this.fgColor = fgColor;
            return this;
        }

        public MonitorInfoBuilder fgColor2(RgbColor fgColor2) {
            this.fgColor2 = fgColor2;
            return this;
        }

        public MonitorInfoBuilder bgColor(RgbColor bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public MonitorInfoBuilder scaleColor(RgbColor scaleColor) {
            this.scaleColor = scaleColor;
            return this;
        }

        public MonitorInfoBuilder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public MonitorInfo build() {
            return new MonitorInfo(this);
        }
    }

}
