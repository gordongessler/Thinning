import com.sun.deploy.util.ArrayUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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

    private static void kmm(BufferedImage in){

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
