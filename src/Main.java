import javafx.geometry.Pos;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class Position {
    int X;
    int Y;

    Position(int x, int y) {
        X = x;
        Y = y;
    }
}

public class Main {

    public static void main(String[] args) throws java.io.IOException {
        File f = new File("test.png");
        BufferedImage in = ImageIO.read(f);
        ImageIO.write( k3m(threshold(in, 1)), "png", new File("output.png"));
    }

    //TODO: KMM
    //TODO: K3M

    private static BufferedImage k3m(BufferedImage in){

        int width = in.getWidth(), height = in.getHeight();
        int[][] image = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int val = getG(in.getRGB(x, y));
                image[x][y] = val == 0 ? 1 : 0;
            }
        }

        int oldLenght=0;
        boolean flag = true;
        while (flag){
            ArrayList<Position> borderPix = border(image,a0);
            System.out.println(borderPix.toString());
            borderPix=phase(image,borderPix,a1);
            borderPix=phase(image,borderPix,a2);
            borderPix=phase(image,borderPix,a3);
            borderPix=phase(image,borderPix,a4);
            borderPix=phase(image,borderPix,a5);
            System.out.println(oldLenght+"|"+borderPix.size());
            if (oldLenght==borderPix.size()) flag=false;
            oldLenght=borderPix.size();
        }

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int weight = pixelWeight(image, x, y);

                if (a1pix.contains(weight))
                    image[x][y] = 0;
            }
        }

        printMask(image);


        BufferedImage out = new BufferedImage(width, height, in.getType());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                out.setRGB(x, y, toRGB(255,255,255));
                if (image[x][y] == 1)
                    out.setRGB(x, y, toRGB(0,0,0));
            }
        }
        return out;
    }

    private static int[][] addMaskZeroPadding(int[][] mask){
        //Add 0 padding to mask
        int[][] paddedMask= new int[mask.length+2][mask[0].length+2];
        for (int i = 0; i < paddedMask.length; i++) {
            for (int j = 0; j < paddedMask[0].length; j++) {
                if (i == 0 || i == paddedMask.length - 1 || j == 0 || j == paddedMask[0].length - 1) {
                    paddedMask[i][j]=0;
                } else {
                    paddedMask[i][j]=mask[i-1][j-1];
                }
            }
        }
        return paddedMask;
    }

    //Printing the mask (2D array)
    private static void printMask(int[][] mask){
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[0].length; j++) {
                int val = mask[i][j];
                //Comment the line below to see the mask with 0s in it
                if (val==0) {System.out.print(" ");} else
                    System.out.print(val);
            }
            System.out.println();
        }
    }

    //K3M Utility
    private static ArrayList<Position> border(int[][] image, HashSet<Integer> a0) {
        ArrayList<Position> borderPixels = new ArrayList<>();

        for (int x=1;x<image.length;x++){
            for (int y = 1; y < image[0].length;y++){
                int weight = pixelWeight(image,x,y);
                if (a0.contains(weight)) borderPixels.add(new Position(x, y));
            }
        }

        return borderPixels;
    }

    private static int pixelWeight( int[][] image, int x, int y) {
        int weight = 0;
        image=addMaskZeroPadding(image);
        x=x+1;
        y=y+1;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                weight += weightMatrix[i + 1][j + 1] * image[x + i][y + j];
            }
        }

        return weight;
    }

    private static ArrayList<Position> phase(int[][] image, ArrayList<Position> list, HashSet<Integer> weightSet){
        ArrayList<Position> toRemove = new ArrayList<>();
        for (Position p:list) {
            int weight = pixelWeight(image,p.X,p.Y);
            if(weightSet.contains(weight)){
                image[p.X][p.Y]=0;
                toRemove.add(p);
            }
        }

        for (Position p:toRemove) {
            list.remove(p);
        }
        return  list;
    }

    private static int[][] weightMatrix = new int[][]{
            {128, 1, 2},
            {64, 0, 4},
            {32, 16, 8}
    };

    private static HashSet<Integer> a0 = new HashSet<>(Arrays.asList(
            3, 6, 7, 12, 14, 15, 24, 28, 30, 31, 48, 56, 60,
            62, 63, 96, 112, 120, 124, 126, 127, 129, 131, 135,
            143, 159, 191, 192, 193, 195, 199, 207, 223, 224,
            225, 227, 231, 239, 240, 241, 243, 247, 248, 249,
            251, 252, 253, 254
    ));

    private static HashSet<Integer> a1 = new HashSet<>(Arrays.asList(
            7, 14, 28, 56, 112, 131, 193, 224
    ));

    private static HashSet<Integer> a2 = new HashSet<>(Arrays.asList(
            7, 14, 15, 28, 30, 56, 60, 112, 120, 131, 135,
            193, 195, 224, 225, 240
    ));

    private static HashSet<Integer> a3 = new HashSet<>(Arrays.asList(
            7, 14, 15, 28, 30, 31, 56, 60, 62, 112, 120,
            124, 131, 135, 143, 193, 195, 199, 224, 225, 227,
            240, 241, 248
    ));

    private static HashSet<Integer> a4 = new HashSet<>(Arrays.asList(
            7, 14, 15, 28, 30, 31, 56, 60, 62, 63, 112, 120,
            124, 126, 131, 135, 143, 159, 193, 195, 199, 207,
            224, 225, 227, 231, 240, 241, 243, 248, 249, 252
    ));

    private static HashSet<Integer> a5 = new HashSet<>(Arrays.asList(
            7, 14, 15, 28, 30, 31, 56, 60, 62, 63, 112, 120,
            124, 126, 131, 135, 143, 159, 191, 193, 195, 199,
            207, 224, 225, 227, 231, 239, 240, 241, 243, 248,
            249, 251, 252, 254
    ));

    private static ArrayList<HashSet<Integer>> weightsSets = new ArrayList<>(Arrays.asList(a1, a2, a3, a4, a5));

    private static HashSet<Integer> a1pix = new HashSet<>(Arrays.asList(
            3, 6, 7, 12, 14, 15, 24, 28, 30, 31, 48, 56,
            60, 62, 63, 96, 112, 120, 124, 126, 127, 129, 131,
            135, 143, 159, 191, 192, 193, 195, 199, 207, 223,
            224, 225, 227, 231, 239, 240, 241, 243, 247, 248,
            249, 251, 252, 253, 254
    ));


    //Utility

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
