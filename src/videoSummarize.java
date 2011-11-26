//import java.awt.image.*;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class videoSummarize {
    public static void main(String[] args) {
	try {
	    if (args.length < 3) {
		System.err.println("usage: java videoSummarize videoInput.rgb audioInput.wav percentage");
		return;
	    }
	    String vfilename = args[0];
	    String aFileName = args[1];	
	    double percent = Double.parseDouble(args[2]);

	    audioSumm(aFileName, percent);
	}	

	catch (PlayWaveException e) {
	    e.printStackTrace();
	    return;
	}
    }

    private static void audioSumm(String aFileName, double percent) throws PlayWaveException {
	try {
	    int headerLen = 46; 							// Length of header in bytes
	    InputStream inputStream = new FileInputStream(aFileName);

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

	    InputStream waveStream = inputStream;
	    InputStream bufferedIn = new BufferedInputStream(waveStream);
	    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
	    AudioFormat audioFormat = audioInputStream.getFormat();

	    File soundFile = new File(aFileName);
	    long totNumBytes = soundFile.length(); 					// Total original audio file length in bytes
	    int numChan = audioFormat.getChannels();					// Number of Channels (= 1)
	    int bytesPerSample = audioFormat.getFrameSize();				// Number of bytes per sample (= 2)
	    float sampleRate = audioFormat.getFrameRate();				// Sample Rate (= 22050 Hz)
	    float bytesPerSecond = sampleRate*bytesPerSample;				// Number bytes per second (= 44100 Hz)
	    long totNumSamples = (totNumBytes-headerLen)/(bytesPerSample*numChan);	// Total original number of audio samples
	    int numSamplesReq = (int)(totNumSamples*percent);
	    int numBytesReq = (int)numSamplesReq*2;

	    int readBytes = 0;
	    byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
	    byte[] audioBufferOld = new byte[EXTERNAL_BUFFER_SIZE];
	    OutputStream outputStream = new FileOutputStream("audioOutput.wav");

	    buildHeader(outputStream, numBytesReq, headerLen, audioFormat);

	    int rollSamples = (int)sampleRate/2; // The pre/post roll is the number of samples that are written before/after the target byte	    
	    int threshold = 80; // Amplitude threshold

	    int roll = rollSamples*2; // Convert samples to bytes
	    boolean firstIter = true;
	    while (readBytes != -1) {
		audioBufferOld=audioBuffer;
		readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
		if((readBytes >= 0)&&(numBytesReq>0)) {
		    // Checks most significant byte for amplitude. 
		    //  If greater than threshold, one second before and after the peak are copied to the new wav file.
		    for(int i=1;i<audioBuffer.length;i+=2) { // Each sample is 2 bytes, MSB is second byte
			if(audioBuffer[i]>=threshold) {
			    i += roll;

			    // If the pre-roll extends beyond the beginning of the audio file
			    if(firstIter&&((i-roll)<0)) {
				for(int j=1;j<(i+roll);j+=2) {
				    outputStream.write(audioBuffer[j-1]);
				    outputStream.write(audioBuffer[j]);
				}
				numBytesReq -= (i+roll); 
			    }

			    // If the pre-roll extends into the previous buffer
			    else if((i-roll)<0) {
				for(int j=(audioBuffer.length-(roll-i)); j<audioBuffer.length; j+=2) {
				    outputStream.write(audioBufferOld[j-1]);
				    outputStream.write(audioBufferOld[j]);
				}
				for(int j=1;j<(i+roll);j+=2) {
				    outputStream.write(audioBuffer[j-1]);
				    outputStream.write(audioBuffer[j]);
				}
				numBytesReq -= 2*roll;
			    }

			    // If the post roll extends into the future buffer.
			    //  Doesn't take into account the information from the future buffer at this time.
			    else if(i+roll>audioBuffer.length){
				for(int j=(i-roll);j<audioBuffer.length;j+=2) {
				    outputStream.write(audioBuffer[j-1]);
				    outputStream.write(audioBuffer[j]);
				}
				numBytesReq -= (audioBuffer.length-(i-roll));
			    }

			    // All of the pre and post roll are included in this buffer
			    else {
				for(int j=(i-roll);j<(i+roll);j+=2) {
				    outputStream.write(audioBuffer[j-1]);				
				    outputStream.write(audioBuffer[j]);				
				}
				numBytesReq -= 2*roll;
			    }
			}
		    }
		    //numSamplesReq -= readBytes/2;
		}
		firstIter = false;
	    }
	    //System.out.println(numBytesReq);
	}
	catch(FileNotFoundException e) {
	    e.printStackTrace();
	    return;
	}
	catch (UnsupportedAudioFileException e1) {
	    throw new PlayWaveException(e1);
	} 
	catch (IOException e1) {
	    throw new PlayWaveException(e1);
	}

	System.out.println("done");
    }

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

    private static final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
}