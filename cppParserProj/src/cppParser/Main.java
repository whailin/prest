package cppParser;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        switch(args.length){
            case 0:
                printInstructions();
                break;
            case 1:
                Extractor e = new Extractor(args[0]);
                e.process();
                break;
            default:
                try{
                    CmdLineParameterParser.parseParameters(args);
                    System.out.println(CmdLineParameterParser.getInputDir()+" out:"+CmdLineParameterParser.getOutputDir());
                }catch(Exception ex){
                    System.out.println("Error:"+ex.getMessage());
                    printInstructions();
                }
		// TODO Replace this main function with a call from Prest
        
        }
        /*
		
		*/
	}
    
    private static void printInstructions(){
        System.out.println("Instructions");
    }

}
