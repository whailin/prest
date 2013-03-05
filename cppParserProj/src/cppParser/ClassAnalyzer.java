package cppParser;

import cppStructures.*;

/**
 * Analyzer for header files / classes.
 * 
 * @author Harri Pellikka 
 */
public class ClassAnalyzer extends Analyzer {

	private String[] tokens = null;
	private int i = 0;
	
	public ClassAnalyzer(SentenceAnalyzer sa) {
		super(sa);
	}

	@Override
	public boolean processSentence(String[] tokens) {
		if(ParsedObjectManager.getInstance().currentFunc != null) return false;
		
		if(ParsedObjectManager.getInstance().currentScope != null)
		{
			return processCurrentClass(tokens);
		}
		else
		{
			return processNewClass(tokens);
		}
	}

	private boolean processCurrentClass(String[] tokens)
	{
		this.tokens = tokens;
		this.i = 0;
		
		// Check the first token before starting the loop
		switch(tokens[0])
		{
		case "enum":
			// TODO handle enums
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
			// TODO handle typedefs
			Log.d("\tFound a typedef " + tokens[1] + "\n");
			return true;
		}
		
		// Check if the sentence forms a function declaration (or a function with a body)
		for(i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("("))
			{
				Log.d("\tFound a function > " + tokens[i-1]);
				
				// Check if there's a body
				if(tokens[tokens.length - 1].equals("{"))
				{
					Log.d("\t... with a body, don't handle it here.");
					return false;
				}
				
				// Find out the return type of the function
				String type = "";
				if(i > 2)
				{
					type = tokens[i-2];
				}
				else
				{
					if(tokens[i-1].contains(ParsedObjectManager.getInstance().currentScope.getName()))
					{
						type = "ctor";
						if(type.startsWith("~")) type = "dtor";
					}
				}
				String name = tokens[i-1];
				
				// Create the CppFunc object
				CppFunc cf = new CppFunc(type, name);
				
				// Search for attributes
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
							cf.parameters.add(attrib);
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
								paramType += tokens[j] + " ";
							}
						}
					}
					
					if(!paramType.equals("") && !paramName.equals(""))
					{
						CppFuncParam attrib = new CppFuncParam(paramType, paramName);
						cf.parameters.add(attrib);
					}
				}
				
				Log.d("\t\t\tParameters:");
				for(CppFuncParam cfa : cf.parameters)
				{
					Log.d("\t\t\t - " + cfa.type + " - " + cfa.name);
				}
				
				// Finally, store the CppFunc object
				cf.fileOfFunc = Extractor.currentFile;
				ParsedObjectManager.getInstance().currentScope.addFunc(cf);
				
				return true;
			}
		}
		
		if(tokens.length > 2 && tokens[tokens.length - 1].equals(";"))
		{
			Log.d("\tFound a member variable: " + tokens[tokens.length - 2] + " (type: " + tokens[tokens.length - 3] + ")");
		}
		// Log.d();
		
		return true;
	}
	
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
					
					if(!isInheriting)
					{
						Log.d("   ... called " + tokens[tokens.length - 2]);
						CppClass cc = (CppClass)ParsedObjectManager.getInstance().addClass(tokens[tokens.length - 2]);
						cc.nameOfFile = Extractor.currentFile;
						cc.braceCount = sentenceAnalyzer.braceCount;
						
						sentenceAnalyzer.setCurrentScope(cc.getName(), true);
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
}
