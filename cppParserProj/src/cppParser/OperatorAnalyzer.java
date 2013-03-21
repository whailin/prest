package cppParser;

public class OperatorAnalyzer extends Analyzer 
{
	private int i = -1;
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
		this.tokens = tokens;
		
		for(i = 0; i < tokens.length; ++i)
		{
			// Early bail out on tokens that are too long to be delimiters
			if(tokens[i].length() > 2) continue;
			
			if(tokens[i].equals("\"")) openString = !openString;
			
			if(!openString && StringTools.isOperator(tokens[i]))
			{
				handleOperator();
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
				if(leftSide != null && canAddOperand(i-1)) 
				{
					objManager.currentFunc.addOperand(leftSide);
				}
			}
		}
		
		// Process the rest of the tokens
		for(i = i + 1; i < tokens.length; ++i)
		{
			if(StringTools.isOperator(tokens[i]))
			{
				//Log.d("Found operator: " + tokens[i]);
				op = constructOperator();
				objManager.currentFunc.addOperator(op);
			}
			else
			{
				//Log.d("Found something else: " + tokens[i]);
				if(tokens[i].equals(";")) continue;
				
				if(StringTools.isOperator(tokens[i-1]))
				{
					// TODO Construct a whole operand, if it consists of multiple tokens
					String operand = tokens[i];
					if(operand.equals("\"")) operand = constructStringLiteral(i, false);
					if(operand != null && canAddOperand(i))
					{
						objManager.currentFunc.addOperand(operand);
					}
					
				}
			}
		}
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
		
		switch(op)
		{
		case "+":
		case "-":
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
			break;
		case "*":
		case "/":
		case "%":
		case "&":
		case "|":
		case "^":
			if(tokens[i+1].equals("="))
			{
				op = op + tokens[i+1];
				i++;
			}
			break;
		case "=":
			if(tokens[i-1].equals(op))
			{
				op = op + op;
				
			}
			else if(tokens[i+1].equals(op))
			{
				op = op + op;
				i++;
			}
			else switch(tokens[i-1])
			{
			case "<":
			case ">":
			case "+":
			case "-":
			case "*":
			case "/":
			case "%":
				op = tokens[i-1] + op;
				
				break;
			}
			break;
		case "<":
		case ">":
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
			break;
		}
		
		return op;
	}
	
	/**
	 * Checks if the operand at index 'index' isn't added already
	 * by VarFinder.
	 * @param index The index of the operand
	 * @return 'true' if the operand isn't yet added, 'false' if it is already added
	 */
	private boolean canAddOperand(int index)
	{
		for(Integer integer : functionAnalyzer.getVarFinder().getHandledIndices())
		{
			if(integer.intValue() == i - 1)
			{
				return false;
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
