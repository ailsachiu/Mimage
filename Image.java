import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;
import javax.media.jai.*;
import java.awt.image.renderable.ParameterBlock;

public class Image {
	public BufferedImage img;
  public BufferedImage edgeImg;
	public String name;

	private Histogram hist;

	public Image(BufferedImage img, String name) {
		this.img = img;
		this.name = name;
    setHistograms();
    edgeDetection();
	}

  public void edgeDetection() {
    PlanarImage im0 = PlanarImage.wrapRenderedImage(this.img);
    float data_h[] = new float[] { 1.0F,   0.0F,   -1.0F,
                                   1.414F, 0.0F,   -1.414F,
                                   1.0F,   0.0F,   -1.0F};
    float data_v[] = new float[] {-1.0F,  -1.414F, -1.0F,
                                   0.0F,   0.0F,    0.0F,
                                   1.0F,   1.414F,  1.0F};

    KernelJAI kern_h = new KernelJAI(3,3,data_h);
    KernelJAI kern_v = new KernelJAI(3,3,data_v);
    // Create the Gradient operation.
    PlanarImage im1 = (PlanarImage)JAI.create("gradientmagnitude", im0, kern_h, kern_v);
    this.edgeImg = im1.getAsBufferedImage();
  }

	public void setHistograms() {
		// Set up the parameters for the Histogram object.
    int[] bins = {256, 256, 256};             // The number of bins.
    double[] low = {0.0D, 0.0D, 0.0D};        // The low value.
    double[] high = {256.0D, 256.0D, 256.0D}; // The high value.
	     
    // Construct the Histogram object.
    Histogram hist = new Histogram(bins, low, high);
 		// Create the parameter block.
    ParameterBlock pb = new ParameterBlock();
    pb.addSource(this.img);            // Specify the source image
    pb.add(null);                      // No ROI
    pb.add(1);                         // Sampling
    pb.add(1);                         // periods
    pb.add(bins);
    pb.add(low);
    pb.add(high);
    pb.add(hist);                      // Specify the histogram
    // Perform the histogram operation.
    PlanarImage dst = (PlanarImage)JAI.create("histogram", pb, null);
    // Retrieve the histogram data.
    this.hist = (Histogram) dst.getProperty("histogram");
	}

	public Histogram getHistogram() {
		return this.hist;
	}

}