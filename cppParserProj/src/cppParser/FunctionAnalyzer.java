package cppParser;

import cppStructures.*;

/**
 * This class is responsible of analyzing and constructing functions found in the source code.
 * 
 * @author Harri Pellikka
 */
public class FunctionAnalyzer {
	
	private SentenceAnalyzer sentenceAnalyzer;
	
	public FunctionAnalyzer(SentenceAnalyzer sa)
	{
		this.sentenceAnalyzer = sa;
	}
	
	public void processSentence(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{
			if(ParsedObjectManager.getInstance().currentFunc == null)
			{
				// Either a classname::function or a classname::member found
				if(tokens[i].equals("::") && i > 0)
				{
					if(SentenceAnalyzer.ignoreStd && tokens[i-1].equals("std")) continue;
					
					// Check if tokens form a function with a body
					if(isFuncWithBody(tokens, i))
					{
						Log.d("   FUNCTION " + tokens[i+1] + " START (line: " + Extractor.lineno + ")");
						
						sentenceAnalyzer.setCurrentScope(tokens[i-1], false);
						
						String currentFuncName = tokens[i+1];
						
						String returnType = tokens[0];
						if(returnType.equals(tokens[i-1]) && i == 1)
						{
							if(currentFuncName.startsWith("~")) returnType = "dtor";
							else returnType = "ctor";
						}else{
							if(i > 1)
							{
								for(int j = 1; j < i - 1; ++j)
								{
									returnType += (tokens[j].equals("*") ? "" : " ") + tokens[j];
								}
							}
						}

						ParsedObjectManager.getInstance().currentFunc = new CppFunc(returnType, currentFuncName);
						ParsedObjectManager.getInstance().currentScope.addFunc(ParsedObjectManager.getInstance().currentFunc);
						
						ParsedObjectManager.getInstance().currentFunc.funcBraceCount = SentenceAnalyzer.braceCount;
						sentenceAnalyzer.lineDone = true;
					}
					else if(isFuncDecl(tokens, i))
					{
						
					}
				}
			}
		}
		
		// TODO If function body starts, create a new function or fetch an existing function
		// TODO If function declared, create a new function
		// TODO If function ends, handle it(!)
	}
	
	/**
	 * Checks if the given tokens form a classname::function(); -type of line
	 * @param tokens The tokens that form the line
	 * @param di The index of found "::" token
	 * @return true if the line is of form classname::function(), false otherwise
	 */
	private boolean isFuncWithBody(String[] tokens, int di)
	{
		if(di > 0)
		{
			for(int i = 0; i < tokens[di-1].length(); ++i)
			{
				char c = tokens[di-1].charAt(i);
				if(c == '=') return false;
			}
		}
		
		if(tokens.length > di + 2)
		{
			if(tokens[di + 2].equals("("))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isFuncDecl(String[] tokens, int di)
	{
		return false;
	}
	
	private void functionComplete()
	{
		
	}
}
