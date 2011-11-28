import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;


public class videoSegment {

	videoSegment(String fileName){
		this.fileName = fileName;
	}
	
	public void analyze(){
		img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		File file;
		long tm = System.currentTimeMillis();
		double delay = 1000/FPS;
		try{
			file = new File(fileName);
			is = new FileInputStream(file);
			
			long len = WIDTH*HEIGHT*3;
		    long numFrames = file.length()/len;

		    JFrame frame = new JFrame();
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame.setTitle("Wumpi Player");
		    frame.setSize(WIDTH,HEIGHT+22);	

		    bytes = new byte[(int)len];
		    histogramPrev = new int[4][4][4];
		    histogramNext = new int[4][4][4];

		    imageReaderComponent component = new imageReaderComponent();
		    readBytes(true, histogramPrev);
		    int tillCompareHist = 0;
		    int pastBreak = 0;
		   	int total = 0;    
		    
		    for(int i = 1; i < numFrames; i ++){
		    	tm = System.currentTimeMillis();
		    	if(tillCompareHist == 12){
		    		clearHistogramNext();
		    		readBytes(true, histogramNext);
		    		double val = SDvalue();
		    		val = val / (WIDTH*HEIGHT);
		    		val *= 100;
		 
		    		if(val > THRESHOLD && pastBreak <= 0 && val < 100){
		    			System.out.println("BREAK AT FRAME: " + i);
		    			total++;
		    			pastBreak = 24*3;
		    		}
		    		
		    		//System.out.println(val);
		    		copyHistogramBack();
		    		tillCompareHist = 0;
		    	}else{
		    	readBytes(false, null);
		    	}
			    component.setImg(img);
			    frame.add(component);
			    frame.repaint();	
			    frame.setVisible(true);	
			    tm += delay;
			    Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
			    tillCompareHist++;
			    pastBreak--;
		    }
		    
			System.out.println("NUM SHOTS: " + total);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch (InterruptedException e){
		    e.printStackTrace();
		}
	}
	
	
	private void readBytes(boolean record, int[][][] histogram) {
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
					
					if(record){
						int ri = (int)( (r & 0xff) &0xC0);
						int gi = (int)( (g & 0xff) &0xC0);
						int bi = (int)( (b & 0xff) &0xC0);
						
						histogram[ri/64][gi/64][bi/64]++;
					}

					int pix = 0xff000000 | ((r & 0xff) << 16)
							| ((g & 0xff) << 8) | (b & 0xff);
					img.setRGB(x, y, pix);
					ind++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void copyHistogramBack(){
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				for(int k = 0; k < 4; k++){
					histogramPrev[i][j][k] = histogramNext[i][j][k]; 
				}
	}
	
	private void clearHistogramNext(){
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				for(int k = 0; k < 4; k++){
					histogramNext[i][j][k] = 0; 
				}
	}
	
	private double SDvalue(){
		int sum = 0;
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				for(int k = 0; k < 4; k++){
					sum += Math.abs(histogramPrev[i][j][k] - histogramNext[i][j][k]);
				}
		return ((double)sum);
	}
	
	//THRESHOLD::::  25 on sample1, 20 on sample2 so far
	private final int THRESHOLD = 25;
	private int[][][] histogramPrev;
	private int[][][] histogramNext;
	private final int WIDTH = 320;
    private final int HEIGHT = 240;
    private final double FPS = 24.0;
    private static String fileName;
    private InputStream is;
    private BufferedImage img;
    private byte[] bytes;
}
