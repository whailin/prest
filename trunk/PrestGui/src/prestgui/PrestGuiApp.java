/*
 * PrestApp.java
 */

package prestgui;

import java.io.File;
import java.util.EventObject;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Application.ExitListener;

import com.sd.dev.lib.ISDContext;
import common.gui.packageexplorer.CommandLineExplorer;

import definitions.application.ApplicationProperties;

/**
 * The main class of the application.
 */
public class PrestGuiApp extends SingleFrameApplication implements ExitListener {

	// SciDesktop Modification TA_R001	--- Local folder to be created for settings under member home
	private static final String PRESTHOME = "PREST";
	
	private static boolean fromCommandLine = false;
	private static String [] cmdArguments;
	
	// SciDesktop Modification TA_R001	--- additional variables are needed for the created instance
	private static ISDContext sdContext;
	private PrestGuiView prestView;
	private boolean disposed;
	
	/**
	 * At startup create and show the main frame of the application.
	 */
	@Override
	protected void startup() {
		// SciDesktop Modification TA_R001	--- Changes to startup call
		// application.properties file is directed to member home folder
		// prestView is initialized with the context variable
		// exitlistener is added to the created instance
		String propPath = getPropertiesPath();
		ApplicationProperties.initiateManual(propPath);
		String repository = ApplicationProperties.get("repositorylocation");
		if (repository == null) {
			JOptionPane.showMessageDialog(null,
					"Please create a repository location for Prest Tool!",
					"Create Repository", JOptionPane.INFORMATION_MESSAGE);
			File repositoryFile = getProjectDirectoryFromUser();
			if (repositoryFile != null) {
				ApplicationProperties.setRepositoryLocation(propPath, repositoryFile.getAbsolutePath().replace("\\", "\\\\"));
				ApplicationProperties.initiate();
			}
		}
		
		// if command line option is chosen, change working style to command line
		if(fromCommandLine){
			System.out.println("Prest is selected to work from command line");
			CommandLineExplorer cmdLineExplorer = new CommandLineExplorer();
			cmdLineExplorer.startExecFromCmdLine(cmdArguments);
		}
		else
		{
			addExitListener(this);
			prestView = new PrestGuiView(this, sdContext); 
			show(prestView);
		}
		
	}

	// SciDesktop Modification TA_R001	--- getPropertiesPath is added to get full path of application.properties
	private String getPropertiesPath()
	{
		if (sdContext != null && sdContext.getMode() == ISDContext.MODE_NATIVE)
			try
			{
				File home = new File(sdContext.getMemberHome());
				File prestHome = new File(home, PRESTHOME);
				if (!prestHome.exists())
					prestHome.mkdir();
				if (prestHome.exists())
				{
					File propF = new File(prestHome, ApplicationProperties.getPropertiesFileName());
					return propF.getPath();
				}
			}
			catch (Exception e)
			{
			}
			
		return ApplicationProperties.getPropertiesFileName();
	}

	/**
	 * This method is to initialize the specified window by injecting resources.
	 * Windows shown in our application come fully initialized from the GUI
	 * builder, so this additional configuration is not needed.
	 */
	@Override
	protected void configureWindow(java.awt.Window root) {
	}

	/**
	 * A convenient static getter for the application instance.
	 * 
	 * @return the instance of PrestApp
	 */
	public static PrestGuiApp getApplication() {
		return Application.getInstance(PrestGuiApp.class);
	}
	
	public static void changeWorkStyle(String[] args)
	{
		fromCommandLine = true;
		cmdArguments = args;
	}
	

	/**
	 * Main method launching the application.
	 */
	public static void main(String[] args) {

		launch(PrestGuiApp.class, args);
		if(args.length != 0)
		{
			changeWorkStyle(args);
		}

	}

	public static File getProjectDirectoryFromUser() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select folder for repository");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showOpenDialog(null);

		File dir = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			dir = fileChooser.getSelectedFile();
		}
		if (dir == null) {
			dir.mkdir();
		}
		return dir;
	}

	// SciDesktop Modification TA_R001	--- added to ensure that proper termination occurs with user interaction (exit from the menu or closing the frame) 
	public void terminate()
	{
		if (sdContext == null || sdContext.getMode() == ISDContext.MODE_OFFLINE)
			end();
		else
			prestView.getFrame().dispose();
		disposed = true;
	}
	
	// SciDesktop Modification TA_R001	--- added to externally set the static SciDesktop context 
	public static void setContext(ISDContext ctx)
	{
		sdContext = ctx;
	}
	
	// SciDesktop Modification TA_R001	--- added to externally check whether the instance has terminated or not 
	public boolean isDisposed()
	{
		return disposed;
	}
	
	// SciDesktop Modification TA_R001	--- added to externally get the active frame of instance 
	public JFrame getFrameInstance()
	{
		return prestView != null ? prestView.getFrame() : null;
	}
	
	// SciDesktop Modification TA_R001	--- exit listener methods start here 
	public boolean canExit(EventObject arg0)
	{
		return false;
	}

	public void willExit(EventObject arg0)
	{
	}

}
