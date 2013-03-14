package cppParser;

import java.util.ArrayList;
import java.util.Stack;

import cppStructures.CppClass;
import cppStructures.CppNamespace;
import cppStructures.CppScope;

public class SentenceAnalyzer {

	public Stack<CppScope> cppScopeStack = new Stack<CppScope>();
	
	public int braceCount = 0;
	
	public static boolean ignoreStd = false;
	
	// List of "splitters" that are used to tokenize a single line of source code
	private String[] splitterChars = new String[] {" ", "(", ")", "{", "}", "->", ";", ",", "=", "+", "-", "*", "/", "::", ":", ".", "\""};
	//private String[] splitterChars = new String[] { " ", "(", ")", "{", "}", ";", "::", ","};
	
	private ArrayList<Analyzer> analyzers = new ArrayList<Analyzer>();
	private FunctionAnalyzer functionAnalyzer;
	private ClassAnalyzer classAnalyzer;
	private ScopeAnalyzer scopeAnalyzer;
	
	public SentenceAnalyzer()
	{
		functionAnalyzer = new FunctionAnalyzer(this);
		classAnalyzer = new ClassAnalyzer(this);
		scopeAnalyzer = new ScopeAnalyzer(this);
	
		analyzers.add(functionAnalyzer);
		analyzers.add(classAnalyzer);
		analyzers.add(scopeAnalyzer);
	}
	
	/**
	 * Sets the current scope to "scopeName". If the scope is already known,
	 * the existing scope will be used. Otherwise, a new scope is created.
	 * If 'addToStack' is 'true', the scope is also stored in a scope stack.
	 * This is useful for i.e. inner classes.
	 * 
	 * @param scopeName The name of the scope
	 * @param addToStack If 'true', store the scope in a scope stack
	 */
	public void setCurrentScope(String scopeName, boolean addToStack)
	{
		// Search for an existing scope with the given name
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
			ParsedObjectManager.getInstance().getScopes().add(cc);
			ParsedObjectManager.getInstance().currentScope = cc;
			if(addToStack)
			{
				cppScopeStack.push(cc);
				Log.d("SCOPE " + cppScopeStack.peek().getName() + " START (line: " + Extractor.lineno + ")");
			}
			else
			{
				if(!cppScopeStack.isEmpty()) Log.d("SCOPE " + ParsedObjectManager.getInstance().currentScope.getName() + " PART (line: " + Extractor.lineno + ")");
			}
			
		}
	}
	
	/**
	 * Handles an ending brace.
	 * In the case of a function body, the current function handling is ended.
	 * In the case of a scope, the current scope handling is ended, and if
	 * the scope is in the scope stack, it is removed from there.
	 */
	private void lexEndBrace()
	{
		
		// If function body is ended
		if(ParsedObjectManager.getInstance().currentFunc != null && ParsedObjectManager.getInstance().currentFunc.funcBraceCount == braceCount)
		{
			Log.d("   FUNCTION " + ParsedObjectManager.getInstance().currentFunc.getName() + " END (line: " + Extractor.lineno + ")");
			ParsedObjectManager.getInstance().currentFunc = null;
		}
		
		// If scope body is ended
		if(!cppScopeStack.isEmpty() && cppScopeStack.peek().braceCount == braceCount)
		{
			if(cppScopeStack.peek() instanceof CppClass) Log.d("CLASS " + cppScopeStack.peek().getName() + " END (line: " + Extractor.lineno + ")");
			else if(cppScopeStack.peek() instanceof CppNamespace)Log.d("NAMESPACE " + cppScopeStack.peek().getName() + " END (line: " + Extractor.lineno + ")"); 
			else Log.d("SCOPE " + cppScopeStack.peek().getName() + " END (line: " + Extractor.lineno + ")");
			
			cppScopeStack.pop();
			if(cppScopeStack.size() > 0) ParsedObjectManager.getInstance().currentScope = cppScopeStack.peek();
		}
		
		/*
		if(ParsedObjectManager.getInstance().currentScope != null)
		{
			if(ParsedObjectManager.getInstance().currentScope.braceCount == braceCount)
			{
				Log.d("SCOPE " + ParsedObjectManager.getInstance().currentScope.getName() + " END (line: " + Extractor.lineno + ")");
				ParsedObjectManager.getInstance().currentScope = null;
			}
		}
		*/
		
		braceCount--;
	}
	
	/**
	 * Does lexical analysis (tokenizing) of a given line of code
	 */
	public void lexLine(String line)
	{
		// TODO Create a preprocessor analyzer and remove this
		if(line.startsWith("#")) return;
		
		if(Extractor.lineno == 365)
		{
			Log.d("dbg start");
		}
		
		// Split the line into tokens
		String[] tokens = StringTools.split(line, splitterChars, true);

		boolean stringOpen = false;
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("\"")) stringOpen = !stringOpen;
			
			if(!stringOpen && tokens[i].length() == 1)
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
		}
		
		if(ParsedObjectManager.getInstance().currentFunc != null)
		{
			functionAnalyzer.processSentence(tokens);
			return;
		}
		else if(ParsedObjectManager.getInstance().currentScope != null)
		{
			
		}
		
		// Loop through analyzers
		for(Analyzer a: analyzers)
		{
			if(a.processSentence(tokens)) break;
		}
	}
}
