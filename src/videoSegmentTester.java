
public class videoSegmentTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 1) {
			System.err.println("usage: java videoSegment file.");
			return;
		    }
		
		videoSegment vs = new videoSegment(args[0]);
		vs.analyze();
	}

}
