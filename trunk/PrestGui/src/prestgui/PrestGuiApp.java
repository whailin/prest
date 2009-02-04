/*
 * PrestApp.java
 */

package prestgui;

import definitions.application.ApplicationProperties;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import prestgui.PrestGuiView;

/**
 * The main class of the application.
 */
public class PrestGuiApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        ApplicationProperties.initiate();
        String repository = ApplicationProperties.get("repositorylocation");
        if(repository == null){
            JOptionPane.showMessageDialog(null, "Please create a repository location for Prest Tool!", "Create Repository", JOptionPane.INFORMATION_MESSAGE);
            File repositoryFile = getProjectDirectoryFromUser();
            if(repositoryFile != null){
                ApplicationProperties.setRepositoryLocation(repositoryFile.getAbsolutePath().replace("\\", "\\\\"));
                ApplicationProperties.initiate();
            }
        }
        show(new PrestGuiView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of PrestApp
     */
    public static PrestGuiApp getApplication() {
        return Application.getInstance(PrestGuiApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(PrestGuiApp.class, args);
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
        if(dir == null) {
            dir.mkdir();
        }
        return dir;
    }
}
