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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.ApplicationProperties;
import common.DirectoryListing;
import executor.ParserExecutor;
import common.CsvToArff;
import predictor.WekaRunner;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;

import org.apache.log4j.Logger;

public class PackageExplorer {

	static Logger logger = Logger.getLogger(PrestConsoleApp.class.getName());
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
					logger.info(" *** " + projectDir.getName() + " "
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
				logger.warn("The project you tried to add has already been added!");
			}
		}
	}

	public void parseManualCmd(String projectDirectoryStr, String freeze, String fileCsvPath) {
		int result;
		try {
			File projectDirectory = new File(projectDirectoryStr);
			result = ParserExecutor.parseDirectoryCmd(projectDirectory, fileCsvPath);
			if (result == ParserExecutor.PARSING_SUCCESSFUL) {
				logger.info("Project parsed successfully.");
//				DefectMatcher.main(new String[] {projectDirectory.getName(), releaseLabel, ApplicationProperties
//						.get("repositorylocation")
//						+ File.separator
//						+ projectDirectory.getName()
//						+ File.separator + "parse_results"});
			} else {
				logger.error("There was an error while parsing the project.");
			}
		} catch (Exception ex) {

		}
	}
	

	
	public void convertCsvToArff(String fileName, String outputPath) {
		CsvToArff c = new CsvToArff();
		try {
			CsvToArff cCommand = new CsvToArff();
			cCommand.csvToArffCommand(fileName, outputPath);
		} catch (Exception eCtoArff) {
			logger.error("csv File name wrong or file corrupt!");
		}
	}
	/* This function applies log filtering on metric values.
	 * input: filename (at file level) 
	 * output: (true) if new file with log-filtered attributes
	 * IMPORTANT!!: Due to hard-coded attribute indices, this function works for file level only.
	 * */
	public boolean logFiltering(String filename)
	{
		ArffLoader loader = new ArffLoader();
		Instances data;
		BufferedWriter writer = null;
		boolean methodLevel = false;
	    try 
	    {
	    	logger.info(filename);
			loader.setSource(new File(filename));
			data = loader.getDataSet();
			data.setClassIndex(data.numAttributes()-1);
			if(data.attribute(2).name().equals("Method Name"))
				methodLevel = true;
			
			Instances newdata = new Instances(data, data.numInstances());
			
			for(int j=0; j<data.numAttributes(); j++)
			{
			//	System.out.println("Attribute: " + j);
				for(int i=0; i<data.numInstances(); i++)
				{
			//		System.out.println("Instance: " + i);
					if(j==0) //Filename
					{
						newdata.add(data.instance(i));
						newdata.instance(i).setValue(j, data.instance(i).stringValue(j));
					}
					else if (j==1) //File ID
						newdata.instance(i).setValue(j, data.instance(i).value(j));
					else if (j==data.numAttributes()-1) //Class attribute
						newdata.instance(i).setClassValue(data.instance(i).stringValue(j));
					else
					{
						if(methodLevel)
						{
							if(j!= 2 && j!=3)
							{
								if(data.instance(i).value(j) != 0)
									newdata.instance(i).setValue(j, Math.log(data.instance(i).value(j)));
								else
									newdata.instance(i).setValue(j, Math.log(0.0001));
							}
						}
						else
						{
							if(data.instance(i).value(j) != 0)
								newdata.instance(i).setValue(j, Math.log(data.instance(i).value(j)));
							else
								newdata.instance(i).setValue(j, Math.log(0.0001));
						}
					}
				}
			}
		
			logger.info("Writing to new file...");
			String outFile = filename.substring(0, filename.lastIndexOf(".")); 
			logger.info(outFile);
			writer = new BufferedWriter(new FileWriter(outFile + "_LF.arff"));
						
			writer.write(newdata.toString());
    	    writer.flush();
    	    writer.close();
    	    logger.info("Log filter is applied to " + filename + " successfully");
		} 
	    catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Log filter could not be processed due to IOException");
			e.printStackTrace();
			return false;
		}
	    
	    return true;  
	}

	/* this function takes values at method level 
	 * and aggregates them up to file level.
	 * input: filename (method level)
	 * output: (true) if new file with aggregated values
	 * */
	public boolean aggregateMethod2File(String file)
	{
		Instances data;
		ArffLoader loader = new ArffLoader();
		BufferedWriter writer = null;
		try
		{
			logger.info(file);
			loader.setSource(new File(file));
			data = loader.getDataSet();
			data.setClassIndex(data.numAttributes()-1);
			Instances newdata = new Instances(data, data.numInstances());
			
			String filename = "";
			String temp="";
			double minValue = -1;
			double maxValue = 0;
			double totalValue = 0;
			double avgValue;

			/*put distinct file names into newdata */
			int count = 0;
			for(int i=0; i<data.numInstances(); i++)
			{
				temp = data.instance(i).stringValue(0);
				if(i==0)
				{
					filename = temp;
					newdata.add(data.instance(i));
					newdata.instance(count).setValue(0, data.instance(i).stringValue(0));
					count ++;
				}
				if(temp.equals(filename))
				{		
				}
				else
				{
					filename = temp;
					newdata.add(data.instance(i));
					newdata.instance(count).setValue(0, data.instance(i).stringValue(0));
					count++;
				}	
			}
			
			ArrayList<ArrayList<String>> valueArray = new ArrayList<ArrayList<String>>();
			ArrayList<String> tempArray = null;
			int index = 4;
			int index2 = newdata.numAttributes()-1;
			boolean firstinst = true;
			Instance tempinst;
			int lastpos = -1;
			for(int j=0; j<newdata.numInstances(); j++)
			{
				filename = newdata.instance(j).stringValue(0);
			//	System.out.println(filename);
				tempArray = new ArrayList<String>();
				for(int k=lastpos+1; k<data.numInstances(); k++)
				{
					tempinst = data.instance(k);
					if(filename.equals(tempinst.stringValue(0)))
					{
					//	System.out.println(tempinst.stringValue(0));
						for(int y=4; y<data.numAttributes(); y++)
						{
							tempArray.add(""+tempinst.value(y));
						}
						valueArray.add(tempArray);
						tempArray = new ArrayList<String>();
						lastpos = k;
					}
					else
					{
					//	System.out.println("New File: " + tempinst.stringValue(0));
						/*compute new attributes for each (distinct) file */
						for(int x=0; x<valueArray.get(0).size(); x++)
						{
							//iterate for attributes
							for(int y=0; y< valueArray.size(); y++)
							{
								//iterate for instances (transpose)
								tempArray.add(valueArray.get(y).get(x));
							}
							for(int z=0; z < tempArray.size(); z++)
							{
								if(Double.parseDouble(tempArray.get(z)) < minValue || minValue <0)
									minValue = Double.parseDouble(tempArray.get(z));
								if(Double.parseDouble(tempArray.get(z)) > maxValue)
									maxValue = Double.parseDouble(tempArray.get(z));
								totalValue = totalValue + Double.parseDouble(tempArray.get(z));
							}
							avgValue = totalValue / valueArray.size();
							if(firstinst)
							{
								newdata.insertAttributeAt(new Attribute("MaxValue"),index2);
								newdata.insertAttributeAt(new Attribute("TotalValue"), index2+1);
								newdata.insertAttributeAt(new Attribute("AvgValue"), index2+2);
							}
							newdata.instance(j).setValue(index, minValue);
							index++;
							newdata.instance(j).setValue(index2, maxValue);
							index2++;
							newdata.instance(j).setValue(index2, totalValue);
							index2++;
							newdata.instance(j).setValue(index2, avgValue);
							index2++;
							minValue = -1;maxValue=0;totalValue=0; avgValue=0;
							tempArray = new ArrayList<String>();
						}
						firstinst = false;
						valueArray = new ArrayList<ArrayList<String>>();
						tempArray = null;
						index = 4;
						index2 = data.numAttributes()-1;
						break;
					}
				}
			}
			/*final touch: remove method name and method id from the dataset*/
			newdata.deleteAttributeAt(2);
			newdata.deleteAttributeAt(2);
			logger.info("Writing to new file...");
			String outFile = file.substring(0, file.lastIndexOf(".")); 
			logger.info(outFile);
			writer = new BufferedWriter(new FileWriter(outFile + "_AG.arff"));
			writer.write(newdata.toString());
    	    writer.flush();
    	    writer.close();
    	    logger.info("Aggregation is applied to " + file + " successfully");
		}
		catch(Exception e)
		{
			logger.error("Aggregation function could not be processed due to IOException");
			e.printStackTrace();
			return false;
		}
		return true;
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
	
	public String predict(String trainFile,String testFile, String resultPath) {
		String wekaAlgorithmType = "Naive Bayes";
		String wekaPreProcess = "none";
		String wekaCrossValidate = "no";
		String wekaLogFilter = "no";
		String retStr = "";
		retStr = WekaRunner.runWeka(trainFile,testFile,wekaAlgorithmType,wekaPreProcess,wekaCrossValidate,wekaLogFilter, resultPath); 
		return retStr;
	}
}
