/*
 * File.   RgbColor.java
 * Date.   03/16/2024
 * Author. Anan Mahasintunan
 * Description.
 *         Client Side - Linux Performance Monitor for CPU, Memory and Network.
 */

public class RgbColor {
    final private int color_r;
    final private int color_g;
    final private int color_b;

    private RgbColor(RgbColorBuilder builder) {
        this.color_r = builder.color_r;
        this.color_g = builder.color_g;
        this.color_b = builder.color_b;
    }

    public int getColorR() {
        return color_r;
    }

    public int getColorG() {
        return color_g;
    }

    public int getColorB() {
        return color_b;
    }

    @Override
    public String toString() {
        return "RgbColor: " + "color_r=" + color_r + ", color_g=" + color_g + ", color_b=" + color_b;
    }

    public static class RgbColorBuilder {
        private int color_r;
        private int color_g;
        private int color_b;

        public RgbColorBuilder color_r(int color_r) {
            this.color_r = color_r;
            return this;
        }

        public RgbColorBuilder color_g(int color_g) {
            this.color_g = color_g;
            return this;
        }

        public RgbColorBuilder color_b(int color_b) {
            this.color_b = color_b;
            return this;
        }

        public RgbColor build() {
            return new RgbColor(this);
        }
    }

}
