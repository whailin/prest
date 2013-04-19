package cppParser;

import cppParser.utils.FunctionFinder;
import cppParser.utils.Log;
import cppParser.utils.StringTools;

import java.util.ArrayList;
import java.util.HashSet;

import cppStructures.*;
import cppParser.utils.VarFinder;
import java.util.List;
import profiling.Stats;

/**
 * This class is responsible of analyzing and constructing functions found in the source code.
 * 
 * @author Harri Pellikka
 */
public class FunctionAnalyzer extends Analyzer {
	public static int pcf=0;
	// Keywords that increment the cyclomatic complexity
	private static final String[] inFuncCCKeywords = {"for", "while", "if", "?", "case", "&&", "||", "#ifdef", "and", "or"};
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
    
    // Stores "handled" indices
    private ArrayList<Integer> handledIndices = new ArrayList<Integer>();
    
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
		/*
		for(int j = 1; j < i - 1; ++j)
		{
			if(tokens[j].equals("::"))
			{
				String scope = tokens[j-1];
				if(scope.startsWith("&") || scope.startsWith("*"))
				{
					scope = scope.substring(1);
				}
				return scope;
			}
		}
		*/
		
		if(i > 2)
		{
			if(tokens[i-2].equals("::"))
			{
				return tokens[i-3];
			}
		}
		
		// No scope was found from the tokens, return the currentScope from ParsedObjectManager
		if(ParsedObjectManager.getInstance().currentScope != null)
		{
			return ParsedObjectManager.getInstance().currentScope.getName();
		}
		
		return "__MAIN__";
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
				// Log.d("   FUNCTION " + tokens[i-1] + " START (file: " + Extractor.currentFile + " | line: " + Extractor.lineno + ")");
				
				// Get scope
				String scope = getScope(tokens, i);
				if(scope == null)
				{
					Log.d("   SCOPE WAS NULL");
					return false; // TODO Fix this
				}
				
				sentenceAnalyzer.setCurrentScope(scope, false);
				
				String funcName = tokens[i-1];

				String returnType = "";

				// Parse the type backwards
				if(i > 2 && tokens[i-2].equals("::"))
				{
					for(int j = i - 4; j >= 0; --j)
					{
						returnType = tokens[j] + (returnType.length() > 0 ? " " : "") + returnType;
					}
				}
				else
				{
					for(int j = i - 2; j >= 0; --j)
					{
						returnType = tokens[j] + (returnType.length() > 0 ? " " : "") + returnType;
					}
				}
				
				if(returnType.equals(funcName))
				{
					returnType = "ctor";
				}
				else if(returnType.equals("~" + funcName))
				{
					returnType = "dtor";
				}
				else if(returnType == "")
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
                            func.addMember(new MemberVariable(paramType, paramName));
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
                        func.addMember(new MemberVariable(paramType, paramName));
					}
				}
				
				func.funcBraceCount = sentenceAnalyzer.braceCount;
				func.fileOfFunc = Extractor.currentFile;
				ParsedObjectManager.getInstance().addFunction(func, true);
				
				/*
				ParsedObjectManager.getInstance().currentFunc = func;
				ParsedObjectManager.getInstance().currentScope.addFunc(ParsedObjectManager.getInstance().currentFunc);
				ParsedObjectManager.getInstance().currentFunc.funcBraceCount = sentenceAnalyzer.braceCount;
				ParsedObjectManager.getInstance().currentFunc.fileOfFunc = Extractor.currentFile;
				*/
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
		handledIndices.clear();
		pcf++;
        // varFinder.clearHandledIndices();
        varFinder.findVariables(tokens);
        long time=System.currentTimeMillis();
        funcFinder.findFunctions(tokens);
        
        time=System.currentTimeMillis()-time;
        Stats.addTime("funcFinder.findFunctions(String[])", time);
        
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

	public ArrayList<Integer> getHandledIndices() {
		return handledIndices;
	}
}
