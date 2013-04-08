package cppParser;

import java.util.ArrayList;

import cppParser.utils.StringTools;

public class OperatorAnalyzer extends Analyzer 
{
	private String[] operandSkipList = {"{", "(", ";", "="};
	
	private String[] splitters = {"!", "~"};
	
	private int i = -1;
	private String[] origTokens = null;
	private String[] tokens = null;
	
	private boolean openString = false;
	
	private FunctionAnalyzer functionAnalyzer = null;
	
	public OperatorAnalyzer(SentenceAnalyzer sa, FunctionAnalyzer fa) {
		super(sa);
		this.functionAnalyzer = fa;
	}

	@Override
	/**
	 * Processes the tokens in order to find operators and operands
	 */
	public boolean processSentence(String[] tokens) {
		i = 0;
		this.origTokens = tokens;
		this.tokens = StringTools.split(tokens, splitters, true);
		
		for(i = 0; i < this.tokens.length; ++i)
		{
			// Early bail out on tokens that are too long to be delimiters
			// if(tokens[i].length() > 2) continue;
			
			if(this.tokens[i].equals("\"")) openString = !openString;
			
			if(!openString)
			{
				if(StringTools.isOperator(this.tokens[i]))
				{
					handleOperator();
				}
				else if(StringTools.isKeyword(this.tokens[i]) && !StringTools.isPrimitiveType(this.tokens[i]))
				{
					objManager.currentFunc.addOperator(this.tokens[i]);
				}
			}
		}
		
		return true;
	}

	/**
	 * When an operator is found, this method handles the line,
	 * searching for operators and operands in the sentence.
	 */
	private void handleOperator()
	{
		if(i < 1) return;
		
		int origIndex = i;
		String op = constructOperator();
		
		objManager.currentFunc.addOperator(op);
		functionAnalyzer.getHandledIndices().add(new Integer(origIndex));
		
		if(!op.startsWith("++") && !op.startsWith("--"))
		{
			String leftSide = tokens[origIndex-1];
			// Log.d("Leftside: " + leftSide);
			
			if(leftSide.equals("\""))
			{
				leftSide = constructStringLiteral(origIndex-i, true);
			}
			
			// Add the leftside operand
			if(!StringTools.isOperator(leftSide))
			{
				if(leftSide != null && canAddOperand(origIndex-1)) 
				{
					objManager.currentFunc.addOperand(leftSide);
					functionAnalyzer.getHandledIndices().add(new Integer(origIndex-1));
				}
			}
		}
		
		// Process the rest of the tokens
		for(i = i + 1; i < tokens.length; ++i)
		{
			if(StringTools.isOperator(tokens[i]))
			{
				origIndex = i;
				op = constructOperator();
				if(canAddOperator(i))
				{
					objManager.currentFunc.addOperator(op);
					functionAnalyzer.getHandledIndices().add(new Integer(origIndex));
				}
				else
				{
					if(tokens[i].equals("."))
					{
						constructFloatingPointOperand();
					}
				}
			}
			else
			{
				if(StringTools.isKeyword(tokens[i]))
				{
					objManager.currentFunc.addOperator(tokens[i]);
					functionAnalyzer.getHandledIndices().add(new Integer(origIndex));
				}
				else
				{
				
					// TODO Construct a whole operand, if it consists of multiple tokens
					String operand = tokens[i];
					if(operand.equals("\"")) operand = constructStringLiteral(i, false);
					if(operand != null && canAddOperand(i))
					{
						objManager.currentFunc.addOperand(operand);
						functionAnalyzer.getHandledIndices().add(new Integer(i));
					}
				}
			}
		}
	}
	
	private void constructFloatingPointOperand()
	{
		String od = "";
		for(int j = i - 1; j < i + 2 && j < tokens.length; ++j)
		{
			od += tokens[j];
			i = j;
		}
		objManager.currentFunc.addOperand(od);
		
	}
	
	/**
	 * Checks if the given 'possible operand' should be skipped.
	 * @param t The operand to inspect
	 * @return 'True' if the operand should be skipped (not an operand), 'false' otherwise
	 */
	private boolean skipPossibleOperand(String t)
	{
		for(String s : operandSkipList)
		{
			if(t.equals(s)) return true;
		}
		return false;
	}
	
	/**
	 * Constructs an operator from the token at the current index
	 * and stores the related operand, if the operator is a unary operator (!, ++ or --).
	 * 
	 * The main purpose for this method is to check if the token
	 * is part of a multi-token operator, for example when "<" is found,
	 * this method checks if there is a "=" following it.
	 * 
	 * @return String representation of the operator
	 */
	private String constructOperator()
	{
		String op = tokens[i];
		
		switch(tokens[i])
		{
		case "~":
			return handleBitwiseNotOperator();
		case "+":
		case "-":
			return constructPlusMinusOperator();
		case "*":
		case "/":
		case "%":
		case "&":
		case "|":
		case "^":
		case "=":
		case "!":
			return constructCompoundOperator();
		case "<":
		case ">":
		case "<<":
		case ">>":
			return constructAngleBracketOperator();
		}
		
		return op;
	}
	
