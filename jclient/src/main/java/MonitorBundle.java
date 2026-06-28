/*
 * File.   MonitorBundle.java
 * Description.
 *         Loader for externalized user-facing strings (monitor_bundle.properties).
 */

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public final class MonitorBundle {
    private static final Logger logger = Logger.getLogger(MonitorBundle.class.getName());
    private static final String BUNDLE_NAME = "monitor_bundle";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private MonitorBundle() {
    }

    public static String get(String key) {
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException ex) {
            logger.warning("Missing bundle key: " + key);
            return '!' + key + '!';
        }
    }

    public static String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
