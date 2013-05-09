package cppParser;

import cppParser.utils.FunctionFinder;
import cppParser.utils.Log;
import cppParser.utils.StringTools;

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
	private static final String[] inFuncCCKeywords = {"for", "while", "if", "?", "case", "&&", "||", "#ifdef", "and", "or"};
	
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
    private FunctionFinder funcFinder;
	
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
        funcFinder=new FunctionFinder(this,varFinder,func);
	}
	
	public VarFinder getVarFinder()
	{
		return varFinder;
	}
    
    public void storeHandledIndex(Integer index)
    {
    	if(index.intValue() < 0) return;
    	
    	for(Integer storedIndex : getHandledIndices())
    	{
    		if(storedIndex.intValue() == index.intValue()) return;
    	}
    	
    	getHandledIndices().add(index);
    }
	
	private String getScope(String[] tokens, int i)
	{
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
		else if(ParsedObjectManager.getInstance().currentNamespace != null)
		{
			return ParsedObjectManager.getInstance().currentNamespace.getName();
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
		if(!tokens[tokens.length - 1].equals("{")) return false;
		
		for(int i = 1; i < tokens.length; ++i)
		{
			if(tokens[i].equals("typedef")) return false;
			if(tokens[i].equals(",") || tokens[i].equals("=") || tokens[i].equals("{")) return false;
			else if(tokens[i].equals("("))
			{
				if(tokens[i-1].equals("operator")) continue;
				
				// Check for operators before "(" -> only "::" and "~" is allowed
				for(int j = i - 1; j >= 0; --j)
				{
					if(StringTools.isOperator(tokens[j]) && !tokens[j].equals("::") && !tokens[j].equals("~")) return false;
				}
				
				// Get the "zero-level" parenthesis pair count (if > 1, preprocessor directives do messy things)
				int parCount = 1, zeroParCount = 0;
				boolean isThrow = false;
				int lastParenthesisIndex = i;
				for(int z = i + 1; z < tokens.length; ++z)
				{
					if(tokens[z].equals("{")) break;
					else if(tokens[z].equals(":")) break;
					else if(tokens[z].equals("("))
					{
						lastParenthesisIndex = z;
						if(tokens[z-1].equals("throw")) isThrow = true;
						
						parCount++;
					}
					else if(tokens[z].equals(")"))
					{
						parCount--;
						if(isThrow)
						{
							isThrow = false;
						}
						else
						{
							if(parCount == 0) zeroParCount++;
						}
					}
				}
				if(zeroParCount > 1)
				{
					Log.e("Two function declarations on the same line\n  File: " + Extractor.currentFile + "\n  Line: " + Extractor.lineno);
					i = lastParenthesisIndex;
				}
				
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
						if(tokens[j].equals(")")) break;
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
				else if(returnType.equals("~" + funcName) || returnType.endsWith("~"))
				{
					returnType = "dtor";
				}
				else if(returnType == "")
				{
					returnType = "ctor";
					if(tokens[i-1].startsWith("~")) returnType = "dtor";
				}
				
				// Sanitize the return type from keywords (const, virtual etc.)
				if(returnType.contains("const"))
				{
					returnType.replace("const", "");
				}
				if(returnType.contains("virtual"))
				{
					returnType.replace("virtual", "");
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
				func = ParsedObjectManager.getInstance().addFunction(func, tokens[tokens.length - 1].equals("{"));
				func.funcBraceCount = sentenceAnalyzer.braceCount;
				func.fileOfFunc = Extractor.currentFile;
				
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
		if(tokens.length == 1 && tokens[0].equals("}"))
		{
			
		}
		
		handledIndices.clear();
        // varFinder.clearHandledIndices();
        // varFinder.findVariables(tokens);
        // funcFinder.findFunctions(tokens);        
        
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
