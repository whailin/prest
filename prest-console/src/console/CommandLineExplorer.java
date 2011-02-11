package console;

public class CommandLineExplorer {
	PackageExplorer packageExplorer;

	public void startExecFromCmdLine(String[] args) {
		packageExplorer = new PackageExplorer();
		//packageExplorer.traverseRepository();
		//console run format change
		if (args[0].equalsIgnoreCase("-addProject")) {
			packageExplorer.addNewProjectCmd(args[1]);
		}
		else if (args[0].equalsIgnoreCase("-parse")) {
			if(args.length == 2)
				packageExplorer.parseManualCmd(args[1], args[2], "");
			if(args.length == 2)
				packageExplorer.parseManualCmd(args[1], args[2], args[4]);
		}
		else if (args[0].equalsIgnoreCase("-logFilter")) {
			packageExplorer.logFiltering(args[1]);
		}
		else if (args[0].equalsIgnoreCase("-convertCsvToArff")) {
			packageExplorer.convertCsvToArff(args[1]);
		}
		else if (args[0].equalsIgnoreCase("-predict")) {
			packageExplorer.predict(args[1], args[2]);
		}
		else {
			listCommandLineOptions();
		}
	}
	
	
	public void listCommandLineOptions() {
			System.out.println("The command line options are:");
			System.out.println("-addProject projectDirectory");
			System.out.println("-parse projectDirectory freezelabel");
			System.out.println("-parse projectDirectory freezelabel -f FileLevelMetricsOutputDirectory");
			System.out.println("-logFilter filepath");
			System.out.println("-convertCsvToArff filepath");
			System.out.println("-predict trainfile testfile");
		}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
