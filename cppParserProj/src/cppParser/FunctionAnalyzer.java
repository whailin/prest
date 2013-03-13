package cppParser;

import java.util.ArrayList;
import java.util.HashSet;

import cppStructures.*;
import cppParser.utils.VarFinder;

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
	private ArrayList<String> currentOperands = null;
	private ArrayList<String> currentOperators = null;
	private HashSet<Integer> handledOperatorIndices = new HashSet<Integer>();
	private HashSet<Integer> handledOperandIndices = new HashSet<Integer>();
	
	// Iteration index for the current sentence
	private int i = -1;
	
	// Tokens for the current sentence
	private String[] tokens = null;
	
	// The function currently under analysis
	private CppFunc func = null;
        
        //Helper class for finding variables
        private VarFinder varFinder=new VarFinder();
	
	/**
	 * Constructs a new function analyzer
	 * @param sa The sentence analyzer
	 */
	public FunctionAnalyzer(SentenceAnalyzer sa)
	{
		super(sa);
	}
	
	private String getScope(String[] tokens, int i)
	{
		for(int j = 1; j < i - 1; ++j)
		{
			if(tokens[j].equals("::"))
			{
				//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[j]);
				//ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[j-1]);
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
				// Log.d("   FUNCTION " + tokens[i-1] + " START (file: " + Extractor.currentFile + " | line: " + Extractor.lineno + ")");
				
				// Get scope
				String scope = getScope(tokens, i);
				if(scope == null) return false; // TODO Fix this
				sentenceAnalyzer.setCurrentScope(scope, false);
				
				String funcName = tokens[i-1];

				String returnType = "";

				// Parse the type backwards
				if(i > 1)
				{
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
				
				//ParsedObjectManager.getInstance().currentFunc.addOperator(returnType);		//operator return
				
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
					
					//ParsedObjectManager.getInstance().currentFunc.addOperator(paramType);		//operator type of arg
					//ParsedObjectManager.getInstance().currentFunc.addOperand(paramName);		//operator name of arg
				}
				
				ParsedObjectManager.getInstance().currentFunc = func;
				ParsedObjectManager.getInstance().currentScope.addFunc(ParsedObjectManager.getInstance().currentFunc);
				ParsedObjectManager.getInstance().currentFunc.funcBraceCount = sentenceAnalyzer.braceCount;
				ParsedObjectManager.getInstance().currentFunc.fileOfFunc = Extractor.currentFile;
				
				return true;
			}
			
			/*
			// Either a classname::function or a classname::member found
			if((tokens[i].equals("::") || (ParsedObjectManager.getInstance().currentScope != null && tokens[tokens.length - 1].equals("{"))) && i > 0)
			{
				if(SentenceAnalyzer.ignoreStd && tokens[i-1].equals("std")) continue;
				
				// Check if tokens form a function with a body
				if(isFuncWithBody(tokens, i))
				{
					Log.d("   FUNCTION " + tokens[i+1] + " START (file: " + Extractor.currentFile + " | line: " + Extractor.lineno + ")");
					
					if(ParsedObjectManager.getInstance().currentScope == null) sentenceAnalyzer.setCurrentScope(tokens[i-1], false);
					
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
			*/
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
                
                varFinder.findVariables(tokens);
		currentOperands = new ArrayList<String>();
		currentOperators = new ArrayList<String>();
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
			
			// Check for halstead complexity operators
			checkOpsAndOds();
			
		}
		/*
		Log.d("        Operands:");
		for(String s : currentOperands)
		{
			Log.d("         - " + s);
		}
		
		Log.d("        Operators:");
		for(String s : currentOperators)
		{
			Log.d("         - " + s);
		}
		*/
		
		// Log.d();
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
		// Store the function name
		String funcName = tokens[index-1];
		
		//ParsedObjectManager.getInstance().currentFunc.addOperand(funcName);
		//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[index]);
		
		// Check if the function call is parameterless
		if(tokens[index+1].equals(")"))
		{
			// Log.d("      (line: " + Extractor.lineno + ") Function call np > " + funcName);
			func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
			return index + 1;
		}
		
		ArrayList<String> params = new ArrayList<String>();
		String currentParam = "";
		// Loop through the parameters
		for(int j = index + 1; j < tokens.length; ++j)
		{
			switch(tokens[j])
			{
			case ")":
				// Close the function call
				if(!currentParam.equals(""))
				{
					params.add(currentParam);
				}
				// Log.d("      (line: " + Extractor.lineno + ") Function call > " + funcName);
				func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
				return j;
			case "(":
				// Recurse through inner function calls
				// j = handleFunctionCall(j);
				j = handleOpeningParenthesis(j);
				break;
			case ",":
				params.add(currentParam);
				currentParam = "";
				break;
			default:
				currentParam += " " + tokens[j];
				break;
			}
			
		}
		
		
		// This should never happen, but if it does, this is the last one that was checked
		return tokens.length - 1;
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
			// Log.d("      (line: " + Extractor.lineno + ") for-statement");
			func.recognizedLines.add("      (line: " + Extractor.lineno + ") for-statement");
			break;
		case "while":
			// Log.d("      (line: " + Extractor.lineno + ") while-statement");
			func.recognizedLines.add("      (line: " + Extractor.lineno + ") while-statement");
			break;
		case "if":
			// Log.d("      (line: " + Extractor.lineno + ") if-statement");
			func.recognizedLines.add("      (line: " + Extractor.lineno + ") if-statement");
			break;
		case "(":
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index]);	//operand after (
			break;
		case "+":
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index]);	//operand after +
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index-2]);	//operand before +
			break;
		case "-":
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index]);	//operand after -
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index-2]);	//operand before -
			break;
		case "*":
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index]);	//operand after *
			// ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index-2]);	//operand before *
			break;
		case "/":
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index]);	//operand after /
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index-2]);	//operand before /
			break;			
		case ")":
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index-2]);	//operand before )
			break;
		default:
			// TODO Change from 'default' case to actual function handling case
			index = handleFunctionCall(index);
			break;
		}
		
		ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[index-1]);		//operator at before index
		ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[index]);		//operand at index
		
		return index;
	}
	
	private void checkOpsAndOds()
	{
		// Early bail out on tokens that are too long to be delimiters
		if(tokens[i].length() > 2) return;
		
		if(tokens[i].startsWith("++") || tokens[i].startsWith("--"))
		{
			String op = tokens[i];			
			
			if(op.length() > 2)
			{
				op = op.substring(0, 3);
			}
			
			addOperator(i, op);
			addOperand(i, tokens[i].substring(2));
			
			ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[i]);
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[i-2]);
			
			// Log.d("        Op: (Pre) " + op);
			return;
		}
		else if(tokens[i].endsWith("++") || tokens[i].endsWith("--"))
		{
			addOperator(i, tokens[i].substring(tokens[i].length() - 2));
			addOperand(i, tokens[i].substring(0, tokens[i].indexOf(tokens[i].charAt(tokens[i].length() - 1))));
			Log.d("        Op: ++ or -- (post)");
			
			ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[i]);	//operator ++ or --
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[i-2]);	//operand before ++/--
			
			return;
		}
		
		if(tokens[i].contains("->"))
		{
			// TODO Handle pointer operator
			//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[i]);
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[i-1]);	//operand before ->
			ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[i+1]);	//operand after ->
		}
		else if(tokens[i].equals("("))
		{
			i = handleOpeningParenthesis(i);
		}
		
		// Check for a string literal
		else if(tokens[i].startsWith("\""))
		{
			String stringLiteral = tokens[i] + " ";
			if(tokens[i].endsWith("\""))
			{
				stringLiteral = stringLiteral.substring(0, stringLiteral.length());
			}
			else
			{
				for(int j = i + 1; j < tokens.length; ++j)
				{
					if(tokens[j].endsWith("\""))
					{
						stringLiteral += tokens[j].substring(0, tokens[j].length());
						break;
					}
					else
					{
						stringLiteral += tokens[j] + " ";
						i++;
					}					
				}
			}
			addOperand(i, stringLiteral);
			
		}
		
		ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[i]);	//operator at i position

		
		// Check for operators
		/*
		if(tokens[i].length() < 3)
		{
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
					
					// func.addOperator(op);
					addOperator(i, op);
					
					// Check for operand(s)
					checkForOperands(op);
				}
			}
		}
		*/
	}
	
	/**
	 * Adds an operand to the list of operands in the current line
	 * @param i Index of the operand in the token list
	 * @param t The operand token
	 */
	private void addOperand(int i, String t)
	{
		Integer integer = new Integer(i);
		if(!handledOperandIndices.contains(integer))
		{
			handledOperandIndices.add(integer);
			currentOperands.add(t);
		}
		else
		{
			Log.d("TRIED TO ADD AN OPERAND OF AN EXISTING INDEX");
		}
	}
	
	/**
	 * Adds an operator to the list of operators in the current line
	 * @param i Index of the operator in the token list
	 * @param t The operator token
	 */
	private void addOperator(int i , String t)
	{
		Integer integer = new Integer(i);
		if(!handledOperatorIndices.contains(integer))
		{
			handledOperatorIndices.add(integer);
			currentOperators.add(t);
		}
		else
		{
			Log.d("TRIED TO ADD AN OPERATOR OF AN EXISTING INDEX");
		}
	}
	
	/*
	private void checkForOperands(String op)
	{
		switch(op)
		{
		case "=":
		case "!=":
		case "==":
		case "&&":
		case "<=":
		case "<":
		case ">":
		case ">=":
			addOperand(i-1, tokens[i-1]);
			addOperand(i+1, tokens[i+1]);			
			break;
		case "::":
			if(i < tokens.length - 3)
			{
				if(tokens[i+3].equals(";"))
				{
					addOperand(i+2, tokens[i+2]);					
				}
				else if(tokens[i+2].equals("("))
				{
					addOperand(i+1, tokens[i+1]);					
				}
			}
			break;
		case ";":
			if(i > 0 && !tokens[i-1].equals(")"))
			{
				if(i > 1 && !tokens[i-2].equals("="))
				{
					addOperand(i-1, tokens[i-1]);					
				}
			}
			/*
			else
			{
				for(int j = i - 2; j > 0; --j)
				{
					if(tokens[j].equals("("))
					{
						if(tokens[j-1].contains("."))
						{
							addOperator(j-1, tokens[j-1]);
						}
						break;
					}
				}
			}
			
			break;
		}
		
		//ParsedObjectManager.getInstance().currentFunc.addOperator(op);
	}
	*/
	
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
