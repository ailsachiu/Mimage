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
	public String name;

	private Histogram hist;

	public Image(BufferedImage img, String name) {
		this.img = img;
		this.name = name;
	}

	public void setHistogram() {
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