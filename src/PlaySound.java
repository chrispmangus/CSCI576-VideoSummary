// CSCI 576 Final Project
// File:        PlaySound.java
// Programmers: Christopher Mangus, Louis Schwartz

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound implements Runnable{

    /**
     * CONSTRUCTOR
     */
    public PlaySound(InputStream waveStream, int audStartDelay) {
	this.waveStream = waveStream;
	this.startDelay = audStartDelay;
    }

    public void run(){
	try {
	    this.play();
	} 
	catch (PlayWaveException e) {
	    e.printStackTrace();
	    return;
	}
    }

    public void play() throws PlayWaveException {

	long tm = System.currentTimeMillis();
	try {
	    tm += startDelay;
	    Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
	}
	catch (InterruptedException e){
	    e.printStackTrace();
	}


	AudioInputStream audioInputStream = null;
	try {
	    InputStream bufferedIn = new BufferedInputStream(this.waveStream);
	    audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
	} 
	catch (UnsupportedAudioFileException e1) {
	    throw new PlayWaveException(e1);
	} 
	catch (IOException e1) {
	    throw new PlayWaveException(e1);
	}

	// Obtain the information about the AudioInputStream
	AudioFormat audioFormat = audioInputStream.getFormat();
	Info info = new Info(SourceDataLine.class, audioFormat);

	// opens the audio channel
	SourceDataLine dataLine = null;
	try {
	    dataLine = (SourceDataLine) AudioSystem.getLine(info);
	    dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
	} 
	catch (LineUnavailableException e1) {
	    throw new PlayWaveException(e1);
	}

	// Starts the music :P
	dataLine.start();

	int readBytes = 0;
	byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

	try {
	    while (readBytes != -1) {
		readBytes = audioInputStream.read(audioBuffer, 0,
			audioBuffer.length);
		if (readBytes >= 0){
		    dataLine.write(audioBuffer, 0, readBytes);
		    //System.out.println(dataLine.getFramePosition());
		}		   
	    }
	}
	catch (IOException e1) {
	    throw new PlayWaveException(e1);
	} 
	finally {	    
	    // plays what's left and and closes the audioChannel
	    dataLine.drain();
	    dataLine.close();
	}

    }
    
    private InputStream waveStream;
    private int startDelay;
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
}
