// CSCI 576 Final Project
// File:        videoSummarize.java
// Programmers: Christopher Mangus, Louis Schwartz

import java.util.ArrayList;

/**
 * The driver for the video summarization algorithms.
 * @author Christopher Mangus     
 * @author Louis Schwartz
 *
 */
public class videoSummarize {

    /**
     * Main method for videoSummarize
     * @param args Command line arguments
     */
    public static void main(String[] args) {
	try {
	    if (args.length < 3) {
		System.err.println("usage: java videoSummarize videoInput.rgb audioInput.wav percentage");
		return;
	    }
	    String vFileName = args[0];
	    String aFileName = args[1];	
	    double percent = Double.parseDouble(args[2]);

	    audioAnalyze aa = new audioAnalyze(vFileName,aFileName,percent);
	    ArrayList<Integer> shots = new ArrayList<Integer>();
	    shots = aa.calcAudioWeights();
	    aa.writeVideo(shots);
	    aa.writeAudio(shots);

	    System.out.println("Summarization Complete!");
	}	

	catch (PlayWaveException e) {
	    e.printStackTrace();
	    return;
	}
    }
}