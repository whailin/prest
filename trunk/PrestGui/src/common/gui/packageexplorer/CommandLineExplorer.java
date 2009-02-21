package common.gui.packageexplorer;

import java.io.File;

import javax.swing.JOptionPane;

import common.monitor.Logger;

import prestgui.PrestGuiView;
import definitions.application.ApplicationProperties;
import executor.ParserExecutor;
import common.gui.packageexplorer.PackageExplorer;

public class CommandLineExplorer {
	PackageExplorer packageExplorer;

	public void listCommandLineOptions() {
		System.out.println("The command line options are:");
		System.out.println("-add projectName projectDirectory");
		System.out.println("-parse projectDirectory");
	}

	public void startExecFromCmdLine(String[] args) {
		packageExplorer = new PackageExplorer();
		//packageExplorer.traverseRepository();

		if (args[0].equalsIgnoreCase("-parse")) {
			packageExplorer.parseManualCmd(args[1]);
		} else if (args[0].equalsIgnoreCase("-addProject")) {
			packageExplorer.addNewProjectCmd(args[1]);
		} else {
			listCommandLineOptions();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
