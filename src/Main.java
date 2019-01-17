import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {

    public static void main(String[] args) throws java.io.IOException {
        File f = new File("input.png");
        BufferedImage in = ImageIO.read(f);
        kmm(threshold(in, 1));
    }

    //TODO: KMM
    //TODO: K3M

    private static void kmm(BufferedImage in){

    }

    // Thresholding
    private static BufferedImage threshold(BufferedImage in, int threshold) {
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), in.getType());
        in = grayscale(in);
        for (int i = 0; i < in.getHeight(); i++) {
            for (int j = 0; j < in.getWidth(); j++) {
                int r = getR(in.getRGB(j, i));
                if (r >= threshold) {
                    r = 255;
                } else {
                    r = 0;
                }
                out.setRGB(j, i, toRGB(r, r, r));
            }
        }
        return out;
    }


    // Grayscale
    private static BufferedImage grayscale(BufferedImage in) {
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), in.getType());
        for (int i = 0; i < in.getHeight(); i++) {
            for (int j = 0; j < in.getWidth(); j++) {
                int avg = (getR(in.getRGB(j, i)) + getB(in.getRGB(j, i)) + getG(in.getRGB(j, i))) / 3;
                out.setRGB(j, i, toRGB(avg, avg, avg));
            }
        }
        return out;
    }

    // Methods provided in the intro document
    private static int getR(int in) {
        return (int) ((in << 8) >> 24) & 0xff;
    }

    private static int getG(int in) {
        return (int) ((in << 16) >> 24) & 0xff;
    }

    private static int getB(int in) {
        return (int) ((in << 24) >> 24) & 0xff;
    }

    private static int toRGB(int r, int g, int b) {
        return (int) ((((r << 8) | g) << 8) | b);
    }


}
