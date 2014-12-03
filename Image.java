import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import javax.swing.*;

public class Image {
	public BufferedImage img;
	public String name;

	public Image(BufferedImage img, String name) {
		this.img = img;
		this.name = name;
	}
}