import java.util.*;

public class Clusterizer {

  ArrayList<Image> images;
  int[][] matrix;

  public Clusterizer(ArrayList<Image> images, int[][] matrix) {
    this.images = images;
    this.matrix = matrix;
  }

  public ArrayList<ArrayList<Image>> getClusters(int clusters) {
    ArrayList<ArrayList<Integer>> groupings = getIndices(clusters);
    // create clusters of images
    ArrayList<ArrayList<Image>> imageClusters = new ArrayList<ArrayList<Image>>();
    for (ArrayList<Integer> group : groupings) {
      System.out.println("NEW GROUP:");
      ArrayList<Image> imageGroup = new ArrayList<Image>();
      for (int index : group) {
        System.out.print(index + " ");
        imageGroup.add(images.get(index));
      }
      System.out.println();
      imageClusters.add(imageGroup);
    }
    System.out.println("TOTAL GROUPS: " + groupings.size());
    return imageClusters;
  }

  public ArrayList<ArrayList<Integer>> getIndices(int clusters) {
    // keep track of the indices used for our clusters
    ArrayList<Integer> indices = new ArrayList<Integer>();
    // go through k iterations to get k clusters
    for (int k = 0; k < clusters; k++) {
      // keep track of max difference found in matrix
      int max = 0;
      int firstIndex = 0;
      int secondIndex = 0;
      // search through entire matrix to find max distance
      for (int i = 0; i < matrix.length; i++) {
        for (int j = 0; j < matrix.length; j++) {
          if (matrix[i][j] > max) {
            // check if the indices in this pairing have already been used
            if (indices.contains(i) && indices.contains(j))
              continue;
            // update max value and indices
            max = matrix[i][j];
            firstIndex = i;
            secondIndex = j;
          }
        }
      }
      System.out.println("MAXXXXXX of " + max);
      // add indices to arraylist
      if (!indices.contains(firstIndex))
        indices.add(firstIndex);
      if (!indices.contains(secondIndex))
        indices.add(secondIndex);
    }

    // build clusters
    ArrayList<ArrayList<Integer>> groupings = new ArrayList<ArrayList<Integer>>();
    for (int index : indices) {
      ArrayList<Integer> group = new ArrayList<Integer>();
      group.add(index);
      groupings.add(group);
    }

    // add each element to their respective cluster
    for (int i = 0; i < matrix.length; i++) {
      // skip value if it is used as a cluster head
      if (indices.contains(i))
        continue;
      int minValue = Integer.MAX_VALUE;
      int minCluster = 0;
      for (int clusterHead : indices) {
        // skip value if it is equal to the cluster head
        if (clusterHead == i)
          continue;
        if (matrix[i][clusterHead] < minValue) {
          minValue = matrix[i][clusterHead];
          minCluster = clusterHead;
        }
      }
      // add to a grouping
      for (ArrayList<Integer> group : groupings) {
        if (group.get(0) == minCluster) {
          group.add(i);
        }
      }
    }
    return groupings;
  }

}