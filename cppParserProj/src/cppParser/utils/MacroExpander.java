package cppParser.utils;

import java.util.ArrayList;

import cppParser.Extractor;
import cppParser.ParsedObjectManager;
import cppStructures.CppDefine;
import cppStructures.CppFile;

/**
 * Expands preprocessor macros during the main pass analysis
 * @author Harri Pellikka
 *
 */
public class MacroExpander {

	private static ArrayList<CppDefine> currentDefines = null;
	
	/**
	 * Setups the needed macro definitions for the analysis of the given file.
	 * In practice this means that all the #defines found in the given file AND
	 * in the recursive #include tree are loaded for future macro expansions.
	 * @param file Current file
	 */
	public static void setup()
	{
		currentDefines = new ArrayList<CppDefine>();
		currentDefines = ParsedObjectManager.getInstance().getCurrentFile().getDefinesRecursively(null);
	}
	
	/**
	 * Checks a given list of tokens for possible macro expansions.
	 * If macros are found, the resulting list will contain the "final"
	 * form of the sentence.
	 * @param tokens Original list of tokens
	 * @return List of tokens with possible macros expanded
	 */
	public static String[] expand(String[] tokens)
	{
		String[] newTokens = tokens;
		
		for(int i = 0; i < newTokens.length; ++i)
		{
			// for(CppDefine cd : ParsedObjectManager.getInstance().getDefines())
			for(CppDefine cd : currentDefines)
			{
				if(newTokens[i].equals(cd.getName()))
				{
					// Expand
					if(cd.getParameters() != null && cd.getParameters().size() > 0)
					{
						newTokens = handleExpansion(newTokens, i, cd);
					}
					else
					{
						newTokens = replace(newTokens, i, cd);
					}
				}
			}
		}
		
		return newTokens;
	}
	
	private static String[] replace(String[] tokens, int i, CppDefine cd)
	{
		String[] spaceDelim = new String[] {" "};
		String[] definitionTokens = StringTools.split(cd.getDefinition(), spaceDelim, true);
		String[] expandedDefinition = MacroExpander.expand(definitionTokens);
		
		ArrayList<String> newTokens = new ArrayList<String>();
		for(int k = 0; k < i; ++k)
		{
			if(tokens[k].length() > 0) newTokens.add(tokens[k]);
		}
		// if(cd.getDefinition().length() > 0) newTokens.add(cd.getDefinition());
		for(String s : expandedDefinition)
		{
			newTokens.add(s);
		}
		for(int k = i + 1; k < tokens.length; ++k)
		{
			if(tokens[k].length() > 0) newTokens.add(tokens[k]);
		}
		
		return expand(StringTools.listToArray(newTokens));
	}
	
	private static String[] handleExpansion(String[] tokens, int i, CppDefine cd)
	{
		String repString = "";
		ArrayList<String> params = new ArrayList<String>();
		String param = "";
		int pCount = 0;
		int j = i;
		for( ; j < tokens.length; ++j)
		{
			repString += (repString.length() > 0 ? " " : "") + tokens[j];
			
			if(tokens[j].equals("(")) pCount++;
			else if(tokens[j].equals(")"))
			{
				pCount--;
				if(pCount == 0)
				{
					params.add(param);
					break;
				}
			}
			else if(j > i + 1 && pCount == 1 && tokens[j].equals(","))
			{
				params.add(param);
				param = "";
			}
			
			if(!tokens[j].equals(","))
			{
				if(j > i + 1) param += (param.length() > 0 ? " " : "") + tokens[j];
			}
		}
		
		ArrayList<String> tmpParams = new ArrayList<String>(params);
		for(int k = 0; k < params.size(); ++k)
		{
			String[] tmpParamsArray = expand(new String[] {params.get(k)});
			String tmpParamString = "";
			for(String s : tmpParamsArray) param += (tmpParamString.length() > 0 ? " " : "") + s;
			tmpParams.remove(k);
			tmpParams.add(k, tmpParamString);
		}
		
		
		String[] defLine = StringTools.split(cd.getDefinition(), null, true);
		for(int k = 0; k < params.size(); ++k)
		{
			for(int l = 0; l < defLine.length; ++l)
			{
				if(defLine[l].equals(cd.getParameters().get(k)))
				{
					defLine[l] = params.get(k);
				}
			}
		}
		
		// Form the new array of tokens
		ArrayList<String> newTokens = new ArrayList<String>();
		
		// First, add all the tokens before the macro call
		for(int k = 0; k < i; ++k)
		{
			if(tokens[k].length() > 0) newTokens.add(tokens[k]);
		}
		
		// Second, add all the tokens expanded from the macro
		for(int k = 0; k < defLine.length; ++k)
		{
			if(defLine[k].length() > 0) newTokens.add(defLine[k]);
		}
		
		// Third, add the rest of the tokens from the original list of tokens
		for(int k = j + 1; k < tokens.length; ++k)
		{
			if(tokens[k].length() > 0) newTokens.add(tokens[k]);
		}
		
		return expand(StringTools.listToArray(newTokens));
	}
	
}
