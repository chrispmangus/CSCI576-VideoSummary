
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class imageReader {


    public static void main(String[] args) {


	String fileName = args[0];
	int width = Integer.parseInt(args[1]);
	int height = Integer.parseInt(args[2]);

	BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	try {
	    File file = new File(fileName);
	    InputStream is = new FileInputStream(file);

	    //long len = file.length();
	    long len = width*height*3;
	    
	    byte[] bytes = new byte[(int)len];

	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		offset += numRead;
	    }


	    int ind = 0;
	    for(int y = 0; y < height; y++){

		for(int x = 0; x < width; x++){

		    byte a = 0;
		    byte r = bytes[ind];
		    byte g = bytes[ind+height*width];
		    byte b = bytes[ind+height*width*2]; 

		    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
		    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
		    img.setRGB(x,y,pix);
		    ind++;
		}
	    }


	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	// Use a label to display the image
	JFrame frame = new JFrame();
	JLabel label = new JLabel(new ImageIcon(img));
	frame.getContentPane().add(label, BorderLayout.CENTER);
	frame.pack();
	frame.setVisible(true);

    }
    

}