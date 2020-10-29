package ro.Domain;

public class Pixel {

    /*
            This class encapsulates the pixel of the image in 2 ways.

            RGB - By means of 3 components:
                Red color component,
                Blue color component,
                Green color component.
            YUV - By means of 3 components:
                Luminance component,
                Chrominance blue component,
                Chrominance red component.
     */

    private int red;
    private int green;
    private int blue;
    private double luminance;
    private double chrominance_blue;
    private double chrominance_red;

    public Pixel(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        convertRGBtoYUV();
    }

    public Pixel(double luminance, double chrominance_blue, double chrominance_red) {
        this.luminance = luminance;
        this.chrominance_blue = chrominance_blue;
        this.chrominance_red = chrominance_red;
        convertYUVtoRGB();
    }

    public Pixel() {
        this.red = 0;
        this.blue = 0;
        this.green = 0;
        convertRGBtoYUV();
    }

    private void convertRGBtoYUV() {
        this.luminance = 0.299 * this.red + 0.587 * this.green + 0.114 * this.blue;
        this.chrominance_blue = 128 - 0.169 * this.red - 0.331 * this.green + 0.499 * this.blue;
        this.chrominance_red = 128 + 0.499 * this.red - 0.418 * this.green - 0.0813 * this.blue;
    }

    private void convertYUVtoRGB() {
        this.red = Math.max(Math.min((int) (this.luminance + 1.402 * (this.chrominance_red - 128)), 255), 0);
        this.green = Math.max(Math.min((int) (this.luminance - 0.344 * (this.chrominance_blue - 128) - 0.714 * (this.chrominance_red - 128)), 255), 0);
        this.blue = Math.max(Math.min((int) (this.luminance + 1.772 * (this.chrominance_blue - 128)), 255), 0);
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public double getLuminance() {
        return luminance;
    }

    public void setLuminance(double luminance) {
        this.luminance = luminance;
    }

    public double getChrominance_blue() {
        return chrominance_blue;
    }

    public void setChrominance_blue(double chrominance_blue) {
        this.chrominance_blue = chrominance_blue;
    }

    public double getChrominance_red() {
        return chrominance_red;
    }

    public void setChrominance_red(double chrominance_red) {
        this.chrominance_red = chrominance_red;
    }

    @Override
    public String toString() {
        return red + "\n" + green + "\n" + blue;
    }
}
