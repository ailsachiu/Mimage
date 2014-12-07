import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import javax.media.jai.*;

public class Mimage {

  private static int WIDTH;
  private static int HEIGHT;
  private static ArrayList<Image> content;

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
    content = new ArrayList<Image>();
    for (File file : files) {
      String filePath = folderPath + file.getName();
      ArrayList<BufferedImage> frames = getImages(WIDTH, HEIGHT, filePath);
      for (BufferedImage frame : frames) {
        Image image = new Image(frame, file.getName());
        image.setHistogram();
        content.add(image);
      }
    }
    // Display image clusters
    ArrayList<ArrayList<Image>> clusters = getImageClusters(content, content.size()/8);
    displayGrid(clusters);
  }

  private static BufferedImage getScaledImage(BufferedImage original, int w, int h){
    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
    Graphics2D g2 = resizedImg.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(original, 0, 0, w, h, null);
    g2.dispose();
    return resizedImg;
  }

  private static ArrayList<ArrayList<Image>> getImageClusters(ArrayList<Image> images, int clusterSize) {
    if (images.size() < clusterSize) {
      ArrayList<ArrayList<Image>> cluster = new ArrayList<ArrayList<Image>>();
      cluster.add(images);
      return cluster;
    }
    int[][] differences = findDifferences(images);
    Clusterizer clusterizer = new Clusterizer(images, differences);
    ArrayList<ArrayList<Image>> clusters = clusterizer.getClusters(2);
    ArrayList<ArrayList<Image>> finalClusters = new ArrayList<ArrayList<Image>>();
    for (ArrayList<Image> cluster : clusters) {
      finalClusters.addAll(getImageClusters(cluster, clusterSize));
    }
    return finalClusters;
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
    // get Red bin from histograms
    int[] bins1 = h1.getBins(0);
    int[] bins2 = h2.getBins(0);
    int differenceSum = 0;
    if(bins1.length == bins2.length) {
      for(int i=0; i < bins1.length; i++) {
        differenceSum += Math.abs(bins1[i] - bins2[i]);
      }
    }
    // get Green bin from histograms
    bins1 = h1.getBins(1);
    bins2 = h2.getBins(1);
    if(bins1.length == bins2.length) {
      for(int i=0; i < bins1.length; i++) {
        differenceSum += Math.abs(bins1[i] - bins2[i]);
      }
    }
    // get Blue bin from histograms
    bins1 = h1.getBins(2);
    bins2 = h2.getBins(2);
    if(bins1.length == bins2.length) {
      for(int i=0; i < bins1.length; i++) {
        differenceSum += Math.abs(bins1[i] - bins2[i]);
      }
    }
    // return total histogram difference
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

  private static void displayGrid(ArrayList<ArrayList<Image>> clusters) {
    JFrame frame = new JFrame("Mīmâgé");
    JPanel container = new JPanel(new GridLayout(0, 6, 10, 10));
    container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
    for (ArrayList<Image> cluster : clusters) {
      BufferedImage scaledImg = getScaledImage(cluster.get(0).img, WIDTH/2, HEIGHT/2);
      JLabel label = new JLabel(new ImageIcon(scaledImg));
      container.add(label);
      label.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent evt) {
          if (cluster.size() > clusters.size()) {
            displayGrid(getImageClusters(cluster, cluster.size()/8));
          }
          else if (cluster.size() > 1) {
            String clusterName = "Cluster " + (clusters.indexOf(cluster)+1);
            displayCluster(cluster, clusterName);
          } else {
            displayImage(cluster.get(0));
          }
        }
      });
    }
    // JScrollPane pane = new JScrollPane(container);
    frame.getContentPane().add(container, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  private static void displayCluster(ArrayList<Image> cluster, String name) {
    JFrame frame = new JFrame(name);
    JPanel container = new JPanel(new GridLayout(0, 6, 10, 10));
    container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    for (Image image : cluster) {
      BufferedImage scaledImg = getScaledImage(image.img, WIDTH/2, HEIGHT/2);
      JLabel label = new JLabel(new ImageIcon(scaledImg));
      container.add(label);
      label.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent evt) {
          displayImage(image);
        }
      });
    }
    // JScrollPane pane = new JScrollPane(container);
    frame.getContentPane().add(container, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  private static void displayImage(Image image) {
    ArrayList<Image> videoFrames = new ArrayList<Image>();
    for (Image item : content) {
      if (item.name.equals(image.name)) {
        videoFrames.add(item);
      }
    }
    if (videoFrames.size() > 1) {
      displayVideo(videoFrames);
      return;
    }
    // Use a label to display the image
    JFrame frame = new JFrame(image.name);
    JLabel label = new JLabel(new ImageIcon(image.img));
    frame.getContentPane().add(label, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  private static void displayVideo(ArrayList<Image> videoFrames) {
    System.out.println("Displaying video " + videoFrames.get(0).name + " with # of frames: " + videoFrames.size());
    JFrame frame = new JFrame(videoFrames.get(0).name);
    JLabel label = new JLabel(new ImageIcon(videoFrames.get(0).img));
    frame.getContentPane().add(label, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
    // javax.swing.Timer timer = new javax.swing.Timer(1000/30, new ActionListener() {
    //   public void actionPerformed(ActionEvent e) {
    //     if (count < videoFrames.size()) {
    //       label.setIcon(new ImageIcon(videoFrames.get(count).img));
    //       count ++;
    //     }
    //   }
    // });
    for (int i = 1; i < videoFrames.size(); i++) {
      label.setIcon(new ImageIcon(videoFrames.get(i).img));
      try {
        Thread.sleep(1000/30); 
      } catch (InterruptedException e) {
        System.out.println("exception coughtt");
        Thread.currentThread().interrupt();
      }
    }
  }

}