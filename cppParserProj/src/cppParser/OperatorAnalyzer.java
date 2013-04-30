package cppParser;

import cppParser.utils.Log;
import cppParser.utils.StringTools;

/**
 * Analyses lists of tokens and tries to figure out operands and operators.
 * 
 * @author Harri Pellikka
 */
public class OperatorAnalyzer extends Analyzer 
{
	// List of operators that tend to pop up in the operand list (and thus they should be skipped)
	private String[] operandSkipList = {"{", "(", ";", "=", "]", "::"};
	
	// Extra splitters
	private String[] splitters = {"!", "~"};
	
	private int i = -1;
	private String[] tokens = null;
	
	private boolean openString = false;
	
	private FunctionAnalyzer functionAnalyzer = null;
	
	/**
	 * Constructs a new operator analyze
	 * @param sa Sentence analyzer
	 * @param fa Function analyzer
	 */
	public OperatorAnalyzer(SentenceAnalyzer sa, FunctionAnalyzer fa) {
		super(sa);
		this.functionAnalyzer = fa;
	}

	@Override
	/**
	 * Processes the tokens in order to find operators and operands
	 */
	public boolean processSentence(String[] tokens) {
		
		// Split and reconstruct the tokens
		this.tokens = StringTools.reconstructOperators(StringTools.split(tokens, splitters, true));
		
		// Loop through the tokens
		for(i = 0; i < this.tokens.length; ++i)
		{
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
		// Handle bracket pairs
		int bracketPairIndex = StringTools.getBracketPair(tokens, i);
		if(bracketPairIndex > -1)
		{
			// Log.d("Found brace pair " + tokens[Math.min(i, bracketPairIndex)] + " " + tokens[Math.max(i, bracketPairIndex)]);
			if(functionAnalyzer.getHandledIndices().contains(new Integer(bracketPairIndex)))
			{
				// Log.d("  Already handled.");
			}
			else
			{
				functionAnalyzer.getHandledIndices().add(new Integer(bracketPairIndex));
			}
		
		}
		
		int origIndex = i;
		String op = tokens[i];
		
		// If the operator is inc. or dec., set the PRE or POST label accordingly
		if(op.equals("++") || op.equals("--"))
		{
			if(i == 0)
			{
				op += " PRE";
			}
			else op += " POST";
		}
		
		objManager.currentFunc.addOperator(op);
		functionAnalyzer.getHandledIndices().add(new Integer(origIndex));
		
		// If there's a "left side" for the operator, handle that
		if(i > 0)
		{
			String leftSide = tokens[origIndex-1];
			
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
				// Operator found, construct and store it
				origIndex = i;
				op = tokens[i];
				if(canAddOperator(i))
				{
					if(op.equals("++") || op.equals("--"))
					{
						op += " PRE";
					}
					objManager.currentFunc.addOperator(op);
					functionAnalyzer.getHandledIndices().add(new Integer(origIndex));
				}
			}
			else
			{
				if(StringTools.isKeyword(tokens[i]))
				{
					// Operator found, construct and store it
					objManager.currentFunc.addOperator(tokens[i]);
					functionAnalyzer.getHandledIndices().add(new Integer(origIndex));
				}
				else
				{
					// Operand found, construct and store it
					String operand = tokens[i];
					if(operand.equals("\"")) operand = constructStringLiteral(i, false);
					if(operand != null && canAddOperand(i))
					{
						objManager.currentFunc.addOperand(operand);
						functionAnalyzer.getHandledIndices().add(new Integer(i));
						
						// Check for possible post-increment or post-decrement operator
						if(i < tokens.length - 1)
						{
							if(tokens[i+1].equals("++") || tokens[i+1].equals("--"))
							{
								objManager.currentFunc.addOperator(tokens[i+1] + " POST");
								functionAnalyzer.getHandledIndices().add(new Integer(i+1));
								i++;
							}
						}
					}
				}
			}
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
