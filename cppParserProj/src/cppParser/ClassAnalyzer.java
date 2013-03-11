package cppParser;

import cppStructures.*;

/**
 * Analyzer for header files / classes.
 * 
 * @author Harri Pellikka 
 */
public class ClassAnalyzer extends Analyzer {

	// List of tokens currently under analysis (stored here for performance)
	private String[] tokens = null;
	
	// Current index of the tokens (stored here for performance)
	private int i = 0;
	
	/**
	 * Constructs a new class analyzer
	 * @param sa The sentence analyzer
	 */
	public ClassAnalyzer(SentenceAnalyzer sa) {
		super(sa);
	}

	@Override
	/**
	 * (Derived from Analyzer.java)
	 * Processes a sentence
	 */
	public boolean processSentence(String[] tokens) {
		if(ParsedObjectManager.getInstance().currentFunc != null) return false;
		
		if(!processNewClass(tokens) && ParsedObjectManager.getInstance().currentScope != null)
		{
			return processCurrentClass(tokens);
		}
		return false;
	}

	/**
	 * Processes a sentence in the current class definition
	 * 
	 * @param tokens The tokens that form the current sentence
	 * @return 'true' if the line was processed successfully, 'false' otherwise.
	 */
	private boolean processCurrentClass(String[] tokens)
	{
		this.tokens = tokens;
		this.i = 0;
		
		
		
		// Check if the sentence forms a function declaration (or a function with a body)
		for(i = 0; i < tokens.length; ++i)
		{
			switch(tokens[i])
			{
			case "(":
				return handleOpeningParenthesis();
			case "enum":
				// TODO handle enums properly
				if(tokens[1].equals("class") || tokens[1].equals("struct"))
				{
					Log.d("\tFound a scope enum " + tokens[2] + "\n");					
				}
				else
				{
					Log.d("\tFound an enum " + tokens[1] + "\n");					
				}
				return true;
			case "typedef":
				// TODO handle typedefs properly
				Log.d("\tFound a typedef " + tokens[1] + "\n");				
				return true;
			}
			
		}
		
		// Check for a member variable declaration
		if(tokens.length > 2 && tokens[tokens.length - 1].equals(";"))
		{
			Log.d("\tFound a member variable: " + tokens[tokens.length - 2] + " (type: " + tokens[tokens.length - 3] + ")");
			//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[tokens.length-3]);		//operator type of variable
			//ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[tokens.length-2]);		//operand name of variable
			//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[tokens.length-1]);		//operator semi-colon
		}
		// Log.d();
		
		return true;
	}
	
