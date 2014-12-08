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
  private static int BIN_SIZE = 32;
  private static int BLOCK_SIZE = 8;
  private static ArrayList<Image> content;
  private static ArrayList<Image> videoContent;

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
    videoContent = new ArrayList<Image>();
    for (File file : files) {
      String filePath = folderPath + file.getName();
      ArrayList<BufferedImage> frames = getImages(WIDTH, HEIGHT, filePath);
      // Perform different actions for image vs video
      if (frames.size() > 1) {
        // Run separate clustering for video
        ArrayList<Image> video = new ArrayList<Image>();
        for (BufferedImage frame : frames) {
          Image image = new Image(frame, file.getName());
          video.add(image);
          videoContent.add(image);
        }
        ArrayList<ArrayList<Image>> videoClusters = getImageClusters(video, video.size()/4);
        for (ArrayList<Image> cluster : videoClusters) {
          content.add(cluster.get(0));
        }
      } else if (frames.size() == 1){
        // Just an image, add to list of images
        Image image = new Image(frames.get(0), file.getName());
        content.add(image);
      }
    }
    // Display image clusters
    ArrayList<ArrayList<Image>> clusters = getImageClusters(content, content.size()/8);
    displayGrid(clusters);
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
    double[][] differences = findDifferences(images);
    Clusterizer clusterizer = new Clusterizer(images, differences);
    ArrayList<ArrayList<Image>> clusters = clusterizer.getClusters(2);
    ArrayList<ArrayList<Image>> finalClusters = new ArrayList<ArrayList<Image>>();
    for (ArrayList<Image> cluster : clusters) {
      finalClusters.addAll(getImageClusters(cluster, clusterSize));
    }
    return finalClusters;
  }

  private static double[][] findDifferences(ArrayList<Image> images) {
    double[][] differenceMatrix = new double[images.size()][images.size()];
    for(int i=0; i < images.size(); i++) {
      for(int j=0; j < images.size(); j++) {
        if(i == j)
          continue;
        differenceMatrix[i][j] = getDifference(images.get(i),images.get(j));
        // differenceMatrix[i][j] += getEdgeDifference(images.get(i), images.get(j));
      }
    }
    return differenceMatrix;
  }

  private static double getDifference(Image image1, Image image2) {
    Histogram h1 = image1.getHistogram();
    Histogram h2 = image2.getHistogram();
    // get Red bin from histograms
    int[] bins1 = h1.getBins(0);
    int[] bins2 = h2.getBins(0);
    double differenceSum = 0;
    for (int i = 0; i < bins1.length; i += BIN_SIZE) {
      int binCount1 = 0;
      int binCount2 = 0;
      for (int j = i; j < i + BIN_SIZE; j++) {
        binCount1 += bins1[j];
        binCount2 += bins2[j];
      }
      double normal1 = (double)binCount1/(double)(HEIGHT*WIDTH);
      double normal2 = (double)binCount2/(double)(HEIGHT*WIDTH);
      differenceSum += Math.abs(binCount1 - binCount2);
    }
    // get Green bin from histograms
    bins1 = h1.getBins(1);
    bins2 = h2.getBins(1);
    for (int i = 0; i < bins1.length; i += BIN_SIZE) {
      int binCount1 = 0;
      int binCount2 = 0;
      for (int j = i; j < i + BIN_SIZE; j++) {
        binCount1 += bins1[j];
        binCount2 += bins2[j];
      }
      double normal1 = (double)binCount1/(double)(HEIGHT*WIDTH);
      double normal2 = (double)binCount2/(double)(HEIGHT*WIDTH);
      differenceSum += Math.abs(binCount1 - binCount2);
    }
    // get Blue bin from histograms
    bins1 = h1.getBins(2);
    bins2 = h2.getBins(2);
    for (int i = 0; i < bins1.length; i += BIN_SIZE) {
      int binCount1 = 0;
      int binCount2 = 0;
      for (int j = i; j < i + BIN_SIZE; j++) {
        binCount1 += bins1[j];
        binCount2 += bins2[j];
      }
      double normal1 = (double)binCount1/(double)(HEIGHT*WIDTH);
      double normal2 = (double)binCount2/(double)(HEIGHT*WIDTH);
      differenceSum += Math.abs(binCount1 - binCount2);
    }
    return differenceSum;
  }

  private static double getEdgeDifference(Image image1, Image image2) {
    BufferedImage edge1 = image1.edgeImg;
    BufferedImage edge2 = image2.edgeImg;
    // get Red bin from histograms
    double differenceSum = 0;
    for (int i = 0; i < WIDTH; i += BLOCK_SIZE) {
      for (int j = 0; j < HEIGHT; j += BLOCK_SIZE) {
        int count1 = 0;
        int count2 = 0;
        for (int k = i; k < i + BLOCK_SIZE; k++) {
          for (int l = j; l < j + BLOCK_SIZE; l++) {
            Color pixel1 = new Color(edge1.getRGB(k, l));
            Color pixel2 = new Color(edge2.getRGB(k, l));
            if (pixel1.getRed() > 64 || pixel1.getGreen() > 64 || pixel1.getBlue() > 64) {
              count1 ++;
            }
            if (pixel2.getRed() > 64 || pixel2.getGreen() > 64 || pixel2.getBlue() > 64) {
              count2 ++;
            }
          }
        }
        double normal1 = (double)count1/(double)(BLOCK_SIZE*BLOCK_SIZE);
        double normal2 = (double)count2/(double)(BLOCK_SIZE*BLOCK_SIZE);
        differenceSum += Math.abs(normal1 - normal2);
      }
    }
    // return total edge difference
    System.out.println(image1.name + " " + image2.name + " " + differenceSum);
    return differenceSum;
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
    frame.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent ke) {
        if(ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
          frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
      }
    });
    frame.getContentPane().add(container, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  private static void displayCluster(ArrayList<Image> cluster, String name) {
    JFrame frame = new JFrame(name);
    frame.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent ke) {
        if(ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
          frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
      }
    });
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
    for (Image item : videoContent) {
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
    frame.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent ke) {
        if(ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
          frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
      }
    });
    frame.getContentPane().add(label, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }

  private static void displayVideo(ArrayList<Image> videoFrames) {
    JFrame frame = new JFrame(videoFrames.get(0).name);
    frame.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent ke) {
        if(ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
          frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        }
      }
    });
    JLabel label = new JLabel(new ImageIcon(videoFrames.get(0).img));
    frame.getContentPane().add(label, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
    javax.swing.Timer t = new javax.swing.Timer(1000/30, new ActionListener() {
      int count = 1;
      @Override
      public void actionPerformed(ActionEvent e) {
        if (count < videoFrames.size()) {
          label.setIcon(new ImageIcon(videoFrames.get(count).img));
          count ++;
        }
      }
    });
    t.start();
  }

}