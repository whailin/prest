package cppParser;

import cppMetrics.LOCMetrics;
import cppParser.utils.LLOCCounter;
import java.util.ArrayList;

import cppParser.utils.Log;
import cppParser.utils.MacroExpander;
import cppParser.utils.StringTools;
import cppStructures.CppClass;
import cppStructures.CppNamespace;
import cppStructures.CppScope;

public class SentenceAnalyzer {

	
	
	public int braceCount = 0;
	
	public static boolean ignoreStd = false;
	
	//private String[] splitterChars = new String[] { " ", "(", ")", "{", "}", ";", "::", ","};
	
	private ArrayList<Analyzer> analyzers = new ArrayList<Analyzer>();
	private FunctionAnalyzer functionAnalyzer;
	private ClassAnalyzer classAnalyzer;
	private ScopeAnalyzer scopeAnalyzer;
    private LLOCCounter llocCounter=null;
    private LOCMetrics loc=null;
	
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
		assert(scopeName != null);
		
		// Search for an existing scope with the given name
		boolean found = false;
		for(CppScope cc : ParsedObjectManager.getInstance().getScopes())
		{
			if(cc.getName().equals(scopeName))
			{
				if(addToStack) ParsedObjectManager.getInstance().getCppScopeStack().push(cc);
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
				ParsedObjectManager.getInstance().getCppScopeStack().push(cc);
				// Log.d("SCOPE " + ParsedObjectManager.getInstance().getCppScopeStack().peek().getName() + " START (line: " + Extractor.lineno + ")");
			}
			else
			{
				// if(!ParsedObjectManager.getInstance().getCppScopeStack().isEmpty()) Log.d("SCOPE " + ParsedObjectManager.getInstance().currentScope.getName() + " PART (line: " + Extractor.lineno + ")");
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
		// if(ParsedObjectManager.getInstance().currentFunc != null) Log.d("Lexing end brace. " + ParsedObjectManager.getInstance().currentFunc.funcBraceCount + " | " + braceCount);
		
		// If function body is ended
		if(ParsedObjectManager.getInstance().currentFunc != null && ParsedObjectManager.getInstance().currentFunc.funcBraceCount == braceCount)
		{
			// Log.d("   FUNCTION " + ParsedObjectManager.getInstance().currentFunc.getName() + " END (line: " + Extractor.lineno + ")");
			ParsedObjectManager.getInstance().currentFunc = null;
		}
		
		// If scope body is ended
		if(!ParsedObjectManager.getInstance().getCppScopeStack().isEmpty() && ParsedObjectManager.getInstance().getCppScopeStack().peek().braceCount == braceCount)
		{
			/*
			if(ParsedObjectManager.getInstance().getCppScopeStack().peek() instanceof CppClass) Log.d("CLASS " + ParsedObjectManager.getInstance().getCppScopeStack().peek().getName() + " END (line: " + Extractor.lineno + ")");
			else if(ParsedObjectManager.getInstance().getCppScopeStack().peek() instanceof CppNamespace)Log.d("NAMESPACE " + ParsedObjectManager.getInstance().getCppScopeStack().peek().getName() + " END (line: " + Extractor.lineno + ")"); 
			else Log.d("SCOPE " + ParsedObjectManager.getInstance().getCppScopeStack().peek().getName() + " END (line: " + Extractor.lineno + ")");
			*/
			
			ParsedObjectManager.getInstance().getCppScopeStack().pop();
			if(ParsedObjectManager.getInstance().getCppScopeStack().size() > 0) ParsedObjectManager.getInstance().currentScope = ParsedObjectManager.getInstance().getCppScopeStack().peek();
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
		if(line.startsWith("#"))
		{ 
            llocCounter.addLloc();
            return;
        }
		
		// Split the line into tokens
		String[] tokens = StringTools.cleanEmptyEntries(StringTools.reconstructOperators(StringTools.split(line, null, true)));
		
		// Expand macros
		// tokens = StringTools.cleanEmptyEntries((new MacroExpander()).expand(tokens));
		
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
					if(ParsedObjectManager.getInstance().currentFunc != null)
					{
						// ParsedObjectManager.getInstance().currentFunc.addOperator("}");
					}
					lexEndBrace();
					
					// If there's only one token ("}"), don't continue to the analyzer stage
					//if(tokens.length == 1) return;
					
					continue;
				}
			}
		}
		
		llocCounter.processSentence(tokens);

		if(ParsedObjectManager.getInstance().currentFunc != null)
		{
			functionAnalyzer.processSentence(tokens);
			return;
		}
		else if(ParsedObjectManager.getInstance().currentScope != null)
		{
			
		}
		
		boolean handled = false;
		
		// Loop through analyzers
		for(Analyzer a: analyzers)
		{
			if(handled = a.processSentence(tokens)) break;
		}
		
		if(!handled)
		{
			// Log.d("Couldn't handle: " + Extractor.currentFile + ": " + Extractor.lineno);
		}
	}
/**
 * Used by LLOC counting
 * @param file 
 */
    public void fileChanged(String file, LOCMetrics loc) {
        if(llocCounter!=null){
            //Log.d("File: "+llocCounter.getFile()+" LLOC: "+llocCounter.getLloc());
            this.loc.logicalLOC=llocCounter.getLloc();
        }
        this.loc=loc;
        llocCounter = new LLOCCounter();        loc.file=file;
        llocCounter.setFile(file);
    }
    public void lastFileProcessed(){
        //Log.d("File: "+llocCounter.getFile()+" LLOC: "+llocCounter.getLloc());
        this.loc.logicalLOC=llocCounter.getLloc();
    }
}
