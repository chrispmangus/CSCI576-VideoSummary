import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class videoSegment {

	videoSegment(String fileName){
		this.fileName = fileName;
	}
	
	public void analyze(){
		
		File file;
		
		breaks = new ArrayList<Integer>();
		try{
			file = new File(fileName);
			is = new FileInputStream(file);
			
			long frameByteSize = WIDTH*HEIGHT*3;
		    long numFrames = file.length()/frameByteSize;

		   

		    bytes = new byte[(int)frameByteSize];
		    histogramPrev = new int[4][4][4];
		    histogramNext = new int[4][4][4];

			
			/* read in the first frame to seed the process */
		    readBytes(histogramPrev);
			breaks.add(0);
			
			/* compareHist = compare every x frame
			   tillCompareHist = countdown till compareHist
			   pastBreak = FPS * SEC_BUFFER buffer of seconds
					until a break can be made again */
			int compareHist = 3;
		    int tillCompareHist = 0;
		    int pastBreak = 0;
		    		   	
		    for(int i = 1; i < numFrames-1; i ++){
		    	
		    	if(tillCompareHist == compareHist){
		    		clearHistogramNext();
		    		readBytes(histogramNext);
		    		double val = SDvalue();
		    		val = val / (WIDTH*HEIGHT);
		    		val *= 100;
		    		
					/*if the SDValue is greater than the threshold
					  and is 
					*/
		    		if(val > THRESHOLD && pastBreak <= 0 && val < 100){
		    			breaks.add(i);
		    			pastBreak = FPS * SEC_BUFFER;
		    		}
		    		
		    		
		    		copyHistogramBack();
		    		tillCompareHist = 0;
		    	}else{
					skipBytes(frameByteSize);
		    	}
			    
			    tillCompareHist++;
			    pastBreak--;
		    }
		    is.close();
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		
	}
	
	public ArrayList<Integer> getBreaks(){
		return breaks;
	}
	
	public void printBreaks(){
		for(Integer i : breaks){
			System.out.println("BREAK AT: " + i);
		}
	}
	
	private void skipBytes(long l){
		try {
			is.skip(l);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readBytes(int[][][] histogram) {
		try {
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
			int ind = 0;
			for (int y = 0; y < HEIGHT; y++) {
				for (int x = 0; x < WIDTH; x++) {
					byte r = bytes[ind];
					byte g = bytes[ind + HEIGHT * WIDTH];
					byte b = bytes[ind + HEIGHT * WIDTH * 2];
					
					/* extracting the 2 most significant bits 
						and filling the histogram bins*/
					int ri = (int)( (r & 0xff) &0xC0);
					int gi = (int)( (g & 0xff) &0xC0);
					int bi = (int)( (b & 0xff) &0xC0);
						
					histogram[ri/64][gi/64][bi/64]++;
					
					ind++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//save the histogram as the previous histogram
	private void copyHistogramBack(){
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				for(int k = 0; k < 4; k++){
					histogramPrev[i][j][k] = histogramNext[i][j][k]; 
				}
	}
	
	//clears HistogramNext
	private void clearHistogramNext(){
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				for(int k = 0; k < 4; k++){
					histogramNext[i][j][k] = 0; 
				}
	}
	
	
	/*gathers the average color intensity change between the two
	  shots. this is a double value that will be compared with the
	  THRESHOLD set at the bottom of the file */
	private double SDvalue(){
		int sum = 0;
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				for(int k = 0; k < 4; k++){
					sum += Math.abs(histogramPrev[i][j][k] - histogramNext[i][j][k]);
				}
		return ((double)sum);
	}
	
	//THRESHOLD::::  25 on sample1, 20 on sample2 so far, 25 seems reasonable for sample3
	ArrayList<Integer> breaks;
	private final int THRESHOLD = 25;
	private final int SEC_BUFFER = 5;
	private int[][][] histogramPrev;
	private int[][][] histogramNext;
	private final int WIDTH = 320;
    private final int HEIGHT = 240;
    private final int FPS = 24;
    private String fileName;
    private InputStream is;
    private byte[] bytes;
}
