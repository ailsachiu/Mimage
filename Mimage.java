import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import javax.media.jai.*;

public class Mimage {

  private static int WIDTH;
  private static int HEIGHT;
  private static int CLUSTERS;

  public static void main(String[] args) {
    // Read in command line arguments
    String folderPath = args[0];
    WIDTH = Integer.parseInt(args[1]);
    HEIGHT = Integer.parseInt(args[2]);
    CLUSTERS = Integer.parseInt(args[3]);
    // Grab every image file in the directory
    File folder = new File(folderPath);
    File[] files = folder.listFiles();
    if (files == null) {
      System.out.println("No content found in the given directory.");
      return;
    }
    // Create a buffered image for each file
    ArrayList<Image> images = new ArrayList<Image>();
    for (File file : files) {
      String filePath = folderPath + file.getName();
      ArrayList<BufferedImage> frames = getImages(WIDTH, HEIGHT, filePath);
      for (BufferedImage frame : frames) {
        Image image = new Image(frame, file.getName());
        image.setHistogram();
        images.add(image);
      }
    }
    int[][] differenceMatrix = findDifferences(images);
    for(int i=0; i < images.size(); i++) {
      System.out.print(images.get(i).name + ": ");
      for(int j=0; j < images.size(); j++) {
        System.out.print(differenceMatrix[j][i] + " ");
      }
      System.out.println();
    }
    Clusterizer clusterizer = new Clusterizer(images, differenceMatrix);
    ArrayList<ArrayList<Image>> clusters = clusterizer.getClusters(CLUSTERS);

    int index = 1;
    for (ArrayList<Image> cluster : clusters) {
      JFrame frame = new JFrame("Cluster " + index);
      JPanel container = new JPanel();
      for (Image image : cluster) {
        container.add(new JLabel(new ImageIcon(image.img)));
      }
      JScrollPane pane = new JScrollPane(container);
      frame.getContentPane().add(pane, BorderLayout.CENTER);
      frame.pack();
      frame.setVisible(true);
      index ++;
    }
    // Display each buffered image
    // for (Image image : images) {
    //   displayImage(image);
    // }
  }


  private static int[][] findDifferences(ArrayList<Image> images) {
    int[][] differenceMatrix = new int[images.size()][images.size()];
    for(int i=0; i < images.size(); i++) {
      for(int j=0; j < images.size(); j++) {
        if(i == j)
          continue;
        differenceMatrix[i][j] = getDifference(images.get(i),images.get(j));
      }
    }
    return differenceMatrix;
  }


  private static int getDifference(Image image1, Image image2) {
    Histogram h1 = image1.getHistogram();
    Histogram h2 = image2.getHistogram();

    int[] bins1 = h1.getBins(0);
    int[] bins2 = h2.getBins(0);
    int differenceSum = 0;
    if(bins1.length == bins2.length) {
      for(int i=0; i < bins1.length; i++) {
        differenceSum += Math.abs(bins1[i] - bins2[i]);
      }
    }

    bins1 = h1.getBins(1);
    bins2 = h2.getBins(1);
    if(bins1.length == bins2.length) {
      for(int i=0; i < bins1.length; i++) {
        differenceSum += Math.abs(bins1[i] - bins2[i]);
      }
    }

    bins1 = h1.getBins(2);
    bins2 = h2.getBins(2);
    if(bins1.length == bins2.length) {
      for(int i=0; i < bins1.length; i++) {
        differenceSum += Math.abs(bins1[i] - bins2[i]);
      }
    }

    return differenceSum;
  }


  private static ArrayList<BufferedImage> getImages(int width, int height, String fileName) {
    ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
    try {
      File file = new File(fileName);
      InputStream is = new FileInputStream(file);
      // Get length of file and create byte array
      long len = file.length();
      byte[] bytes = new byte[(int)len];
      // Read all bytes from image file into byte array
      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        offset += numRead;
      }
      // Fill contents of the buffered image
      int ind = 0;
      while (ind+HEIGHT*WIDTH*2 < len) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for(int y = 0; y < height; y++){
          for(int x = 0; x < width; x++){
            byte r = bytes[ind];
            byte g = bytes[ind+height*width];
            byte b = bytes[ind+height*width*2]; 
            int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
            img.setRGB(x,y,pix);
            ind++;
          }
        }
        // Add buffered image to array list
        images.add(img);
        ind += WIDTH*HEIGHT*2;
      }
      return images;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    // Failed to create image
    return null;
  }


  private static void displayImage(Image image) {
    // Use a label to display the image
    JFrame frame = new JFrame(image.name);
    JLabel label = new JLabel(new ImageIcon(image.img));
    frame.getContentPane().add(label, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

}