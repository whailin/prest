package cppParser;

import java.util.ArrayList;

import cppParser.utils.Log;
import cppParser.utils.StringTools;
import cppStructures.CppDefine;

/**
 * Pass for analyzing the preprocessor directives.
 * This pass is done for all the files before the actual analysis
 * takes place.
 * 
 * @author Harri Pellikka
 */
public class PreprocessorPass {

	// List of deliminators used for tokenization
	private String[] delims = {" ", "#", "(", ")", ",", "*", "/", "+", "-", "<"};
	
	private Extractor extractor;
	
	// Current index of tokens
	private int i = 0;
	
	// List of current tokens
	private String[] tokens = null;
	
	private boolean functionLike = false;
	
	/**
	 * Constructs a new preprocess pass analyzer
	 * @param e Extractor
	 */
	public PreprocessorPass(Extractor e)
	{
		this.extractor = e;
	}
	
	/**
	 * Checks whether or not the line forms a "function-like" macro
	 * @param line Line to check
	 * @return True if the macro is function-like, false if not
	 */
	private boolean isFunctionLike(String line)
	{
		String[] spaceDelim = new String[] {" "};
		String[] tmpTokens = StringTools.split(line, spaceDelim, false);
		if(tmpTokens[1].contains("(")) return true;
		else return false;
	}
	
	/**
	 * Processes a given line.
	 * @param Line to process
	 */
	public void process(String line)
	{
		if(line.startsWith("#define"))
		{
			functionLike = isFunctionLike(line);
			
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
				handleInclude();
				break;
			case "define":
				handleDefine();
				break;
			}
		}
	}
	
	/**
	 * Checks if the #include statement refers to a file in the project,
	 * and if it does, it will store 
	 */
	private void handleInclude()
	{
		if(tokens.length > 2)
		{
			if(tokens[2].startsWith("\"") && tokens[2].endsWith("\""))
			{
				String filename = tokens[2].substring(1, tokens[2].length() - 1);
				ParsedObjectManager.getInstance().getCurrentFile().addInclude(filename);
			}
			else
			{
				Log.d("Angle bracket include? " + tokens[2]);
			}
		}
	}
	
	/**
	 * Handles #define statements (constants, macros etc.) and
	 * extracts CppDefine objects from them.
	 */
	private void handleDefine()
	{
		if(!functionLike)
		{
			// Constant definition
			String def = "";
			for(i = 3; i < tokens.length; ++i)
			{
				def += (def.length() > 0 ? " " : "") + tokens[i];
			}
			ParsedObjectManager.getInstance().getCurrentFile().addDefine(new CppDefine(tokens[2], def));
		}
		else
		{
			// Search for the parameters
			ArrayList<String> params = new ArrayList<String>();
			String param = "";
			for(i = 4; i < tokens.length; ++i)
			{
				if(tokens[i].equals(")"))
				{
					params.add(param);
					i++;
					break;
				}
				else if(tokens[i].equals(","))
				{
					params.add(param);
					param = "";
					continue;
				}
				else
				{
					param += tokens[i];
				}
			}
			
			// Construct the "replacement" definition
			String def = "";
			for( ; i < tokens.length; ++i)
			{
				def += (def.length() > 0 ? " " : "") + tokens[i];
			}
			
			if(def.length() > 0)
			{
				CppDefine cd = new CppDefine(tokens[2], params, def);
				ParsedObjectManager.getInstance().getCurrentFile().addDefine(cd);
			}
			else
			{
				// The parameter list was actually the definition
				String par = "";
				for(int k = 0; k < params.size(); ++k)
				{
					par += (par.length() > 0 ? " " : "") + params.get(k);
				}
				
				CppDefine cd = new CppDefine(tokens[2], par);
				ParsedObjectManager.getInstance().getCurrentFile().addDefine(cd);
			}
		}
	}
}
