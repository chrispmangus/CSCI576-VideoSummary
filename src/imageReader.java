// CSCI 576 Final Project
// File:        imageReader.java
// Programmers: Christopher Mangus, Louis Schwartz

//import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class imageReader implements Runnable{

    public void run(){
	play();
    }

    //public imageReader(String fileName, int vidStartDelay, PlaySound pSound){
    public imageReader(String fileName, PlaySound pSound, int startDelay){
	this.fileName = fileName;
	this.startDelay = startDelay;
	this.playSound = pSound;
    }

    private  void play(){

	//long tm = System.currentTimeMillis();
	//double delay = 1000/FPS;  // delay = milliseconds per frame


	img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

	try {
	    File file = new File(fileName);
	    is = new FileInputStream(file);

	    long len = WIDTH*HEIGHT*3;
	    long numFrames = file.length()/len;

	    JFrame frame = new JFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setTitle("Wumpi Player");
	    frame.setSize(WIDTH,HEIGHT+22);	

	    bytes = new byte[(int)len];

	    imageReaderComponent component = new imageReaderComponent();
	    //tm += delay;

	    // audio Samples Per video Frame
	    double spf = playSound.getSampleRate()/FPS;
	    //System.out.println(spf);

	    // Video Frame offsets to sync audio and video
	    int offset = 5; // only seems to work for Sample 2	
	    //int offset = 0;
	    // Audio ahead of video, roll video forward to catch up
	    int j=0;

	    while(j<Math.round(playSound.getPosition()/spf)) {
		readBytes();
		component.setImg(img);
		frame.add(component);
		frame.repaint();	
		frame.setVisible(true);
		j++;
	    }

	    // Video ahead of audio, wait for audio to catch up
	    while(j>Math.round(offset+playSound.getPosition()/spf)) {

		// Do Nothing
	    }

	    for(int i=j;i<numFrames;i++) {
		//tm = System.currentTimeMillis();

		// Video ahead of audio, wait for audio to catch up
		while(i>Math.round(offset+playSound.getPosition()/spf)) {
		    // Do Nothing
		}

		// Audio ahead of video, roll video forward to catch up
		while(i<Math.round(playSound.getPosition()/spf)) {
		    readBytes();
		    component.setImg(img);
		    frame.add(component);
		    frame.repaint();	
		    frame.setVisible(true);	
		    i++;
		}
		readBytes();
		component.setImg(img);
		frame.add(component);
		frame.repaint();
		frame.setVisible(true);		
		//tm += delay;
		//Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
	    }
	} 
	catch (IOException e) {
	    e.printStackTrace();
	}
	/*
	catch (InterruptedException e){
	    e.printStackTrace();
	}
	 */
    }

    private  void readBytes() {
	try {
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		offset += numRead;
	    }
	    int ind = 0;
	    for(int y = 0; y < HEIGHT; y++){
		for(int x = 0; x < WIDTH; x++){
		    byte r = bytes[ind];
		    byte g = bytes[ind+HEIGHT*WIDTH];
		    byte b = bytes[ind+HEIGHT*WIDTH*2]; 

		    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
		    img.setRGB(x,y,pix);
		    ind++;
		} 
	    }
	} 
	catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private int startDelay;
    private PlaySound playSound;
    private String fileName;
    private final int WIDTH = 320;
    private final int HEIGHT = 240;
    //private final double FPS = 23.976; // Frames Per Second
    private final double FPS = 24; // Frames Per Second
    private InputStream is;
    private BufferedImage img;
    private byte[] bytes;
}
