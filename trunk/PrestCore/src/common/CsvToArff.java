package common;

import weka.core.Instances;
import weka.core.converters.CSVLoader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

public class CsvToArff {

    public CsvToArff() {

    }

    public void convertProject(File projectPath) throws Exception {
	File csvSources = new File(projectPath.getPath() + "\\parse_results\\");
	String[] projectFiles = csvSources.list();

	for (int i = 0; i < projectFiles.length; i++) {
	    if (projectFiles[i].endsWith("csv")
		    && !projectFiles[i].startsWith("callGraph_Java")) {
		this.csvToArff(projectPath.getPath() + "\\parse_results\\"
			+ projectFiles[i], projectFiles[i], projectPath
			.getPath());
	    }

	}

    }

    private int csvToArff(String csvFilePath, String csvFileName,
	    String projectPath) {
	try {
	    CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(csvFilePath));
	    Instances data = loader.getDataSet();

	    BufferedWriter writer = new BufferedWriter(new FileWriter(
		    projectPath
			    + "\\arff_files\\"
			    + csvFileName.substring(0, csvFileName
				    .lastIndexOf('.')) + ".arff"));
	    writer.write(data.toString());
	    writer.flush();
	    writer.close();

	} catch (Exception e) {
	    System.out.println(e.getMessage());
	}
	return 1;
    }
}