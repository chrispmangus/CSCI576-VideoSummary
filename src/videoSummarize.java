// CSCI 576 Final Project
// File:        videoSummarize.java
// Programmers: Christopher Mangus, Louis Schwartz

import java.util.ArrayList;
import java.util.LinkedList;


public class videoSummarize {

    /**
     * Main method for videoSummarize
     * 
     * @param args
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

	    /*video segmenting code
	    videoSegment vs = new videoSegment(vFileName);
	    vs.analyze();
	    if you want to see the breaks yourself
	    	vs.printBreaks();
	    some array list = vs.getBreaks(); */

	    audioAnalyze aa = new audioAnalyze(vFileName,aFileName,percent);
	    ArrayList<Integer> shots = new ArrayList<Integer>();
	    shots = aa.calcAudioWeights();
	    aa.writeVideo(shots);
	    aa = new audioAnalyze(vFileName,aFileName,percent);
	    aa.writeAudio(shots);

	    System.out.println("done");
	}	

	catch (PlayWaveException e) {
	    e.printStackTrace();
	    return;
	}
    }
}