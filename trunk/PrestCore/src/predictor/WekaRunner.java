package predictor;
import java.util.Date;
import java.text.DateFormat;

import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.j48.*;
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.FileReader;
import weka.classifiers.Evaluation;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.*;

public class WekaRunner {

	public static String runWeka(String trainPath, String testPath,
			String algorithm, String preProcess, String CrossValidate) {
		String output = "";
		try {
			Instances trainData = new Instances(new BufferedReader(
					new FileReader(trainPath)));
			// setting class attribute
			trainData.setClassIndex(trainData.numAttributes() - 1);

			Instances testData = new Instances(new BufferedReader(
					new FileReader(trainPath)));
			if (preProcess == "Normalize") {
				trainData = Filter.useFilter(trainData, new Normalize());
				testData = Filter.useFilter(testData, new Normalize());
			}

			// setting class attribute
			testData.setClassIndex(testData.numAttributes() - 1);
			Classifier cls = null;
			if (algorithm == "Naive Bayes") {
				cls = new NaiveBayes();
			} else if (algorithm == "J48") {
				cls = new J48();
			}

			cls.buildClassifier(trainData);

			Evaluation eval = new Evaluation(trainData);

			if (CrossValidate == "true") {
				eval.crossValidateModel(cls, trainData, 10);
			} else {
				eval.evaluateModel(cls, testData);
			}
			
			Date now = new Date();
		    DateFormat df = DateFormat.getDateTimeInstance();
		    
			output = "Experiment Results\n" +  df.format(now) + "\n\n" + eval.toClassDetailsString() + eval.toMatrixString();
		} catch (Exception e) {
			output = "error in parser, examine your datasets...";
		}

		return output;
	}
}
