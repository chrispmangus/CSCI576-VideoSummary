
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class imageReader implements Runnable{

	public void run(){
		play();
	}
	
	public imageReader(String fileName){
		this.fileName = fileName;
	}
	
    private  void play(){

	width = 320;
	height = 240;
	delay = 1000/24;
	
	img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

	try {
	    File file = new File(fileName);
	    is = new FileInputStream(file);

	    //long len = file.length();
	    len = width*height*3;

	    // Use a label to display the image
	    frame = new JFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setTitle("Wumpi Player");

	    bytes = new byte[(int)len];
	    label = new JLabel();
	    long tm = System.currentTimeMillis();
	    for(int i=0;i<14400;i++) {
		readBytes();
		newLabel();
		
			tm += delay;
			Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
	    }
	} 
	catch (IOException e) {
	    e.printStackTrace();
	}
	catch (InterruptedException e){
		e.printStackTrace();
	}

    }

    private  void readBytes() {
	try {
	    //byte[] bytes = new byte[(int)len];
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		offset += numRead;
	    }
	    int ind = 0;
	    for(int y = 0; y < height; y++){

		for(int x = 0; x < width; x++){

		    //byte a = 0;
		    byte r = bytes[ind];
		    byte g = bytes[ind+height*width];
		    byte b = bytes[ind+height*width*2]; 

		    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
		    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
		    img.setRGB(x,y,pix);
		    ind++;
		} 
	    }
	} 
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void newLabel() {
	//label = new JLabel(new ImageIcon(img));
	
	label.setIcon(new ImageIcon(img));
	//icon.setImage(img);
	//label.setIcon(icon);
	frame.getContentPane().add(label, BorderLayout.CENTER);
	frame.pack();
	frame.setVisible(true);
    }

    private  String fileName;
    private  int width;
    private  int height;
    private  long len;
    private  InputStream is;
    private  BufferedImage img;
    private  JFrame frame;
    private  JLabel label;
    private  byte[] bytes;
    private  int delay;
}