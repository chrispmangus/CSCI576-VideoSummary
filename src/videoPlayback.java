// CSCI 576 Final Project
// File:        videoPlayback.java
// Programmers: Christopher Mangus, Louis Schwartz

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * The driver for the audio/video player.
 * Used some skeleton code provided by the TAs as a basis for this driver.
 * @author Christopher Mangus     
 * @author Louis Schwartz
 */
public class videoPlayback {

    /**
     * Main method for videoPlayback
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	try {
	    // get the command line parameters
	    if (args.length < 2) {
		System.err.println("usage: java videoPlayback video.rgb audio.wav");
		return;
	    }
	    String vfilename = args[0];
	    String afilename = args[1];	 

	    // opens the inputStream
	    FileInputStream inputStream = new FileInputStream(afilename);

	    // initializes the playSound and imageReader Objects
	    PlaySound playSound = new PlaySound(inputStream);
	    imageReader imageReader = new imageReader(vfilename, playSound);

	    Thread t1 = new Thread(playSound);
	    Thread t2 = new Thread(imageReader);

	    t1.start();
	    t2.start();
	}
	catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
    }
}
