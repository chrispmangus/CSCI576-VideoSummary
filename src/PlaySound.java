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
 * This class plays the audio file.
 * Used some skeleton code given by the TAs as a basis for this class.
 * @author Christopher Mangus     
 * @author Louis Schwartz
 */
public class PlaySound implements Runnable{

    /**
     * The constructor for PlaySound
     * @param waveStream The audio file input stream
     */
    public PlaySound(InputStream waveStream) {
	this.waveStream = waveStream;
    }

    /**
     * Instantiates the play method.
     */
    public void run(){
	try {
	    this.play();
	} 
	catch (PlayWaveException e) {
	    e.printStackTrace();
	    return;
	}
    }

    /**
     * Plays the audio file.
     * @throws PlayWaveException
     */
    public void play() throws PlayWaveException {
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
	audioFormat = audioInputStream.getFormat();
	Info info = new Info(SourceDataLine.class, audioFormat);
	// opens the audio channel
	dataLine = null;
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
		readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
		if (readBytes >= 0) {
		    dataLine.write(audioBuffer, 0, readBytes);
		}

	    }
	}
	catch (IOException e1) {
	    throw new PlayWaveException(e1);
	} 

	finally {	
	    // plays what's left and and closes the audioChannel
	    dataLine.drain();
	    //dataLine.close();
	}
    }

    public long getPosition() {
	return dataLine.getLongFramePosition();
    }

    public float getSampleRate() {
	return audioFormat.getFrameRate();
    }

    private SourceDataLine dataLine;
    private AudioFormat audioFormat;
    private InputStream waveStream;
    private final int EXTERNAL_BUFFER_SIZE = 524288; 
}
