package cppParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cppStructures.*;

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
	
	/**
	 * Constructs a new function analyzer
	 * @param sa The sentence analyzer
	 */
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
				/*
				Log.d("      Function call > " + funcName + " | params: ");
				for(String s : params)
				{
					Log.d("         - " + s);
				}
				*/
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
		case "+":
		case "-":
		case "*":
		case "/":
			break;
		case ")":
			
			break;
		default:
			// TODO Change from 'default' case to actual function handling case
			index = handleFunctionCall(index);
			break;
		}
		
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
			
			// Log.d("        Op: (Pre) " + op);
			return;
		}
		else if(tokens[i].endsWith("++") || tokens[i].endsWith("--"))
		{
			addOperator(i, tokens[i].substring(tokens[i].length() - 2));
			addOperand(i, tokens[i].substring(0, tokens[i].indexOf(tokens[i].charAt(tokens[i].length() - 1))));
			Log.d("        Op: ++ or -- (post)");
			return;
		}
		
		if(tokens[i].contains("->"))
		{
			// TODO Handle pointer operator
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
			*/
			break;
		}
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

	
}