package console;

import definitions.application.ApplicationProperties;

public class PrestConsoleApp {

	private static PrestConsoleApp appInstance;
	private static boolean fromCommandLine = false;
	private static String [] cmdArguments;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/**
		 * Main method launching the application.
		 */			
			createInstance(args);

	}
	
	public static PrestConsoleApp createInstance(String[] args)
	{
		if (appInstance != null)
			return appInstance;
		
		appInstance = new PrestConsoleApp();
		if (args != null && args.length != 0)
			System.out.println("No arguments provided.");
		if (args != null && args.length != 0)
			changeWorkStyle(args);
		appInstance.startup();
		
		return appInstance;
	}
	
	public void startup() 
	{
		String propPath = getPropertiesPath();
		ApplicationProperties.initiateManual(propPath);
		String repository = ApplicationProperties.get("repositorylocation");
		if (repository == null) {
			System.out.println("check your application.properties file no repository location selected");
		}
		
		if(fromCommandLine)
		{
			System.out.println("Prest is selected to work from command line");
			CommandLineExplorer cmdLineExplorer = new CommandLineExplorer();
			cmdLineExplorer.startExecFromCmdLine(cmdArguments);
		}
	}
	
	// SciDesktop Modification TA_R001	--- getPropertiesPath is added to get full path of application.properties
	private String getPropertiesPath()
	{
					
		return ApplicationProperties.getPropertiesFileName();
	}
	
	public static void changeWorkStyle(String[] args)
	{
		fromCommandLine = true;
		cmdArguments = args;
	}
	
}
