/**
 * @author anan.mahasintunan
 * file: MonitorResource.java
 * date: 08/17/2025
 */

public class MonitorResource {
    private static String s_browserPath = null; // Browser path for Linux eg. Chrome, Firefox
    private static MonitorResource resourceInstance = null;

    private MonitorResource() {
    }

    public static MonitorResource getInstance() {
        if (resourceInstance == null) {
            resourceInstance = new MonitorResource();
        }

        return resourceInstance;
    }

    public static void loadProperties(String property) throws Exception {
        MonitorProperties prop = MonitorProperties.getSingleProperties();
        prop.loadProperties(property);

        s_browserPath = prop.getMapProp(MonitorClient.BROWSER_PATH);
        if (s_browserPath == null) {
            s_browserPath = MonitorClient.GOOGLE_CMD;
        }
    }

    public static String getBrowserPath() {
        return s_browserPath;
    }

}
