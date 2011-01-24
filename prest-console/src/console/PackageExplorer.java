/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package console;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import common.DirectoryListing;
import definitions.application.ApplicationProperties;
import executor.ParserExecutor;
import common.CsvToArff;
import predictor.WekaRunner;

public class PackageExplorer {

	private HashMap<String, File> projectNamesHashMap = new HashMap<String, File>();
	private File projectDirectory;

	public PackageExplorer() {
	}

	public String readMetadataForProject(File projectDirectory) {
		StringBuffer contents = new StringBuffer();

		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(
					projectDirectory));
			try {
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				while ((line = input.readLine()) != null) {
					contents.append(line);
					// contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return contents.toString();
	}

	public void createMetadataForProject(File projectDirectory) {
		Writer output = null;
		try {
			// FileWriter always assumes default encoding is OK!
			output = new BufferedWriter(new FileWriter(
					new File(ApplicationProperties.get("repositorylocation")
							+ File.separator + projectDirectory.getName()
							+ File.separator + "project.metadata")));
			output.write(projectDirectory.getAbsolutePath());
		} catch (Exception ex) {
		} finally {
			try {
				output.close();
			} catch (IOException ex) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	public boolean searchRepository(File newDirectory) {
		common.DirectoryListing dlx = new DirectoryListing();
		dlx.visitAllOneLevelDirs(new File(ApplicationProperties
				.get("repositorylocation")
				+ File.separator));
		List dirNames = dlx.getDirNames();
		List<File> dirList = dirNames;
		if (dirList != null) {
			for (File projectDir : dirList) {
				if (projectDir.getName().equals(newDirectory.getName())) {
					System.out.println(" *** " + projectDir.getName() + " "
							+ newDirectory.getName());
					return true;
				}
			}
		}
		return false;
	}

	public void addNewProjectCmd(String projectDirectoryStr) {
		File projectDirectory = new File(projectDirectoryStr);
		if (projectDirectory != null) {
			if (!searchRepository(projectDirectory)) {
				new File(ApplicationProperties.get("repositorylocation") + File.separator
						+ projectDirectory.getName()).mkdirs();
				new File(ApplicationProperties.get("repositorylocation") + File.separator
						+ projectDirectory.getName() + File.separator+ "parse_results")
						.mkdirs();
				new File(ApplicationProperties.get("repositorylocation") + File.separator
						+ projectDirectory.getName() + File.separator +"arff_files").mkdirs();
				createMetadataForProject(projectDirectory);
			} else {
				System.out.println("ERROR: The project you tried to add has already been added!");
			}
		}
	}

	public void parseManualCmd(String projectDirectoryStr, String freeze) {
		int result;
		try {
			File projectDirectory = new File(projectDirectoryStr);
			result = ParserExecutor.parseDirectoryCmd(projectDirectory);
			if (result == ParserExecutor.PARSING_SUCCESSFUL) {
				System.out.println("Project parsed successfully.");
//				DefectMatcher.main(new String[] {projectDirectory.getName(), releaseLabel, ApplicationProperties
//						.get("repositorylocation")
//						+ File.separator
//						+ projectDirectory.getName()
//						+ File.separator + "parse_results"});
			} else {
				System.out
						.println("There was an error while parsing the project.");
			}
		} catch (Exception ex) {

		}
	}
	
	public void convertCsvToArff(String fileName) {
		CsvToArff c = new CsvToArff();
		try {
			CsvToArff cCommand = new CsvToArff();
			cCommand.csvToArffCommand(fileName);
			System.out.println(fileName + "converted successfully...");
		} catch (Exception eCtoArff) {
			System.out.println("Error: File name wrong or file corrupt!");
		}
	}


	public HashMap<String, File> getProjectNamesHashMap() {
		return projectNamesHashMap;
	}

	public void setProjectNamesHashMap(HashMap<String, File> projectNamesHashMap) {
		this.projectNamesHashMap = projectNamesHashMap;
	}

	public File getProjectDirectory() {
		return projectDirectory;
	}

	public void setProjectDirectory(File projectDirectory) {
		this.projectDirectory = projectDirectory;
	}
	
	public String predict(String trainFile,String testFile) {
		String wekaAlgorithmType = "Naive Bayes";
		String wekaPreProcess = "none";
		String wekaCrossValidate = "no";
		String wekaLogFilter = "no";
		String retStr = "";
		retStr = WekaRunner.runWeka(trainFile,testFile,wekaAlgorithmType,wekaPreProcess,wekaCrossValidate,wekaLogFilter); 
		
		return retStr;
	}
}
