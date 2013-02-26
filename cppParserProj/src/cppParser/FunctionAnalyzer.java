package cppParser;

import java.util.ArrayList;
import java.util.HashMap;

import cppStructures.*;

/**
 * This class is responsible of analyzing and constructing functions found in the source code.
 * 
 * @author Harri Pellikka
 */
public class FunctionAnalyzer extends Analyzer {
	
	// Keywords that increment the cyclomatic complexity
	private static final String[] inFuncCCKeywords = {"for", "while", "if", "?", "case", "&&", "||", "#ifdef"};
	private static final String[] inFuncHalsteadOps = {"::", ";", "+", "-", "*", "/", "%", ".", "->", "<<", ">>", "<", "<=", ">", ">=", "!=", "==", "=", "&", "|"};
	
	public FunctionAnalyzer(SentenceAnalyzer sa)
	{
		super(sa);
	}
	
	/**
	 * Analyses the list of tokens to find out whether or not
	 * the tokens form a new function.
	 * @param tokens Tokens that form the sentence
	 * @return 'true' if a new function was found, 'false' otherwise
	 */
	private boolean processNewFunction(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{
		
			// Either a classname::function or a classname::member found
			if(tokens[i].equals("::") && i > 0)
			{
				if(SentenceAnalyzer.ignoreStd && tokens[i-1].equals("std")) continue;
				
				// Check if tokens form a function with a body
				if(isFuncWithBody(tokens, i))
				{
					Log.d("   FUNCTION " + tokens[i+1] + " START (file: " + Extractor.currentFile + " | line: " + Extractor.lineno + ")");
					
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
					
					ParsedObjectManager.getInstance().currentFunc.funcBraceCount = sentenceAnalyzer.braceCount;
					ParsedObjectManager.getInstance().currentFunc.fileOfFunc = Extractor.currentFile;
					
					return true;
				}
				else if(isFuncDecl(tokens, i))
				{
					
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Processes sentences that belong to a currently open function
	 * @param tokens Tokens that form the sentence to process
	 * @return 'true' if the sentence was processed, 'false' if not
	 */
	private boolean processCurrentFunction(String[] tokens)
	{
		currentOperands = new ArrayList<String>();
		
		// Pull the currentFunc to a local variable for fast and easy access
		CppFunc func = ParsedObjectManager.getInstance().currentFunc;
		
		for(int i = 0; i < tokens.length; ++i)
		{
			Log.d("      PCF: " + tokens[i]);
			
			// Check for cyclomatic complexity
			checkForCC(func, tokens, i);
			
			// Check for halstead complexity operators
			checkOpsAndOds(func, tokens, i);
			
		}
		
		for(String s : currentOperands)
		{
			Log.d("        Od: " + s);
		}
		
		Log.d();
		return true;
	}
	
	// private String[] currentOperands = null;
	private ArrayList<String> currentOperands = null;
	
	private void checkOpsAndOds(CppFunc func, String[] tokens, int i)
	{
		if(tokens[i].startsWith("++") || tokens[i].startsWith("--"))
		{
			func.addOperator(tokens[i].substring(0, 3));
			currentOperands.add(tokens[i].substring(2));
			Log.d("        Op: ++ or -- (pre)");
			return;
		}
		else if(tokens[i].endsWith("++") || tokens[i].endsWith("--"))
		{
			func.addOperator(tokens[i].substring(tokens[i].length() - 2));
			currentOperands.add(tokens[i].substring(0, tokens[i].indexOf(tokens[i].charAt(tokens[i].length() - 1))));
			Log.d("        Op: ++ or -- (post)");
			return;
		}else if(tokens[i].contains("->"))
		{
			
		}
		
		for(int j = 0; j < inFuncHalsteadOps.length; ++j)
		{
			
			if(inFuncHalsteadOps[j].equals(tokens[i]))
			{
				// Add the operator
				String op = tokens[i];
				if(tokens[i].equals("+") || tokens[i].equals("-"))
				{
					if(i > 0 && tokens[i-1].equals(tokens[i])) return;
					if(i < tokens.length - 1 && tokens[i+1].equals(tokens[i]))
					{
						op += tokens[i+1];
						
					}
				}
				
				if(!tokens[i].equals(";"))
				{
					func.addOperator(op);
					Log.d("        Op: " + (i) + ": " + op);
				}
				
				
				
				
				// Check for operand(s)
				checkForOperands(func, tokens, i, op);
			}
		}
	}
	
	private void checkForOperands(CppFunc func, String[] tokens, int i, String op)
	{
		if(op.equals("=") || op.equals("!=") || op.equals("==") || op.equals("&&"))
		{
			
			currentOperands.add(tokens[i-1]);
			currentOperands.add(tokens[i+1]);
			
		}
		else if(tokens[i].endsWith("++"))
		{
			String od = tokens[i].substring(0, tokens[i].indexOf("++"));
			currentOperands.add(od);
		}
		else if(tokens[i].startsWith("++"))
		{
			String od = tokens[i].substring(tokens[i].indexOf("++") + 1);
			currentOperands.add(od);
		}
		else if(op.equals("::"))
		{
			if(i < tokens.length - 3)
			{
				if(tokens[i+3].equals(";"))
				{
					currentOperands.add(tokens[i+2]);
				}
			}
		}
		else if(op.equals(";"))
		{
			if(i > 0 && !tokens[i-1].equals(")"))
			{
				currentOperands.add(tokens[i-1]);
			}else{
				for(int j = i - 2; j > 0; --j)
				{
					if(tokens[j].equals("("))
					{
						
						Log.d("      Found function call: " + tokens[j-1]);
						if(tokens[j-1].contains(".")) func.addOperator(".");
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Checks if the given token should increase the function's cyclomatic complexity
	 * @param func The function under analysis
	 * @param tokens The tokens to inspect
	 * @param i The iterator position for tokens
	 */
	private void checkForCC(CppFunc func, String[] tokens, int i)
	{
		for(int j = 0; j < inFuncCCKeywords.length; ++j)
		{
			if(tokens[i].equals(inFuncCCKeywords[j]))
			{
				// TODO: Check that && and || are only inside if clauses (or where they matter)
				func.incCC();
			}
		}
	}
	
	/**
	 * Decides whether or not the tokens should be interpreted as a possible
	 * new function or as a sentence in a currently open function
	 */
	public boolean processSentence(String[] tokens)
	{
		if(ParsedObjectManager.getInstance().currentFunc == null)
		{
			return processNewFunction(tokens);
		}
		else
		{
			return processCurrentFunction(tokens);
		}
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
