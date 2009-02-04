package parser.C;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;

import parser.C.constants.CConstants;
import parser.C.fileops.ConvertToDataContext;
import parser.C.fileops.Module;
import parser.C.fileops.Parser;
import parser.C.fileops.SourceFile;
import parser.Java.MetricsRelatedFiles.ClassContainer;
import parser.enumeration.Language;
import parser.parserinterface.IParser;

import common.DataContext;
import definitions.application.ApplicationProperties;

public class CParser implements IParser {

	Language language = Language.C;
	String workspace = "default_C_workspace";
	String className = "default_C_class";

	public String CALLGRAPHFILE;

	public CParser() {

	}

	// added by ekrem
	// the following liked list will keep all the modules
	// in all the source files
	public LinkedList<Module> allModules = new LinkedList<Module>();
	// added by ekrem
	// the following 2D array will contain the function call matrix
	public String functionCallMatrix[][];

	public DataContext startExecution(String[] files, String projectName,String xmlFileName, String packageCsvFileName,
		    String fileCsvFileName, String classCsvFileName, String methodCsvFileName)
			throws Exception {
		CALLGRAPHFILE = ApplicationProperties.get("repositorylocation") + "\\" + projectName + "\\callGraph" + "_"
				+ Language.C.getLangName() + ".csv";

		DataContext general = new DataContext();
		general.add(workspace, new DataContext());
		DataContext metrics = general.getNode(workspace);
		for (int i = 0; i < files.length; i++) {
			File currentFile = new File(files[i]);
			Parser parser = new Parser(currentFile);
			LinkedList<Module> modules = parser.getModules();
			SourceFile sf = parser.getSourceFile();
			DataContext fileMetrics = ConvertToDataContext
					.createMetricsDataContext(sf,
							CConstants.FILE_METRICS_HEADER);
			metrics.add(currentFile.getName(), fileMetrics);
			Iterator<Module> itr2 = modules.iterator();
			
			DataContext currentFileDataContext = metrics.getNode(currentFile
					.getName());
			while (itr2.hasNext()) {
				Module m = itr2.next();
				if (m.isHasBlock() && (m.getType() == Module.FUNCTION)
						&& !(m.getName().equalsIgnoreCase(""))) {
					DataContext moduleMetrics = ConvertToDataContext
							.createMetricsDataContext(m,
									CConstants.MODULE_METRICS_HEADER);
					currentFileDataContext.add(className + "/" + m.getName(),
							moduleMetrics);

					// added by ekrem
					// this module is a function module
					// so add it to allModules
					allModules.add(m);
				}
			}
		}

		// added by ekrem
		// the following function will make a csv file
		// which will contain the function call matrix
		//buildFunctionCallMatrix(CALLGRAPHFILE);

		
		/*try {
			System.out.println("writing from container to csv files");
			container.writeToFileAsXls(xmlFileName, packageCsvFileName,
				    fileCsvFileName, classCsvFileName, methodCsvFileName); // converts the given xml to csv file
			System.out.println("csv files written");
		} catch (Exception e) {
			System.out.println("writing to csv file failed");
			e.printStackTrace();
		}*/
		
		general.writeToFile(xmlFileName);
		return general;
		

	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	/**
	 * added as a patch to build a function call matrix
	 * 
	 * @author Ekrem Kocaguneli
	 */
	public void buildFunctionCallMatrix(String fileName) {
		int dimension;
		// get the size of the array
		dimension = allModules.size();
		// create the 2D array of strings
		functionCallMatrix = new String[dimension + 1][dimension + 1];
		// define an iterator for allModules
		Iterator<Module> allModulesItr = allModules.iterator();

		// assign * to 0*0 position of the 2D array
		functionCallMatrix[0][0] = "*";
		// copy the names of the modules to the edges of the 2D array
		for (int i = 1; i <= dimension; i++) {
			functionCallMatrix[i][0] = allModulesItr.next().getName();
			functionCallMatrix[0][i] = functionCallMatrix[i][0];
		}

		// FUNCTION CALL MATRIXI TARAYIPO 0 VE 1 LE DOLDURCAM
		// SONRA DA ONU DOSYAYA YAZCAM
		Iterator<Module> allModulesItr2 = allModules.iterator();

		for (int i = 1; i <= dimension; i++) {
			Module moduleToCheck = new Module();
			moduleToCheck = allModulesItr2.next();
			// System.out.println( "\n" );
			for (int j = 1; j <= dimension; j++) {

				if (moduleToCheck.getCalledFunctions().contains(
						functionCallMatrix[0][j])) {
					functionCallMatrix[i][j] = "1";
				} else {
					functionCallMatrix[i][j] = "0";
				}
			}
		}

		try {
			File functionCallMatrixFile = new File(fileName);
			functionCallMatrixFile.createNewFile();
			FileOutputStream fosFuncCallMatrix = new FileOutputStream(
					functionCallMatrixFile);

			StringBuffer line = new StringBuffer("");
			for (int i = 0; i <= dimension; i++) {
				for (int j = 0; j <= dimension; j++) {
					line.append(functionCallMatrix[i][j]);
					line.append(",");
				}
				line.append("\n");
				fosFuncCallMatrix.write(line.toString().getBytes());
				line.delete(0, line.length());
			}
			fosFuncCallMatrix.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}

	}

	public void writeToFileAsXls(String xmlFileName,String fileCsvFileName, 
		String methodCsvFileName) throws Exception {

	BufferedOutputStream outF = null;
	BufferedOutputStream outM = null;
	
	try {
		outF = new BufferedOutputStream(new FileOutputStream(
				fileCsvFileName));
		
		outM = new BufferedOutputStream(new FileOutputStream(
				methodCsvFileName));
		
		outF.write("File Name,".getBytes());
		outF.write("File Id,".getBytes());
		outF.write("Cyclometric Density,".getBytes());
		outF.write("Decision Density,".getBytes());
		outF.write("Essential Density,".getBytes());
		outF.write("Branch Count,".getBytes());
		outF.write("Condition Count,".getBytes());
		outF.write("Cyclometric Complexity,".getBytes());
		outF.write("Decision Count,".getBytes());
		outF.write("Essential Complexity,".getBytes());
		outF.write("LOC,".getBytes());
		outF.write("Total Operands,".getBytes());
		outF.write("Total Operators,".getBytes());
		outF.write("Unique Operands Count,".getBytes());
		outF.write("Unique Operators Count,".getBytes());
		outF.write("Halstead Difficulty,".getBytes());
		outF.write("Halstead Length,".getBytes());
		outF.write("Halstead Level,".getBytes());
		outF.write("Halstead Programming Effort,".getBytes());
		outF.write("Halstead Programming Time,".getBytes());
		outF.write("Halstead Volume,".getBytes());
		outF.write("Maintenance Severity,".getBytes());
		outF.write("Defected?(false/true) \n".getBytes());
		outF.flush();

		outM.write("File Name,".getBytes());
		outM.write("Method Name,".getBytes());
		outM.write("Method Id,".getBytes());
		outM.write("Cyclometric Density,".getBytes());
		outM.write("Decision Density,".getBytes());
		outM.write("Essential Density,".getBytes());
		outM.write("Branch Count,".getBytes());
		outM.write("Condition Count,".getBytes());
		outM.write("Cyclometric Complexity,".getBytes());
		outM.write("Decision Count,".getBytes());
		outM.write("Essential Complexity,".getBytes());
		outM.write("LOC,".getBytes());
		outM.write("Total Operands,".getBytes());
		outM.write("Total Operators,".getBytes());
		outM.write("Unique Operands Count,".getBytes());
		outM.write("Unique Operators Count,".getBytes());
		outM.write("Halstead Difficulty,".getBytes());
		outM.write("Halstead Length,".getBytes());
		outM.write("Halstead Level,".getBytes());
		outM.write("Halstead Programming Effort,".getBytes());
		outM.write("Halstead Programming Time,".getBytes());
		outM.write("Halstead Volume,".getBytes());
		outM.write("Maintenance Severity,".getBytes());
		outM.write("Formal Parameters,".getBytes());
		outM.write("Call Pair Length,".getBytes());
		outM.write("Defected?(false/true)\n".getBytes());
		outM.flush();

		int fileCount = 0;
		int methodCount = 0;

		
		
			
			// file metrics level

			for () {
				
				outF.write(",false\n".getBytes());
				outF.flush();
					// Method metrics level

					for () {
					
							outM.write(",false\n".getBytes());
				            outM.flush();
							}

		}

		outF.close();
		outM.close();

	} catch (Exception e) {
		outF.close();
		outM.close();
		e.printStackTrace();
	}

}
	
}