	/**
	 * Handles the bitwise NOT operator (~)
	 * @return The bitwise NOT operator
	 */
	private String handleBitwiseNotOperator()
	{
		objManager.currentFunc.addOperand(tokens[i+1]);
		i++;
		return "~";
	}
	
	/**
	 * Constructs an operator that has '<' or '>' in it.
	 * @return The constructed operator ('<', '<<', '<=', '<<=' etc.)
	 */
	private String constructAngleBracketOperator()
	{
		String op = tokens[i];
		if(tokens[i+1].equals("="))
		{
			op += "=";
			i++;
		}
		else if(tokens[i+1].equals(op) || tokens[i-1].equals(op))
		{
			op = op + op;
			i++;
			if(tokens[i+1].equals("="))
			{
				op += "=";
				i++;
			}
		}
		return op;
	}
	
	/**
	 * Constructs an operator that may be a part of a compoun
	 * operator (i.e. + can be a part of +=)
	 * @return The operator itself, or the compound operator if one is found.
	 */
	private String constructCompoundOperator()
	{
		String op = tokens[i];
		if(tokens[i+1].equals("="))
		{
			op += "=";
			i++;
		}
		return op;
	}
	
	/**
	 * Constructs an operator which has '+' or '-' in it.
	 * @return The constructed operator ('+', '++', '+=' etc.)
	 */
	private String constructPlusMinusOperator()
	{
		String op = tokens[i];
		
		String compound = constructCompoundOperator();
		if(!compound.equals(op)) return compound;
		else
		{
			if(tokens[i-1].equals(op) && !tokens[i+1].equals(op))
			{
				op = op + op;
				op += " PRE";
				// TODO Check if the attached operand is "real"
				if(canAddOperand(i+1)) objManager.currentFunc.addOperand(tokens[i+1]);
				i++;
			}
			else if(tokens[i+1].equals(op) && !tokens[i-1].equals(op))
			{
				op = op + op;
				op += " POST";
				// TODO Check if the attached operand is "real"
				if(canAddOperand(i-1)) objManager.currentFunc.addOperand(tokens[i-1]);
				i++;
			}
			else if(tokens[i+1].equals("="))
			{
				op = op + tokens[i+1];
				i++;
			}
			
			return op;
		}
	}
	
	/**
	 * Checks if the operand at index 'index' isn't added already
	 * by VarFinder and if the operand is in fact not a suitable operand.
	 * @param index The index of the operand
	 * @return 'true' if the operand isn't yet added, 'false' if it is already added
	 */
	private boolean canAddOperand(int index)
	{
		// Check if the index is already handled
		for(Integer integer : functionAnalyzer.getHandledIndices())
		{
			if(integer.intValue() == index)
			{
				return false;
			}
		}
		
		// Check if the token at the index is in the "skip list"
		for(String s : operandSkipList)
		{
			if(tokens[index].equals(s))
			{
				return false;
			}
		}
		
		// If the operator is '.', check that it's a method call, not a decimal point
		if(index < tokens.length - 2 && tokens[index+1].equals("."))
		{
			try
			{
				Integer.parseInt(tokens[index]);
				return false;
			}
			catch(NumberFormatException e)
			{
				return true;
			}
		}
		
		return true;
	}
	
	private boolean canAddOperator(int index)
	{
		switch(tokens[index])
		{
		case ".":
			try
			{
				Integer.parseInt(tokens[index-1]);
				return false;
			}
			catch(NumberFormatException e)
			{
				return true;
			}
		}
		return true;
	}
	
	/**
	 * Constructs a string literal from the given index onwards until the closing '"' is found.
	 * @param index The index of the beginning '"'
	 * @param reverse If 'true', the search is done backwards (the current index is the ending '"')
	 * @return The string literal
	 */
	private String constructStringLiteral(int index, boolean reverse)
	{
		String literal = "";
		if(!reverse)
		{
			for(int j = index; j < tokens.length; ++j)
			{
				literal += tokens[j];
				if(j != index && tokens[j].equals("\""))
				{
					i = j;
					break;
				}
			}
		}
		else
		{
			for(int j = index; j >= 0; --j)
			{
				literal = tokens[j] + literal;
				if(j != index && tokens[j].equals("\""))
				{
					i = index ;
					break;
				}
			}
		}
		if(!literal.startsWith("\"") || !literal.endsWith("\"")) return null;
		return literal;
	}
}