	private boolean handleOpeningParenthesis()
	{
		if(i > 0 && tokens[i].equals("("))
		{
			Log.d("\tFound a function > " + tokens[i-1]);
			//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[i]); 	//operator ()
			//ParsedObjectManager.getInstance().currentFunc.addOperand(tokens[i-1]);	//operand name of function
			
			// Check if there's a body
			if(tokens[tokens.length - 1].equals("{"))
			{
				Log.d("\t... with a body, don't handle it here.");
				return false;
			}
			
			// Find out the return type of the function
			String type = "";
			if(i == 1)
			{
				if(type.startsWith("~")) type = "dtor";
				else type = "ctor";
			}
			else if(i > 1)
			{
				type = tokens[i-2];
			}
			
			String name = tokens[i-1];
			
			//ParsedObjectManager.getInstance().currentFunc.addOperator(type);	//operator type of function			
			
			// Create the CppFunc object
			CppFunc cf = new CppFunc(type, name);
			
			// Search for attributes
			if(!tokens[i+1].equals(")"))
			{
				String paramType = "";
				String paramName = "";
				boolean skipCommas = false;
				for(int j = i + 1; j < tokens.length - 1; ++j)
				{
					if(tokens[j].equals(")")) break;
					
					if(tokens[j].contains("<"))
					{

						//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[j]);	//operator comma
						

						skipCommas = true;
					}
					if(tokens[j].contains(">"))
					{
						skipCommas = false;
					}
					
					if(!skipCommas && tokens[j].equals(","))
					{

						CppFuncParam attrib = new CppFuncParam(paramType, paramName);
						cf.parameters.add(attrib);
						paramType = "";
						paramName = "";
					}
					else
					{
						if(tokens[j+1].equals(",") || tokens[j+1].equals(")") || tokens[j+1].equals("="))
						{
							if(skipCommas)
							{
								paramName = tokens[j];
								if(tokens[j+1].equals("="))
								{
									while(j < tokens.length - 1 && !tokens[j+1].equals(",") && !tokens[j+1].equals(")")) j++;
								}
							}
							//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[j+1]);	//operator comma
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
					cf.parameters.add(attrib);
				}
				
				//ParsedObjectManager.getInstance().currentFunc.addOperator(paramType);
				//ParsedObjectManager.getInstance().currentFunc.addOperand(paramName);
			}
			
			if(cf.parameters.size() > 0)
			{
				Log.d("\t\tParameters:");
				for(CppFuncParam cfa : cf.parameters)
				{
					Log.d("\t\t   " + cfa.type + " " + cfa.name);
				}
			}
			// Finally, store the CppFunc object
			cf.fileOfFunc = Extractor.currentFile;
			ParsedObjectManager.getInstance().currentScope.addFunc(cf);
			
			return true;
		}
		return false;
	}
	
	/**
	 * Checks the tokens for a possible new class declaration
	 * 
	 * @param tokens Tokens that form the sentence
	 * @return 'true' if new class was found, 'false' otherwise
	 */
	private boolean processNewClass(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("class"))
			{				
				Log.d("Found class ");
				if(tokens[tokens.length - 1].equals(";"))
				{
					// Found a forward declaration
					Log.d("    ... forward declaration: " + tokens[tokens.length - 2]);
					
					//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[tokens.length-1]);
					
					// Create the class but don't set it as current class
					CppClass cc = (CppClass)ParsedObjectManager.getInstance().addClass(tokens[tokens.length - 2]);
					cc.nameOfFile = Extractor.currentFile;
					
					return true;
				}
				else
				{
					// Found a class with definition
					boolean isInheriting = false;
					
					for(int j = i + 1; j < tokens.length - 1; ++j)
					{
						if(tokens[j].equals(":"))
						{
							isInheriting = true;
							Log.d("   ... called " + tokens[j-1]);							
							
							CppClass cc = (CppClass)ParsedObjectManager.getInstance().addClass(tokens[j-1]);
							cc.nameOfFile = Extractor.currentFile;
							cc.braceCount = sentenceAnalyzer.braceCount;
							cc.parentScope = ParsedObjectManager.getInstance().currentScope;
							
							// Check for all parents
							for(int k = j + 1; k < tokens.length; ++k)
							{
								if(tokens[k].equals(",") || tokens[k].equals("{"))
								{
									Log.d("    ... inherited from " + tokens[k-1]);
									
									ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[k]);	//operator comma									
									
									CppClass pcc = (CppClass)ParsedObjectManager.getInstance().addClass(tokens[k-1]);									
									pcc.addChild(cc);
									pcc.nameOfFile = Extractor.currentFile;
								}
							}
							
							// ParsedObjectManager.getInstance().currentScope = cc;
							sentenceAnalyzer.setCurrentScope(cc.getName(), true);
							
							break;
						}
					}
					
					// If no ancestors were found, create a class with no parents
					if(!isInheriting)
					{
						Log.d("   ... called " + tokens[tokens.length - 2]);						
						
						CppClass cc = (CppClass)ParsedObjectManager.getInstance().addClass(tokens[tokens.length - 2]);
						cc.nameOfFile = Extractor.currentFile;
						cc.braceCount = sentenceAnalyzer.braceCount;
						cc.parentScope = ParsedObjectManager.getInstance().currentScope;
						sentenceAnalyzer.setCurrentScope(cc.getName(), true);
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
}
