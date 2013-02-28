package cppParser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Base class for all the analyzers (except SentenceAnalyzer)
 * 
 * @author Harri Pellikka 
 */
public class Analyzer {

	protected SentenceAnalyzer sentenceAnalyzer;
	
	public Analyzer(SentenceAnalyzer sa)
	{
		this.sentenceAnalyzer = sa;
	}
	
	/**
	 * Processes the given sentence. Should be overridden.
	 * @param tokens Tokens that form the sentence
	 * @return 'True' if the sentence was processed, 'false' if it should be passed to the next analyzer
	 */
	public boolean processSentence(String[] tokens)
	{
		// TODO Do some basic processing / checking here?
		
		return false;
	}
}