import java.awt.*;

/*
 * File.   MonitorConstants.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

public class MonitorConstants {
    public static final long ONE_KIB = 1024;
    public static final long ONE_MIB = 1024 * 1024;
    public static final long ONE_GIB = 1024 * 1024 * 1024;
    public static final String KIB_UNIT = MonitorBundle.get("unit.kib");
    public static final String MIB_UNIT = MonitorBundle.get("unit.mib");
    public static final String GIB_UNIT = MonitorBundle.get("unit.gib");
    public static final int WARNING_HIGH = 90;
    public static final int WARNING_LOW = 75;
    public static final Color COLOR_WARNING_HIGH = new Color(255, 51, 51); // Color.red;
    public static final Color COLOR_WARNING_LOW = new Color(255, 153, 0); // light orange
    public static final Color COLOR_NO_WARNING = Color.black;
    public static final Color COLOR_AGGREGATE = new Color(0, 51, 102); // Color.blue;
    public static final Color COLOR_INDIVIDUAL = new Color(0, 128, 255); // Color.blue;
}
