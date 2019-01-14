public class Main {

    public static void main(String[] args) {

    }

    //TODO: KMM
    //TODO: K3M


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
