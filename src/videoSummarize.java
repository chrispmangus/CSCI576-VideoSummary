// CSCI 576 Final Project
// File:        videoSummarize.java
// Programmers: Christopher Mangus, Louis Schwartz

import java.io.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.LineUnavailableException;
//import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
//import javax.sound.sampled.DataLine.Info;

//import javax.swing.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;

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

	    writeVideo(vFileName, audioSumm(aFileName, percent));

	    System.out.println("done");
	}	

	catch (PlayWaveException e) {
	    e.printStackTrace();
	    return;
	}
    }

    /**
     * This is the primary method for analyzing the wav and writing a new wav file.
     * @param aFileName  The audio file new
     * @param percent  The percentage of video to retain after summarization
     * @return  An ArrayList that contains the locations of each peak value, as well as the associated pre and post roll
     * @throws PlayWaveException
     */
    private static ArrayList<Long> audioSumm(String aFileName, double percent) throws PlayWaveException {

	// ArrayList keeps track of the locations of the peak values. Units of FRAMES.
	// Format: Peak location, Pre-roll length, Post-roll length, ... , Peak location, Pre-roll length, Post-roll length, ...
	ArrayList<Long> peakLocations = new ArrayList<Long>();	 

	try {	    
	    int headerLen = 46; 							// Length of header in bytes
	    File soundFile = new File(aFileName);
	    // RandomAccessFile audioInputStream = new RandomAccessFile(soundFile,"r");

	    InputStream inputStream = new FileInputStream(soundFile);

	    /*
	    // Print Header byte info. Will throw an exception for the rest of the method.
	    byte[] header = new byte[headerLen];
	    inputStream.read(header, 0, headerLen);
	    for(int i=0;i<headerLen;i++) {
		if(i%10==0) {
		    System.out.println();		    
		}
		System.out.print(header[i]+" ");
	    }
	    System.out.println();
	     */

	    //	    RandomAccessFile audioInputStream = new RandomAccessFile(soundFile,"r");


	    InputStream waveStream = inputStream;
	    InputStream bufferedIn = new BufferedInputStream(waveStream);
	    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
	    AudioFormat audioFormat = audioInputStream.getFormat();

	    //File soundFile = new File(aFileName);
	    long totNumBytes = soundFile.length(); 					// Total original audio file length in bytes
	    int numChan = audioFormat.getChannels();					// Number of Channels (= 1)
	    double bytesPerSample = audioFormat.getFrameSize();				// Number of bytes per sample (= 2)
	    double sampleRate = audioFormat.getFrameRate();					// Sample Rate (= 22050 Hz)
	    double bytesPerSecond = sampleRate*bytesPerSample;				// Number bytes per second (= 44100 Hz)
	    long totNumSamples = (long)((totNumBytes-headerLen)/(bytesPerSample*numChan));	// Total original number of audio samples
	    int numSamplesReq = (int)(totNumSamples*percent);  // Number of Samples Required by percentage
	    int numBytesReq = (int)numSamplesReq*2;            // Number of Bytes Required by percentage

	    bytesPerVidFrame = bytesPerSample*sampleRate*(1/FPS);
	    vidFramesPerByte = 1/bytesPerVidFrame;

	    int readBytes = 0;
	    byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
	    byte[] audioBufferOld = new byte[EXTERNAL_BUFFER_SIZE];
	    OutputStream outputStream = new FileOutputStream("audioOutput.wav");

	    //buildHeader(outputStream, numBytesReq, headerLen, audioFormat);
	    buildHeader(outputStream, totNumBytes, headerLen, audioFormat);

	    int rollSamples = (int)sampleRate; // The pre/post roll is the number of samples that are written before/after the target byte	    
	    int threshold = 80; 		 // Amplitude threshold
	    long currentPos=1;
	    //long byteCount = 0;
	    int advanceBytes = 0; // Number of bytes to skip in a future buffer

	    int roll = rollSamples*2; // Convert samples to bytes
	    //while(numBytesReq>0) {

	    boolean firstIter = true; // True if we are reading from the first buffer iteration
	    while (readBytes != -1) {
		audioBufferOld=audioBuffer;
		readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);

		//if((readBytes >= 0)&&(numBytesReq>0)) {
		if(readBytes >= 0) {  

		    // Checks most significant byte for amplitude. 
		    //  If greater than threshold, a pre roll and post roll to the peak are copied to the new wav file.
		    for(int i=1;i<readBytes;i+=2) { // Each sample is 2 bytes, MSB is second byte

			// Add the rest of the previous clip from this (the future) buffer
			if(advanceBytes!=0) {
			    while((i<advanceBytes)&&(i<readBytes)) {
				outputStream.write(audioBuffer[i-1]);
				outputStream.write(audioBuffer[i]);
				i+=2;
			    }
			    advanceBytes=0;		
			}

			if(audioBuffer[i]>=threshold) {
			    //double delta = Math.round(Math.round(i/bytesPerVidFrame)*bytesPerVidFrame)-i;

			    peakLocations.add(bytesToFrames(currentPos-1)); // Subtract one to align with the first byte of audio.

			    // If the pre-roll extends beyond the beginning of the audio file
			    if(firstIter&&((i-roll)<0)) {
				for(int j=1;j<=(i+roll);j+=2) {
				    outputStream.write(audioBuffer[j-1]);
				    outputStream.write(audioBuffer[j]);
				    //byteCount+=2;
				}
				numBytesReq -= ((i+1)+roll); // +1 to even number of bytes
				peakLocations.add(bytesToFrames(i));
				peakLocations.add(bytesToFrames(roll));
			    }

			    // If the pre-roll extends into the previous buffer
			    else if((i-roll)<0) {
				for(int j=(audioBuffer.length-(roll-i)); j<audioBuffer.length; j+=2) {
				    outputStream.write(audioBufferOld[j-1]);
				    outputStream.write(audioBufferOld[j]);
				    //byteCount+=2;

				}
				for(int j=1;j<=(i+roll);j+=2) {
				    outputStream.write(audioBuffer[j-1]);
				    outputStream.write(audioBuffer[j]);
				    //byteCount+=2;

				}
				numBytesReq -= (2*roll+2); //+2 to include current sample
				peakLocations.add(bytesToFrames(roll));
				peakLocations.add(bytesToFrames(roll));
			    }

			    // If the post roll extends into the future buffer.
			    else if((i+roll)>audioBuffer.length){
				for(int j=(i-roll);j<audioBuffer.length;j+=2) {
				    outputStream.write(audioBuffer[j-1]);
				    outputStream.write(audioBuffer[j]);
				    //byteCount+=2;

				}
				numBytesReq -= (audioBuffer.length-((i-1)-roll));
				peakLocations.add(bytesToFrames(roll));
				peakLocations.add(bytesToFrames(roll));
				advanceBytes = roll-(audioBuffer.length-i)+1; //+1 to even number of bytes
			    }

			    // All of the pre and post roll are included in this buffer
			    else {
				for(int j=(i-roll);j<=(i+roll);j+=2) {
				    outputStream.write(audioBuffer[j-1]);				
				    outputStream.write(audioBuffer[j]);
				    // byteCount+=2;

				}
				numBytesReq -= (2*roll+2);
				peakLocations.add(bytesToFrames(roll));
				peakLocations.add(bytesToFrames(roll));
			    }
			    // To avoid overlap, the required +2 will occur at the end of the iteration of the for loop
			    currentPos += (2*roll);
			    i += (2*roll); 
			}
			currentPos += 2;			
		    }// End of for loop

		}// End of 1st if statement
		firstIter = false;
	    }
	    outputStream.flush();
	    outputStream.close();
	    System.out.println(peakLocations);
	    //} // End of While Loop
	}
	catch(FileNotFoundException e) {
	    e.printStackTrace();
	    return null;
	    //return;
	}
	catch (UnsupportedAudioFileException e1) {
	    throw new PlayWaveException(e1);
	} 
	catch (IOException e1) {
	    throw new PlayWaveException(e1);
	}
	return peakLocations;
    }

    /**
     * This method constructs the header for the wav file
     * @param outputStream  The FileOutputStream to which the wav file is being written
     * @param numBytes  The total number of bytes in the data chunk
     * @param headerLen  The header length in bytes
     * @param audioFormat  AudioFormat object contains numChannels, frameRate, and frameSize
     * @throws PlayWaveException
     */
    private static void buildHeader(OutputStream outputStream, long numBytes, int headerLen, AudioFormat audioFormat) throws PlayWaveException {
	try {
	    // Build header (byte values taken from original wav files)
	    // Byte order is LITTLE ENDIAN!
	    // For reference: http://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html

	    // 0x46464952
	    // ckID: "RIFF"
	    outputStream.write(0x52); //R
	    outputStream.write(0x49); //I
	    outputStream.write(0x46); //F
	    outputStream.write(0x46); //F

	    // 0x........ Hex value dependent on total file size
	    // cksize: 4 + 26 + (8 + M*Nc*Ns + (0 or 1))
	    //	M = bytes/sample (= 2 in our wav files)
	    //  Nc = Number of Channels (= 1 in our wav files)
	    //  Ns = Number of Samples
	    //	0 or 1: padding byte at end if M*Nc*Ns is odd
	    // Can calculate by taking total file size in bytes - headerLen - 8
	    long riffckSize = numBytes-8;

	    // Calculate the little endian byte pair equivalent of RIFF chunk size
	    byte[] b = new byte[4];
	    for(int i=0;i<4;i++) {
		if(Math.floor(riffckSize/Math.pow(16, 6-(2*i)))!=0) {
		    b[i] = (byte)Math.floor(riffckSize/Math.pow(16, 6-(2*i)));
		    riffckSize -= Math.pow(16, 6-(2*i));
		}
		else {
		    b[i] = 0x00;
		}
		//System.out.println(b[i]+" ");
	    }
	    outputStream.write(b[3]);
	    outputStream.write(b[2]);
	    outputStream.write(b[1]);
	    outputStream.write(b[0]);

	    // 0x45564157
	    // WAVEID: "WAVE"
	    outputStream.write(0x57); //W
	    outputStream.write(0x41); //A
	    outputStream.write(0x56); //V
	    outputStream.write(0x45); //E

	    // 0x20746d66
	    // ckID: "fmt "
	    outputStream.write(0x66); //f
	    outputStream.write(0x6d); //m
	    outputStream.write(0x74); //t
	    outputStream.write(0x20); //'Space'

	    // 0x00000012
	    // cksize: 18
	    outputStream.write(0x12);
	    outputStream.write(0x00);
	    outputStream.write(0x00);
	    outputStream.write(0x00);

	    // 0x0001
	    // wFormatTag: WAVE_FORMAT_PCM
	    outputStream.write(0x01);
	    outputStream.write(0x00);

	    // 0x0001
	    // nChannels (Nc): 1 (mono)
	    int numChannels = audioFormat.getChannels();
	    for(int i=0;i<2;i++) {
		if(Math.floor(numChannels/Math.pow(16, 2-(2*i)))!=0) {
		    b[i] = (byte)Math.floor(numChannels/Math.pow(16, 2-(2*i)));
		    numChannels -= Math.pow(16, 2-(2*i));
		}
		else {
		    b[i] = 0x00;
		}
		//System.out.println(b[i]+" ");
	    }
	    outputStream.write(b[1]); // 0x01
	    outputStream.write(b[0]); // 0x00

	    // 0x00005622
	    // nSamplesPerSec (F): 22050 Hz
	    float sampleRate = audioFormat.getFrameRate();
	    for(int i=0;i<4;i++) {
		if(Math.floor(sampleRate/Math.pow(16, 6-(2*i)))!=0) {
		    b[i] = (byte)Math.floor(sampleRate/Math.pow(16, 6-(2*i)));
		    sampleRate -= Math.pow(16, 6-(2*i));
		}
		else {
		    b[i] = 0x00;
		}
		//System.out.println(b[i]+" ");
	    }
	    outputStream.write(b[3]); //0x22
	    outputStream.write(b[2]); //0x56
	    outputStream.write(b[1]); //0x00
	    outputStream.write(b[0]); //0x00

	    // 0x0000ac44
	    // nAvgBytesPerSec (F*M*Nc): 44100 Hz
	    float bytesPerSec = audioFormat.getFrameRate()*audioFormat.getFrameSize()*audioFormat.getChannels();
	    for(int i=0;i<4;i++) {
		if(Math.floor(bytesPerSec/Math.pow(16, 6-(2*i)))!=0) {
		    b[i] = (byte)Math.floor(bytesPerSec/Math.pow(16, 6-(2*i)));
		    bytesPerSec -= Math.pow(16, 6-(2*i));
		}
		else {
		    b[i] = 0x00;
		}
		//System.out.println(b[i]+" ");
	    }
	    outputStream.write(b[3]); //0x44
	    outputStream.write(b[2]); //0xac
	    outputStream.write(b[1]); //0x00
	    outputStream.write(b[0]); //0x00

	    // 0x0002
	    // nBlockAlign (M*Nc): 2 bytes/frame
	    int bytesPerFrame = audioFormat.getFrameSize();
	    for(int i=0;i<2;i++) {
		if(Math.floor(bytesPerFrame/Math.pow(16, 2-(2*i)))!=0) {
		    b[i] = (byte)Math.floor(bytesPerFrame/Math.pow(16, 2-(2*i)));
		    bytesPerFrame -= Math.pow(16, 2-(2*i));
		}
		else {
		    b[i] = 0x00;
		}
		//System.out.println(b[i]+" ");
	    }
	    outputStream.write(b[1]); //0x02
	    outputStream.write(b[0]); //0x00

	    // 0x0010
	    // wBitsPerSample (8*M): 16 bits/sample
	    int bitsPerSample = 8*audioFormat.getFrameSize();
	    for(int i=0;i<2;i++) {
		if(Math.floor(bitsPerSample/Math.pow(16, 2-(2*i)))!=0) {
		    b[i] = (byte)Math.floor(bitsPerSample/Math.pow(16, 2-(2*i)));
		    bitsPerSample -= Math.pow(16, 2-(2*i));
		}
		else {
		    b[i] = 0x00;
		}
		//System.out.println(b[i]+" ");
	    }
	    outputStream.write(0x10); //0x10
	    outputStream.write(0x00); //0x00

	    // 0x0000
	    // cbSize: 0
	    outputStream.write(0x00);
	    outputStream.write(0x00);

	    // 0x61746164
	    // ckID: "data"
	    outputStream.write(0x64); //d
	    outputStream.write(0x61); //a
	    outputStream.write(0x74); //t
	    outputStream.write(0x61); //a

	    // 0x........ Hex value dependent on total file size
	    // cksize: M*Nc*Ns
	    //	M = bytes/sample (= 2 in our wav files)
	    //  Nc = Number of Channels (= 1 in our wav files)
	    //  Ns = Number of Samples
	    //	0 or 1: padding byte at end if M*Nc*Ns is odd
	    // Can calculate by taking total file size in bytes - headerLen
	    long datackSize = numBytes-headerLen;

	    // Calculate the little endian byte pair equivalent of data chunk size
	    //byte[] b = new byte[4];
	    for(int i=0;i<4;i++) {
		if(Math.floor(datackSize/Math.pow(16, 6-(2*i)))!=0) {
		    b[i] = (byte)Math.floor(datackSize/Math.pow(16, 6-(2*i)));
		    datackSize -= Math.pow(16, 6-(2*i));
		}
		else {
		    b[i] = 0x00;
		}
		//System.out.println(b[i]+" ");
	    }
	    outputStream.write(b[3]);
	    outputStream.write(b[2]);
	    outputStream.write(b[1]);
	    outputStream.write(b[0]);
	}
	catch (IOException e1) {
	    throw new PlayWaveException(e1);
	}
    }

    /**
     * Simple method to convert number of bytes into number of frames. 
     * @param bytes  The number of bytes to be converted
     * @return  The equivalent number of frames
     */
    private static long bytesToFrames(double bytes) {
	// Convert current byte to current video frame
	return Math.round(bytes*vidFramesPerByte);
    }

    /**
     * This method write the video video based upon which audio samples were conserved
     * @param vFileName  The original video file name
     * @param peakLocations  The ArrayList contains the frame locations of each peak sample as well as the associated rolls
     */
    private static void writeVideo(String vFileName, ArrayList<Long> peakLocations) {
	try {
	    File file = new File(vFileName);
	    InputStream is = new FileInputStream(file);
	    long len = WIDTH*HEIGHT*3;
	    long numFrames = file.length()/len;
	    byte[] bytes = new byte[(int)len];

	    OutputStream vidOutputStream = new FileOutputStream("videoOutput.rgb");
	    int count=0;
	    long framePos = peakLocations.get(count);
	    long preRoll = peakLocations.get(count+1);
	    long postRoll = peakLocations.get(count+2);
	    boolean keepWriting = false;
	    for(int i=0;i<numFrames;i++) {
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
		    offset += numRead;
		}
		if((i==(framePos-preRoll))||keepWriting) {
		    //System.out.println("writing: "+i);
		    vidOutputStream.write(bytes);
		    keepWriting = true;
		    if(i==(framePos+postRoll)) {
			System.out.println(Math.round(100*(double)i/(double)numFrames)+"%");
			count += 3;
			if(count==peakLocations.size()) {
			    i=(int)numFrames;
			}
			else {
			    framePos = peakLocations.get(count);
			    preRoll = peakLocations.get(count+1);
			    postRoll = peakLocations.get(count+2);
			}
			keepWriting = false;

			if(i==(framePos-preRoll)) {
			    keepWriting=true;
			}
		    }
		}

	    }
	    vidOutputStream.flush();
	    vidOutputStream.close();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}

    }

    private static double bytesPerVidFrame;
    private static double vidFramesPerByte;
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    //private static final double FPS = 23.976; // Frames Per Second
    private static final double FPS = 24; // Frames Per Second
    private static final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
}