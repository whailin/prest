package cppParser;

import cppParser.utils.Log;
import cppParser.utils.StringTools;

/**
 * Pass for analyzing the preprocessor directives.
 * This pass is done for all the files before the actual analysis
 * takes place.
 * 
 * @author Harri Pellikka
 */
public class PreprocessorPass {

	// List of deliminators used for tokenization
	private String[] delims = {" ", "#", "(", ")", ","};
	
	private Extractor extractor;
	
	// Current index of tokens
	private int i = 0;
	
	// List of current tokens
	private String[] tokens = null;
	
	/**
	 * Constructs a new preprocess pass analyzer
	 * @param e Extractor
	 */
	public PreprocessorPass(Extractor e)
	{
		this.extractor = e;
	}
	
	/**
	 * Processes a given line.
	 * @param Line to process
	 */
	public void process(String line)
	{
		if(line.startsWith("#"))
		{
			String[] tokens = StringTools.split(line, delims, true);
			analyze(tokens);
		}
	}

	/**
	 * Analyzes the given list of tokens
	 * @param tokens Tokens that form the sentence under analysis
	 */
	private void analyze(String[] tokens)
	{
		this.tokens = tokens;
		
		for(i = 0; i < tokens.length; ++i)
		{
			switch(tokens[i])
			{
			case "include":
				break;
			case "define":
				handleDefine();
				break;
			}
		}
	}
	
	/**
	 * Handles #define statements (constants, macros etc.)
	 */
	private void handleDefine()
	{
		if(tokens.length == 4)
		{
			Log.d("Define: name = " + tokens[2] + "    value = " + tokens[3]);
			
		}
		else
		{
			if(tokens[3].equals("("))
			{
				Log.d("Macro: " + tokens[2]);
				//TODO Handle macros properly
			}
		}
	}
}
