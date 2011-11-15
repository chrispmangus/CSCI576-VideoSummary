// CSCI 576 Final Project
// File:        PlayWaveFile.java
// Programmers: Christopher Mangus, Louis Schwartz

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//import java.io.IOException;

/**
 * plays a wave file using PlaySound class
 * 
 * @author Giulio
 */
public class PlayWaveFile {

    /**
     * <Replace this with one clearly defined responsibility this method does.>
     * 
     * @param args
     *            the name of the wave file to play
     */
    public static void main(String[] args) {
	try {
	    // get the command line parameters
	    if (args.length < 1) {
		System.err.println("usage: java -jar PlayWaveFile.jar [filename]");
		return;
	    }
	    String afilename = args[0];
	    String vfilename = args[1];	    
	    
	    // Start delay in milliseconds
	    int vidStartDelay = 0;
	    int audStartDelay = 0;

	    // opens the inputStream
	    FileInputStream inputStream = new FileInputStream(afilename);	    

	    // initializes the playSound Object
	    PlaySound playSound = new PlaySound(inputStream, audStartDelay);
	    imageReader imageReader = new imageReader(vfilename, vidStartDelay);

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
