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

  public static void main(String[] args) {
    // Read in command line arguments
    String folderPath = args[0];
    WIDTH = Integer.parseInt(args[1]);
    HEIGHT = Integer.parseInt(args[2]);
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
      images.add(new Image(getImage(WIDTH, HEIGHT, filePath),file.getName()));
    }
    // Display each buffered image
    for (Image image : images) {
      image.setHistogram();
      displayImage(image);
    }

  }

  private static BufferedImage getImage(int width, int height, String fileName) {
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
      // Return buffered image
      return img;
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