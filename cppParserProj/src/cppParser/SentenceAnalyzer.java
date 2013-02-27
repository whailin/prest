package cppParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

import cppStructures.CppClass;
import cppStructures.CppFunc;
import cppStructures.CppNamespace;
import cppStructures.CppScope;

public class SentenceAnalyzer {

	public Stack<CppScope> cppScopeStack = new Stack<CppScope>();
	
	public int braceCount = 0;
	
	public static boolean ignoreStd = false;
	
	// List of "splitters" that are used to tokenize a single line of source code
	// private String[] splitterChars = new String[] {" ", "(", ")", "{", "}", "->", ";", ",", "=", "+", "-", "*", "/", "::", ":", "."};
	private String[] splitterChars = new String[] { " ", "(", ")", "{", "}", ";", "::" };
	
	private ArrayList<Analyzer> analyzers = new ArrayList<Analyzer>();
	private FunctionAnalyzer functionAnalyzer;
	
	public SentenceAnalyzer()
	{
		functionAnalyzer = new FunctionAnalyzer(this);
		analyzers.add(functionAnalyzer);
		analyzers.add(new ScopeAnalyzer(this));
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
				if(!cppScopeStack.isEmpty()) Log.d("SCOPE " + ParsedObjectManager.getInstance().currentScope.getName() + " PART (line: " + Extractor.lineno + ")");
			}
			
		}
	}
	
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
		
		braceCount--;
	}
	
	/**
	 * Does lexical analysis (tokenizing) of a given line of code
	 */
	public void lexLine(String line)
	{
		// Split the line into tokens
		String[] tokens = StringTools.split(line, splitterChars, true);

		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("{"))
			{
				braceCount++;
				continue;
			}
			if(tokens[i].equals("}"))
			{
				lexEndBrace();
				continue;
			}
		}
		
		if(ParsedObjectManager.getInstance().currentFunc != null)
		{
			functionAnalyzer.processSentence(tokens);
			return;
		}
		
		// Loop through analyzers
		for(Analyzer a: analyzers)
		{
			if(a.processSentence(tokens)) break;
		}
		
		lexDefine(tokens);
		lexInclude(tokens);
		lexClass(tokens);
	}
}
