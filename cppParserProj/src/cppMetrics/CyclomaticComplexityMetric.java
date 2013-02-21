package cppMetrics;

import cppParser.ParsedObjectManager;
import cppStructures.CppFunc;
import cppStructures.CppScope;

public class CyclomaticComplexityMetric implements Metrics {

	@Override
	public void calculateMetrics() {
		for(CppScope cs : ParsedObjectManager.getInstance().getScopes())
		{
			for(CppFunc cf : cs.getFunctions())
			{
				// Initial cc is 1
				int cc = 1;
				
				// Analyze the statements in the function and increase the score if needed
				// TODO Update if and when the CppFunc structure changes
				for(String s : cf.getStatements())
				{
					switch(s)
					{
					case "if":
					case "while":
					case "case":
					case "catch":
						cc++;
						break;
					}
					
					// TODO Increment when boolean operators (&&, ||) are found
					
					// TODO Increment when PP directives are found
					
				}
				
				// Finally, store the cc
				cf.setCyclomaticComplexity(cc);
			}
		}
		
	}

	@Override
	public Result getResults() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int calculateVGComplexity(int edges, int nodes, int exitNodes){
		return (edges - nodes + exitNodes);
	}
	
	public int calculateVCComplexity(int edges, int nodes, int exitNodes){
		return (edges - nodes + 2*exitNodes);
	}

}
