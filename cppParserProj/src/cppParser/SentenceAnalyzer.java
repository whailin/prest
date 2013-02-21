package cppParser;

import java.util.Stack;

import cppStructures.CppClass;
import cppStructures.CppFunc;
import cppStructures.CppNamespace;
import cppStructures.CppScope;

public class SentenceAnalyzer {

	private Stack<CppScope> cppScopeStack = new Stack<CppScope>();
	
	public static int braceCount = 0;
	
	public static boolean ignoreStd = false;
	
	// List of "splitters" that are used to tokenize a single line of source code
	private String[] splitterChars = new String[] {" ", "(", ")", "{", "}", "->", ";", ",", "=", "+", "-", "*", "/", "::"};
	
	private FunctionAnalyzer functionAnalyzer;
	
	public SentenceAnalyzer()
	{
		functionAnalyzer = new FunctionAnalyzer(this);
	}
	
	public void setCurrentScope(String scopeName, boolean addToStack)
	{
		boolean found = false;
		for(CppScope cc : ParsedObjectManager.getInstance().getScopes())
		{
			if(cc.getName().equals(scopeName))
			{
				if(addToStack) cppScopeStack.push(cc);
				ParsedObjectManager.getInstance().currentScope = cc;
				found = true;
				break;
			}
		}
		
		if(!found)
		{
			
			CppScope cc = new CppScope(scopeName);
			cc.nameOfFile = Extractor.currentFile;
			// cppScopes.add(cc);
			ParsedObjectManager.getInstance().getScopes().add(cc);
			// currentScope = cc;
			ParsedObjectManager.getInstance().currentScope = cc;
			if(addToStack)
			{
				cppScopeStack.push(cc);
				Log.d("SCOPE " + cppScopeStack.peek().getName() + " START (line: " + Extractor.lineno + ")");
			}else
			{
				if(!cppScopeStack.isEmpty()) Log.d("SCOPE " + cppScopeStack.peek().getName() + " PART (line: " + Extractor.lineno + ")");
			}
			
		}
	}
	
	public boolean lineDone = false;
	
	private void lexDefine(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("#define"))
			{
				ParsedObjectManager.getInstance().defines.add(tokens[i+1]);
				i++;
			}
		}
	}
	
	private void lexInclude(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("#include"))
			{
				ParsedObjectManager.getInstance().includes.add(tokens[i+1]);
				i++;
			}
		}
	}
	
	private void currentScopeToClass()
	{
		CppScope cs = cppScopeStack.pop();
		CppClass cc = new CppClass(cs);
		Log.d("Found a scope that is a class > " + cc.name);
		cppScopeStack.push(cc);
	}
	
	private void lexClass(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("class"))
			{
				
				for(int j = i + 1; j < tokens.length; ++j)
				{
					if(tokens[j].equals(":") || tokens[j].equals("{"))
					{
						setCurrentScope(tokens[j-1], true);
						if(!(cppScopeStack.peek() instanceof CppClass)) currentScopeToClass();
						cppScopeStack.peek().braceCount = braceCount;
						Log.d("CLASS " + cppScopeStack.peek().getName() + " START (line: " + Extractor.lineno + ")");
						break;
					}
				}
			}
		}
	}
	
	private void lexEndBrace()
	{
		braceCount--;
		
		if(ParsedObjectManager.getInstance().currentFunc != null && ParsedObjectManager.getInstance().currentFunc.funcBraceCount == braceCount)
		{
			Log.d("   FUNCTION " + ParsedObjectManager.getInstance().currentFunc.getName() + " END (line: " + Extractor.lineno + ")");
			ParsedObjectManager.getInstance().currentFunc = null;
		}
		
		
		
		// if(!cppClassStack.isEmpty() && classBraceCount == braceCount)
		if(!cppScopeStack.isEmpty() && cppScopeStack.peek().braceCount == braceCount)
		{
			if(cppScopeStack.peek() instanceof CppClass) Log.d("CLASS " + cppScopeStack.peek().getName() + " END (line: " + Extractor.lineno + ")");
			else if(cppScopeStack.peek() instanceof CppNamespace)Log.d("NAMESPACE " + cppScopeStack.peek().getName() + " END (line: " + Extractor.lineno + ")"); 
			else Log.d("SCOPE " + cppScopeStack.peek().getName() + " END (line: " + Extractor.lineno + ")");
			cppScopeStack.pop();
		}
	}
	
	/**
	 * Does lexical analysis (tokenizing) of a given line of code
	 */
	public void lexLine(String line)
	{
		lineDone = false;
		
		// Split the line into tokens
		String[] tokens = StringTools.split(line, splitterChars, true);
		
		lexDefine(tokens);
		lexInclude(tokens);
		lexClass(tokens);
		
		functionAnalyzer.processSentence(tokens);
		
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("{"))
			{
				braceCount++;
				continue;
			}
			else if(tokens[i].equals("}"))
			{
				lexEndBrace();
				continue;
			}
			else if(tokens[i].equals("namespace"))
			{
				if(!tokens[i+1].equals("{"))
				{
					CppNamespace ns = new CppNamespace(tokens[i+1]);
					ns.braceCount = braceCount;
					ns.nameOfFile = Extractor.currentFile;
					Log.d("NAMESPACE " + ns.name + " START (line: " + Extractor.lineno + ")");
					// cppScopes.add(ns);
					ParsedObjectManager.getInstance().getScopes().add(ns);
					cppScopeStack.add(ns);
				}
			}
		}
		
		if(lineDone)
		{
			return;
		}
		
		if(!cppScopeStack.isEmpty())
		{
			if(ParsedObjectManager.getInstance().currentFunc == null)
			{
				if(!line.trim().equals("}"))
				{
					// Log.d(" - Mem.Line: " + line);
				}
			}
			else
			{
				// Log.d(" - Func.Line: " + line.trim());
				if(tokens[tokens.length - 1].equals(";"))
				{
					boolean isReturn = false;
					
					if(tokens[0].equals("return"))
					{
						// Log.d("        - Return statement");
					}
				}
			}
		}
		
		if(ParsedObjectManager.getInstance().currentFunc != null)
		{
			// if(!line.contains(currentFunc))// Log.d(" - " + line);
			/*
			// System.out.print("   ");
			for(int i = 0; i < tokens.length; ++i)
			{
				// System.out.print(tokens[i]);
				if(i < tokens.length - 1) // System.out.print(" | ");
				else // Log.d();
			}
			*/
		}
	}
	
	
	
}
