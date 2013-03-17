package cppParser;

import cppStructures.CppNamespace;

/**
 * Analyses CPP scope internals and delegates the processing to
 * ClassAnalyzer if needed
 * 
 * @author Harri Pellikka
 */
public class ScopeAnalyzer extends Analyzer {
	
	public ScopeAnalyzer(SentenceAnalyzer sa)
	{
		super(sa);
	}
	
	public boolean processSentence(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{

			if(tokens[i].equals("namespace"))
			{
				if(!tokens[i+1].equals("{"))
				{
					CppNamespace ns = new CppNamespace(tokens[i+1]);
					ns.braceCount = sentenceAnalyzer.braceCount;
					ns.nameOfFile = Extractor.currentFile;
					Log.d("NAMESPACE " + ns.name + " START (line: " + Extractor.lineno + ")");
					
					ParsedObjectManager.getInstance().getScopes().add(ns);
					ParsedObjectManager.getInstance().currentScope = ns;
					sentenceAnalyzer.cppScopeStack.add(ns);
					return true;
				}
			}
		}
		
		return false;
	}
}
