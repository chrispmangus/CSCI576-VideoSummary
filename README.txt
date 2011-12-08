// CSCI 576 Final Project
// File:        README.txt
// Programmers: Christopher Mangus, Louis Schwartz

This project consists of the following files:

videoSummarize.java			// The driver for the video summarization program
audioAnalyze.java			//  Contains the audio analyzer and the audio/video writer methods
videoSegment.java			//  Computes the shot breaks

videoPlayback.java			// The driver for the audio/video player
imageReader.java			//  The video player
imageReaderComponent.java	//  Extends JComponent 
PlaySound.java				//  The audio player
PlayWaveException.java		//  Extends Exception

We have compiled the source code using a Java SE 6 compliant compiler in Eclipse.

To run the video summarization program:

	java videoSummarize videoInput.576v audioInput.wav percentage
	
 where the percentage is a real number in the range [0,1]. 
 0 indicates 0% compression and 100% of the source file is returned.

 
To run the audio/video player:

	java videoPlayback videoOutput.rgb audioOutput.wav