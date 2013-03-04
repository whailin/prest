package cppParser;

import cppStructures.CppClass;

/**
 * Analyzer for header files / classes.
 * 
 * @author Harri Pellikka 
 */
public class ClassAnalyzer extends Analyzer {

	private CppClass currentClass = null;
	
	public ClassAnalyzer(SentenceAnalyzer sa) {
		super(sa);
	}

	@Override
	public boolean processSentence(String[] tokens) {
		if(currentClass != null)
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
		return false;
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
					
					// TODO Create the class but don't set it as current class
					CppClass cc = (CppClass)ParsedObjectManager.getInstance().addClass(tokens[tokens.length - 2]);
					cc.nameOfFile = Extractor.currentFile;
					
					return true;
				}
				else
				{
					// Found a class with definition
					
					
					
					
					
					boolean isInheriting = false;
					
					for(int j = i + 1; j < tokens.length; ++j)
					{
						if(tokens[j].equals(":"))
						{
							isInheriting = true;
							Log.d("   ... called " + tokens[j-1]);
							CppClass cc = (CppClass)ParsedObjectManager.getInstance().addClass(tokens[j-1]);
							cc.nameOfFile = Extractor.currentFile;
							
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
								
								/*
								Log.d("    ... inherited from " + tokens[tokens.length - 2]);
								CppClass pcc = (CppClass)ParsedObjectManager.getInstance().addClass(tokens[tokens.length - 2]);
								
								pcc.addChild(cc);
								pcc.nameOfFile = Extractor.currentFile;
								*/
							}
							break;
						}
					}
					
					if(!isInheriting)
					{
						Log.d("   ... called " + tokens[tokens.length - 2]);
						CppClass cc = (CppClass)ParsedObjectManager.getInstance().addClass(tokens[tokens.length - 2]);
						cc.nameOfFile = Extractor.currentFile;
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
}
