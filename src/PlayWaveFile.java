
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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

	// get the command line parameters
	if (args.length < 1) {
	    System.err.println("usage: java -jar PlayWaveFile.jar [filename]");
	    return;
	}
	String afilename = args[0];
	String vfilename = args[1];

	// opens the inputStream
	FileInputStream inputStream;
	try {
	    inputStream = new FileInputStream(afilename);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    return;
	}

	// initializes the playSound Object
	PlaySound playSound = new PlaySound(inputStream);
	imageReader imageReader = new imageReader(vfilename);
	
	Thread t1 = new Thread(playSound);
	Thread t2 = new Thread(imageReader);
	
	t1.start();
	t2.start();

	// plays the sound
	
    }

}
