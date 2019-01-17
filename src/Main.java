import com.sun.deploy.util.ArrayUtil;
import sun.plugin.javascript.navig.Array;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws java.io.IOException {
        File f = new File("test.png");
        BufferedImage in = ImageIO.read(f);
        kmm(threshold(in, 1));
    }

    //TODO: KMM
    //TODO: K3M

    private static void kmm(BufferedImage in) throws IOException {

        //Represent the output image as a 2D array
        int width = in.getWidth(), height=in.getHeight();
        int[][] mask = new int[height][width];

        //Add zero padding
        in = addZeroPadding(in);

        //Step 1
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //Using getG() here because all channels are the same in grayscale image but this works for now
                //TODO: Replace getG() with a better method
                int val = getG(in.getRGB(i+1,j+1));
                if (val==0){
                    mask[j][i]=1;
                }else{
                    mask[j][i]=0;
                }
            }
        }

        System.out.println("State of the mask after the first step");
        printMask(mask);

        //Step 2

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (mask[j][i]==1){
                    if (checkEdges(addMaskZeroPadding(mask), i+1, j+1)) {
                        mask[j][i] = 2;
                    }
                }
            }
        }

        System.out.println("State of the mask after the second step");
        printMask(mask);

        //Step 3

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (mask[j][i]==1){
                    if (checkCorners(addMaskZeroPadding(mask), i+1, j+1)) {
                        mask[j][i] = 3;
                    }
                }
            }
        }

        System.out.println("State of the mask after the third step");
        printMask(mask);

        //Step 4

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (mask[j][i]==2||mask[j][i]==3){
                    if (countNeighbours(addMaskZeroPadding(mask),i+1,j+1)) {
                        mask[j][i] = 4;
                    }
                }
            }
        }

        System.out.println("State of the mask after the fourth step");
        printMask(mask);

        //Step 5

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (mask[j][i]==4){
                    mask[j][i]=0;
                }
            }
        }

        System.out.println("State of the mask after the fifth step");
        printMask(mask);

        //Step 6
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (mask[j][i]==2){
                    int weight=pixelWeight(addMaskZeroPadding(mask),i+1,j+1);
                  //  System.out.println(weight);
                    if(deletionArray.contains(weight)){ mask[j][i]=0;}else{mask[j][i]=1;}
                }
            }
        }



        ColorModel cm = in.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = in.copyData(null);
        BufferedImage out = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(mask[j][i]>0) out.setRGB(i,j,toRGB(0,0,0));
            }
        }
        ImageIO.write(out, "png", new File("out.png"));

    }

    private static final ArrayList<Integer> deletionArray = new ArrayList(Arrays.asList(3, 5, 7, 12, 13, 14,
            15, 20, 21, 22, 23, 28, 29, 30, 31, 48, 52, 53, 54, 55, 56, 60, 61, 62, 63, 65, 67, 69, 71, 77, 79, 80,
            81, 83, 84, 85, 86, 87, 88, 89, 91, 92, 93, 94, 95, 97, 99, 101, 103, 109, 111, 112,
            113, 115, 116, 117, 118, 119, 120, 121, 123, 124, 125, 126, 127, 131, 133, 135, 141,
            143, 149, 151, 157, 159, 181, 183, 189, 191, 192, 193, 195, 197, 199, 205, 207,
            208, 209, 211, 212, 213, 214, 215, 216, 217, 219, 220, 221, 222, 223, 224, 225, 227,
            229, 231, 237, 239, 240, 241, 243, 244, 245, 246, 247, 248, 249, 251, 252, 253, 254, 255));

    //Calculate pixel weight
    private static int pixelWeight(int[][] mask, int x, int y){
        int[] neighbours = new int[8];
        int[] valArray ={128,1,2,4,8,16,32,64};
        int weight=0;

        neighbours[0]=mask[y-1][x-1];
        neighbours[1]=mask[y-1][x];
        neighbours[2]=mask[y-1][x+1];
        neighbours[3]=mask[y][x+1];
        neighbours[4]=mask[y+1][x+1];
        neighbours[5]=mask[y+1][x];
        neighbours[6]=mask[y+1][x-1];
        neighbours[7]=mask[y][x-1];


            for(int i=0;i<8;i++){
                if(neighbours[i]!=0)weight+=valArray[i];
            }

        if(weight>0){
           // System.out.println("DEBUG: " + Arrays.toString(neighbours) + " |  "+Arrays.toString(valArray));
        }
        return weight;
    }

    //Count sticking neighbours that are not 0
    private static boolean countNeighbours(int[][] mask, int x, int y){
        int[] neighbours = new int[8];
        neighbours[0]=mask[y-1][x-1];
        neighbours[1]=mask[y-1][x];
        neighbours[2]=mask[y-1][x+1];
        neighbours[3]=mask[y][x+1];
        neighbours[4]=mask[y+1][x+1];
        neighbours[5]=mask[y+1][x];
        neighbours[6]=mask[y+1][x-1];
        neighbours[7]=mask[y][x-1];
        int counter=0, loc=0, nonZeroCount=0;
        for (int i =0; i<16;i++){
            if(neighbours[i%8]>0){
                loc++;
                if(i<8) nonZeroCount++;
            }else {
                if(loc>counter)counter=loc;
                loc=0;
            }
        }
        if(loc>counter)counter=loc;
        if(nonZeroCount==counter&&(counter == 2 || counter==3||counter==4)) return true;

        return false;
    }

    //Cheking edges of a pixel to test for neighbours
    private static boolean checkEdges(int[][] mask, int x, int y){
        if(mask[y+1][x]==0||mask[y-1][x]==0||mask[y][x+1]==0||mask[y][x-1]==0) return true;
        return false;
    }

    //Cheking edges of a pixel to test for neighbours
    private static boolean checkCorners(int[][] mask, int x, int y){
        if(mask[y+1][x+1]==0||mask[y-1][x-1]==0||mask[y-1][x+1]==0||mask[y+1][x-1]==0) return true;
        return false;
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

    //Zero padding for mask
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

    // Zero padding
    public static BufferedImage addZeroPadding(BufferedImage in) {
        BufferedImage out = new BufferedImage(in.getWidth() + 2, in.getHeight() + 2, in.getType());

        for (int i = 0; i < out.getHeight(); i++) {
            for (int j = 0; j < out.getWidth(); j++) {
                if (i == 0 || i == out.getHeight() - 1 || j == 0 || j == out.getWidth() - 1) {
                    out.setRGB(j, i, toRGB(0, 0, 0));
                } else {
                    out.setRGB(j, i, in.getRGB(j - 1, i - 1));
                }
            }
        }

        return out;
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
