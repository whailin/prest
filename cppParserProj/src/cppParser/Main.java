package cppParser;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Replace this main function with a call from Prest
		
		Extractor e = new Extractor(args[0]);
		e.process();
		
	}

}
