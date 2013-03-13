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

			
			if(objManager.currentScope instanceof CppClass)
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
		
		// Index for an assignment operator (if there's one)
		int assignIndex = -1;
		
		// Check if the sentence forms a function declaration (or a function with a body)
		for(i = 0; i < tokens.length - 1; ++i)
		{
			switch(tokens[i])
			{
			case "(":
				return handleOpeningParenthesis();
			case "enum":
				// TODO handle enums properly
				if(tokens[i+1].equals("class") || tokens[i+1].equals("struct"))
				{
					Log.d("\tFound a scope enum " + tokens[i+2] + "\n");					
				}
				else
				{
					Log.d("\tFound an enum " + tokens[i + 1] + "\n");					
				}
				return true;
			case "typedef":
				// TODO handle typedefs properly
				Log.d("\tFound a typedef " + tokens[1] + "\n");				
				return true;
			case "struct":
				// TODO handle structs
				return true;
			case "=":
				assignIndex = i;
				break;
			}
		}
		
		// At this point, we know the line cannot be a function declaration.
		// Check for a member variable declaration
		if(tokens.length > 2)
		{
			String mvName = "", mvType = "";
			if(assignIndex > 0)
			{
				// Build the type backwards
				for(int j = assignIndex - 2; j >= 0; --j)
				{
					if(!StringTools.isKeyword(tokens[j]))
					{
						if(tokens[j].equals(":"))
						{
							if(j > 0 && !tokens[j-1].equals(":")) break;
						}
						
						mvType = tokens[j] + (mvType.equals("") ? "" : " ") + mvType;
						
						
					}
				}
				mvName = tokens[assignIndex - 1];
			}
			else
			{
				// Build the type backwards
				for(int j = tokens.length - 3; j >= 0; --j)
				{
					if(!StringTools.isKeyword(tokens[j]))
					{
						if(tokens[j].equals(":"))
						{
							if(j > 0 && !tokens[j-1].equals(":")) break;
						}
						
						mvType = tokens[j] + (mvType.equals("") ? "" : " ") + mvType;
					}
				}
				mvName = tokens[tokens.length - 2];
			}
			
			
			
			Log.d("\tVar: " + mvType + " | " + mvName);
			MemberVariable mv = new MemberVariable(mvType, mvName);
			objManager.currentScope.addMember(mv);
			return true;
		}
		
		Log.d("Could not process line " + Extractor.lineno);
		return false;
	}
	
	/**
	 * Handles function declarations.
	 * Currently working:
	 * - Function names
	 * - Return types
	 * - Parameter lists
	 * - Parameterless functions (either empty parameter list or 'void')
	 * Not yet implemented / problems:
	 * - Commas in template parameters cause false splitting of params (i.e. Foo<T, U> bar)
	 * - No separation between static, virtual, const etc. function attributes
	 * @return
	 */
	private boolean handleFunctionDeclaration()
	{
		// Check if there's a body and bail out
		if(tokens[tokens.length - 1].equals("{"))
		{
			Log.d("\t... with a body, don't handle it here.");
			return false;
		}
		
		// Find out the return type of the function
		String type = "";
		for(int j = i - 2; j >= 0; --j)
		{
			if(!tokens[j].equals("virtual"))
			{
				if(!StringTools.isKeyword(tokens[j]))
				{
					if(tokens[j].equals(":"))
					{
						if(j > 0)
						{
							if(!tokens[j-1].equals(":")) break;
							else if(StringTools.isKeyword(tokens[j-1])) break;
						}
					}
					
					type = tokens[j] + (type.length() > 0 ? " " : "") + type;
				}
			}
		}
		
		String name = tokens[i-1];
		
		if(type.equals(""))
		{
			if(name.contains(objManager.currentScope.getName()))
			{
				type = "ctor";
				if(name.startsWith("~")) type = "dtor";
			}
		}
		
		Log.d("\tFunction: " + type + " | " + name);

		// Create the CppFunc object
		CppFunc cf = new CppFunc(type, name);
		
		// Search for parameters
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
					skipCommas = true;
				}
				if(tokens[j].contains(">"))
				{
					skipCommas = false;
				}
				
				if(!skipCommas && (tokens[j].equals(",") || tokens[j].equals("=")))
				{
					CppFuncParam attrib = new CppFuncParam(paramType, paramName);
					cf.parameters.add(attrib);
					paramType = "";
					paramName = "";
					if(tokens[j].equals("=")) while(!tokens[j].equals(",") && !tokens[j+1].equals(")")) j++;
				}
				else
				{
					if(tokens[j+1].equals(",") || tokens[j+1].equals(")") || tokens[j+1].equals("="))
					{
						if(!paramType.equals("")) paramName = tokens[j];
						else paramType = tokens[j];
					}
					else
					{
						paramType += (paramType.length() > 0 ? " " : "") + tokens[j];
					}
				}
			}
			
			if(!paramType.equals("") || !paramName.equals(""))
			{
				if(!paramName.equals("") && !paramType.equals("void"))
				{
					CppFuncParam attrib = new CppFuncParam(paramType, paramName);
					cf.parameters.add(attrib);
				}
			}
		}
		
		// Print the parameters for debugging purposes
		if(cf.parameters.size() > 0)
		{
			Log.d("\t\tParameters:");
			for(CppFuncParam cfa : cf.parameters)
			{
				Log.d("\t\t   " + cfa.type + " | " + cfa.name);
			}
		}
		
		// Finally, store the CppFunc object
		cf.fileOfFunc = Extractor.currentFile;
		ParsedObjectManager.getInstance().currentScope.addFunc(cf);
		
		return true;
	}
	
	private boolean handleOpeningParenthesis()
	{
		if(i > 0 && tokens[i].equals("("))
		{
			return handleFunctionDeclaration();
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
