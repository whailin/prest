package cppParser;

import cppParser.utils.FunctionFinder;
import java.util.ArrayList;
import java.util.HashSet;

import cppStructures.*;
import cppParser.utils.VarFinder;
import java.util.List;

/**
 * This class is responsible of analyzing and constructing functions found in the source code.
 * 
 * @author Harri Pellikka
 */
public class FunctionAnalyzer extends Analyzer {
	
	// Keywords that increment the cyclomatic complexity
	private static final String[] inFuncCCKeywords = {"for", "while", "if", "?", "case", "&&", "||", "#ifdef"};
	// private static final String[] inFuncHalsteadOps = {"::", ";", "+", "-", "*", "/", "%", ".", "<<", ">>", "<", "<=", ">", ">=", "!=", "==", "=", "&", "|"};
	
	// Operands found from the sentence that is currently being processed
	private HashSet<Integer> handledOperatorIndices = new HashSet<Integer>();
	private HashSet<Integer> handledOperandIndices = new HashSet<Integer>();
	
	// Iteration index for the current sentence
	private int i = -1;
	
	// Tokens for the current sentence
	private String[] tokens = null;
	
	// The function currently under analysis
	private CppFunc func = null;
        
    // Helper class for finding variables and function calls
    private VarFinder varFinder = new VarFinder(this);
    private FunctionFinder funcFinder = new FunctionFinder(varFinder,func);
	
    private OperatorAnalyzer operatorAnalyzer;
    
    // Indicator for "open string" (this is true if odd number of quotes has been read)
    private boolean openString = false;
    
	/**
	 * Constructs a new function analyzer
	 * @param sa The sentence analyzer
	 */
	public FunctionAnalyzer(SentenceAnalyzer sa)
	{
		super(sa);
		this.operatorAnalyzer = new OperatorAnalyzer(sa, this);
	}
	
	public VarFinder getVarFinder()
	{
		return varFinder;
	}
	
	private String getScope(String[] tokens, int i)
	{
		for(int j = 1; j < i - 1; ++j)
		{
			if(tokens[j].equals("::"))
			{
				return tokens[j-1];
			}
		}
		
		// No scope was found from the tokens, return the currentScope from ParsedObjectManager
		if(ParsedObjectManager.getInstance().currentScope != null)
		{
			return ParsedObjectManager.getInstance().currentScope.getName();
		}
		
		return null;
	}
	
