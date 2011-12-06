// CSCI 576 Final Project
// File:        videoPlayback.java
// Programmers: Christopher Mangus, Louis Schwartz

//import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//import java.io.IOException;

/**
 * plays a wave file using PlaySound class
 * 
 * @author Giulio
 */
public class videoPlayback {

    public static void main(String[] args) {
	try {
	    // get the command line parameters
	    if (args.length < 2) {
		System.err.println("usage: java videoPlayback video.rgb audio.wav");
		return;
	    }
	    String vfilename = args[0];
	    String afilename = args[1];	 


	    int aDelay = 0;
	    int vDelay = 0;

	    // opens the inputStream
	    FileInputStream inputStream = new FileInputStream(afilename);

	    // initializes the playSound and imageReader Objects
	    PlaySound playSound = new PlaySound(inputStream, aDelay);
	    imageReader imageReader = new imageReader(vfilename, playSound, vDelay);

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
