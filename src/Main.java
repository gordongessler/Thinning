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
        File f = new File("test2.png");
        BufferedImage in = ImageIO.read(f);
        ImageIO.write( k3m(threshold(in, 1)), "png", new File("outputK3M.png"));
        ImageIO.write( kmm(threshold(in, 1)), "png", new File("outputKMM.png"));
    }

    //TODO: KMM

    private static BufferedImage k3m(BufferedImage in){

        int[][] image = markPixels(in);
        int width = image.length, height = image[0].length;

        int oldLenght=0;
        boolean flag = true;
        while (flag){
            ArrayList<Position> borderPix = border(image,a0);
           // System.out.println(borderPix.toString());
            borderPix=phase(image,borderPix,a1);
            borderPix=phase(image,borderPix,a2);
            borderPix=phase(image,borderPix,a3);
            borderPix=phase(image,borderPix,a4);
            borderPix=phase(image,borderPix,a5);
           // System.out.println(oldLenght+"|"+borderPix.size());
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

    private static BufferedImage kmm(BufferedImage in){
        int[][] image = markPixels(in);
        int width = image.length, height = image[0].length;
        boolean flag = true;
        int[][] old = new int[width][height];
        while (flag) {
            image = markContourPixels(image);
            image = markCornerPixels(image);
            image = markOutlinePixels(image);
            image = removePixel(image, 4);
            image = removePixelWithCheck(image, 2);
            image = removePixelWithCheck(image, 3);
            if(Arrays.deepEquals(image,old)) flag=false;
            arrayCopy(image,old);
        }
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

    public static void arrayCopy(int[][] aSource, int[][] aDestination) {
        for (int i = 0; i < aSource.length; i++) {
            System.arraycopy(aSource[i], 0, aDestination[i], 0, aSource[i].length);
        }
    }

    private static int[][] removePixelWithCheck(int[][] image, int i) {
        int width=image.length, height=image[0].length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int weight = pixelWeightKMM(image,x,y);
                if (image[x][y]==i){
                    if(deletionArray.contains(weight)) {
                        image[x][y] = 0;
                    }else{
                        image[x][y] = 1;
                    }
                }
            }
        }
        return image;
    }

    private static int[][] removePixel(int[][] image, int i) {
        int width=image.length, height=image[0].length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (image[x][y]==i){
                    image[x][y]=0;
                }
            }
        }
        return image;
    }

    //Calculate pixel weight
    private static int pixelWeightKMM(int[][] image, int x, int y){
        int[] neighbours = new int[8];
        int[] valArray ={128,1,2,4,8,16,32,64};
        image=addMaskZeroPadding(image);
        x=x+1;y=y+1;
        int weight=0;
        neighbours[0]=image[x-1][y-1];
        neighbours[1]=image[x-1][y];
        neighbours[2]=image[x-1][y+1];
        neighbours[3]=image[x][y+1];
        neighbours[4]=image[x+1][y+1];
        neighbours[5]=image[x+1][y];
        neighbours[6]=image[x+1][y-1];
        neighbours[7]=image[x][y-1];
        for(int i=0;i<8;i++){
            if(neighbours[i]!=0)weight+=valArray[i];
        }
        return weight;
    }

    private static int[][] markOutlinePixels(int[][] image) {
        int width=image.length, height=image[0].length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (image[x][y]==2||image[x][y]==3){
                    int neighbourCount=countNeighbours(image,x,y);
                    if ((neighbourCount == 2 || neighbourCount == 3 || neighbourCount == 4)&&outlinePixelWeights.contains(pixelWeightKMM(image,x,y))) {
                        image[x][y] = 4;
                    }
                }
            }
        }
        return image;
    }

    //KMM Utility

    private static int countNeighbours(int[][] image, int x, int y) {
        int neighbourCount =0;
        for (int i = -1; x <= 1; x++) {
            for (int j = -1; y <= 1; y++) {
                if (image[x+i][y+j]>0&&(x!=0||y!=0)){
                    neighbourCount++;
                }
            }
        }
        return neighbourCount;
    }


    private static int[][] markPixels(BufferedImage in){
        int width = in.getWidth(), height = in.getHeight();
        int[][] image = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int val = getG(in.getRGB(x, y));
                image[x][y] = val == 0 ? 1 : 0;
            }
        }
        return image;
    }

   private static int[][] markContourPixels(int[][] image){
       int width=image.length, height=image[0].length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (image[x][y]==1){
                    if (checkEdges(addMaskZeroPadding(image), x+1, y+1)) {
                        image[x][y] = 2;
                    }
                }
            }
        }
        return image;
    }

    //Cheking edges of a pixel to test for neighbours
    private static boolean checkEdges(int[][] image, int x, int y){
        if(image[x+1][y]==0||image[x-1][y]==0||image[x][y+1]==0||image[x][y-1]==0) return true;
        return false;
    }

    private static int[][] markCornerPixels(int[][] image){
        int width=image.length, height=image[0].length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (image[x][y]==1){
                    if (checkCorners(addMaskZeroPadding(image), x+1, y+1)) {
                        image[x][y] = 3;
                    }
                }
            }
        }
        return image;
    }

    //Cheking edges of a pixel to test for neighbours
    private static boolean checkCorners(int[][] mask, int x, int y){
        if(mask[x+1][y+1]==0||mask[x-1][y-1]==0||mask[x-1][y+1]==0||mask[x+1][y-1]==0) return true;
        return false;
    }

    private static HashSet<Integer> deletionArray = new HashSet<>(Arrays.asList(
            3, 5, 7, 12, 13, 14, 15, 20, 21, 22, 23, 28, 29, 30, 31, 48,
            52, 53, 54, 55, 56, 60, 61, 62, 63, 65, 67, 69, 71, 77, 79,
            80, 81, 83, 84, 85, 86, 87, 88, 89, 91, 92, 93, 94, 95, 97,
            99, 101, 103, 109, 111, 112, 113, 115, 116, 117, 118, 119,
            120, 121, 123, 124, 125, 126, 127, 131, 133, 135, 141, 143,
            149, 151, 157, 159, 181, 183, 189, 191, 192, 193, 195, 197,
            199, 205, 207, 208, 209, 211, 212, 213, 214, 215, 216, 217,
            219, 220, 221, 222, 223, 224, 225, 227, 229, 231, 237, 239,
            240, 241, 243, 244, 245, 246, 247, 248, 249, 251, 252, 253, 254, 255
    ));

    private static HashSet<Integer> outlinePixelWeights = new HashSet<Integer>(Arrays.asList(
            3, 6, 7, 12, 4, 15, 24, 28, 30, 48, 56, 60, 96,
            112, 120, 129, 131, 135, 192, 193, 195, 224, 225
    ));


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

    private static HashSet<Integer> a1pix = new HashSet<>(Arrays.asList(
            3, 6, 7, 12, 14, 15, 24, 28, 30, 31, 48, 56,
            60, 62, 63, 96, 112, 120, 124, 126, 127, 129, 131,
            135, 143, 159, 191, 192, 193, 195, 199, 207, 223,
            224, 225, 227, 231, 239, 240, 241, 243, 247, 248,
            249, 251, 252, 253, 254
    ));



    //Utility

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

    //Printing the mask (2D array)
    private static void printMask(int[][] mask){
        //int[][] image = new int[width][height];
        int width=mask.length, height=mask[0].length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel=mask[x][y];
                //Comment the line below to see the mask with 0s in it
                if (pixel==0) {System.out.print(" ");} else
                    System.out.print(pixel);
            }
            System.out.println();
        }
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