	/**
	 * Analyses the list of tokens to find out whether or not
	 * the tokens form a new function.
	 * @param tokens Tokens that form the sentence
	 * @return 'true' if a new function was found, 'false' otherwise
	 */
	private boolean processNewFunction(String[] tokens)
	{
		// Bail out instantly if there's no body starting
		if(!tokens[tokens.length - 1].equals("{")) return false;
		
		for(int i = 1; i < tokens.length; ++i)
		{
			if(tokens[i].equals("("))
			{
				Log.d("   FUNCTION " + tokens[i-1] + " START (file: " + Extractor.currentFile + " | line: " + Extractor.lineno + ")");
				
				// Get scope
				String scope = getScope(tokens, i);
				if(scope == null) return false; // TODO Fix this
				sentenceAnalyzer.setCurrentScope(scope, false);
				
				String funcName = tokens[i-1];

				String returnType = "";

				// Parse the type backwards
				if(i == 1)
				{
					returnType = "ctor";
					if(funcName.startsWith("~")) returnType = "dtor";
				}
				else
				{
					if(i == 1 && !tokens[0].contains("protected") && !tokens[0].contains("private")) 
						returnType = tokens[0];
					else if(i != 2) 
							returnType = tokens[i-2];
						else returnType = funcName;
					
					if(returnType.equals(tokens[i-1]) && i == 1)

					for(int j = i - 2; j >= 0; --j)

					{
						if(tokens[j].equals(":") || StringTools.isKeyword(tokens[j]))
						{
							break;
						}
						returnType = tokens[j] + (returnType.length() > 0 ? " " : "") + returnType;
					}
				}
				
				if(returnType == "")
				{
					returnType = "ctor";
					if(tokens[i-1].startsWith("~")) returnType = "dtor";
				}
				
				CppFunc func = new CppFunc(returnType, funcName);
				
				// Parse parameters
				if(!tokens[i+1].equals(")"))
				{
					String paramType = "";
					String paramName = "";
					for(int j = i + 1; j < tokens.length - 1; ++j)
					{
						if(tokens[j].equals(")")) break;
						
						if(tokens[j].equals(","))
						{
							CppFuncParam attrib = new CppFuncParam(paramType, paramName);
							func.parameters.add(attrib);
							paramType = "";
							paramName = "";
						}
						else
						{
							if(tokens[j+1].equals(",") || tokens[j+1].equals(")"))
							{
								paramName = tokens[j];
							}
							else
							{
								paramType += (paramType.length() > 0 ? " " : "") + tokens[j];
							}
						}
					}
					
					if(!paramType.equals("") && !paramName.equals(""))
					{
						CppFuncParam attrib = new CppFuncParam(paramType, paramName);
						func.parameters.add(attrib);
					}
				}
				
				ParsedObjectManager.getInstance().currentFunc = func;
				ParsedObjectManager.getInstance().currentScope.addFunc(ParsedObjectManager.getInstance().currentFunc);
				ParsedObjectManager.getInstance().currentFunc.funcBraceCount = sentenceAnalyzer.braceCount;
				ParsedObjectManager.getInstance().currentFunc.fileOfFunc = Extractor.currentFile;
				
				return true;
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
            varFinder.clearHandledIndices();
            varFinder.findVariables(tokens);
            funcFinder.findFunctions(tokens);
        
        
        operatorAnalyzer.processSentence(tokens);
        
		handledOperatorIndices.clear();
		handledOperandIndices.clear();
		this.i = 0;
		this.tokens = tokens;
		
		// Pull the currentFunc to a local variable for fast and easy access
		func = ParsedObjectManager.getInstance().currentFunc;
		
		for(i = 0; i < tokens.length; ++i)
		{
			// Check for cyclomatic complexity
			checkForCC();		
			/*
			if(tokens[i].equals("("))
			{
				handleOpeningParenthesis(i);
			}*/
		}
		
		// Finally, set varFinder's originalTokens to null
		varFinder.setOriginalTokens(null);
		
		return true;
	}
	
	
	/**
	 * Analyzes a function call.
	 * This method is recursive, meaning that if it finds another function call
	 * in the parameters, it will call itself to parse the inner function call.
	 * @param index The index in tokens if the opening parenthesis
	 * @return The index of the closing parenthesis
	 */
	private int handleFunctionCall(int index)
	{
		// Bail out if the index is zero (or negative) as there can't be a function call
		if(index < 1) return index;
		
		// Store the function name
		String funcName = tokens[index-1];
		
		// Check if the function call is parameterless
		if(tokens[index+1].equals(")"))
		{
            if(varFinder.isDefined(funcName))
            {
                Log.d(funcName + " is known variable, not function call...");
            }
            else
            {
            	Log.d("      (line: " + Extractor.lineno + ") Function call np > " + funcName);
            	func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
            }
			return index + 1;
		}
        
		// Owners List should contain the owners of the function call eg myObj in "myObj->hello();"
		List<String> owners = new ArrayList<>(); 
		List<List<String>> params = new ArrayList<>();
		List<String> currentParam = new ArrayList<>();
		
		boolean even;
        int skipped = 0;
        for(int j= 2; index-j >= 0;j++)
        {
            if(tokens[index-j].contentEquals("*"))
                skipped++;
            else
            {
                if((j + skipped) % 2 == 0)
                    even = true;
                else even = false;
                if(even)
                {
                    switch(tokens[index-j])
                    {
                        case "->":
                        case ".":
                            owners.add(0, tokens[index-j]);
                            break;
                        case "::":
                            Log.d("Line:" + Extractor.lineno + " contains :: when . or -> was expected");
                            break;
                        default:
                            break;
                    }
                }
                else
                {
                    owners.add(0, tokens[index-j]);
                }
            }
        }
        
        if(!owners.isEmpty())
        {
            String str = "";
            for(String s : owners)
            {
                str += s;
            }
            Log.d("Owner" + str);
        }
        
        // Loop through the parameters
		for(int j = index + 1; j < tokens.length; ++j)
		{
			switch(tokens[j])
			{
			case ")":
				// Close the function call
				if(!currentParam.isEmpty())
				{
					params.add(currentParam);
					//handleParameter(currentParam);
				}
				
                if(varFinder.isDefined(funcName))
                {
                    Log.d(funcName+" is known variable, not function call...");
                }
                else
                {
                    Log.d("      (line: " + Extractor.lineno + ") Function call > " + funcName);
                    func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
                }
                
				return j;
			case "(":
				// Recurse through inner function calls
				j = handleOpeningParenthesis(j);
				break;
			case ",":
				params.add(currentParam);
				//handleParameter(currentParam);
				currentParam = new ArrayList<>();
				break;
			default:
				currentParam.add(tokens[j]);
				break;
			}
		}
		
		// This should never happen, but if it does, this is the last one that was checked
		return tokens.length - 1;
	}
	
	/**
	 * Disseminates a parameter to see if it includes operators or operands
	 */
	private void handleParameter(String p)
	{
		String[] pTokens = StringTools.split(p, null, true);
	}
	
	/**
	 * Analyzes an opening parenthesis to find out what it means
	 * (function call, cast, ordering...) and calls an appropriate
	 * handling function.
	 * 
	 * Note that this function is a part of a recursive call chain.
	 * 
	 * @param index The index of the opening parenthesis
	 * @return The index of the closing parenthesis
	 */
	private int handleOpeningParenthesis(int index)
	{
		@SuppressWarnings("unused")
		int origIndex = index;
		
		if(index < 1) return index;
		// Check the token before the opening parenthesis
		switch(tokens[index-1])
		{
		case "for":
			Log.d("      (line: " + Extractor.lineno + ") for-statement");
			break;
		case "while":
			Log.d("      (line: " + Extractor.lineno + ") while-statement");
			break;
		case "if":
			Log.d("      (line: " + Extractor.lineno + ") if-statement");
			break;
		case "switch":
			Log.d("      (line: " + Extractor.lineno + ") switch-statement");
			break;
		default:
			// TODO Change from 'default' case to actual function handling case
			index = handleFunctionCall(index);
			break;
		}

		return index;
	}
	
	
	
	/**
	 * Checks if the given token should increase the function's cyclomatic complexity
	 * @param func The function under analysis
	 * @param tokens The tokens to inspect
	 * @param i The iterator position for tokens
	 */
	private void checkForCC()
	{
		for(int j = 0; j < inFuncCCKeywords.length; ++j)
		{
			if(tokens[i].equals(inFuncCCKeywords[j]))
			{
				// TODO: Check that && and || are only inside if clauses (or where they matter / change the path)
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
}
