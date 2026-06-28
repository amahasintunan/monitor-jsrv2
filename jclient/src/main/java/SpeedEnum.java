/*
 * File.   SpeedEnum.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

public enum SpeedEnum {
    SLOWER(5000, "speed.slower"), NORMAL(3000, "speed.normal"), FASTER(1000, "speed.faster");

    private final int speed;
    private final String descriptionKey;

    SpeedEnum(int speed, String descriptionKey) {
        this.speed = speed;
        this.descriptionKey = descriptionKey;
    }

    public int getSpeed() {
        return speed;
    }

    public String getDescription() {
        return MonitorBundle.get(descriptionKey);
    }
}
