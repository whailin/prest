

package cppParser.utils;

import cppParser.Extractor;
import cppParser.Log;
import cppStructures.CppFunc;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tomi
 */
public class FunctionFinder {
    private int index;
    private String token, next;
    private VarFinder varFinder;
    private FunctionFinder parent=null;
    private String[] tokens;
    private CppFunc func;
    private FunctionCall currentFc;
    public FunctionFinder(VarFinder varFinder, CppFunc currentFunc){
        this.varFinder=varFinder;
        this.func=currentFunc;
    }
    /**
     * This constructor should be used only by recursive function call search
     * @param f
     * @param varFinder
     * @param currentFunc 
     */
    private FunctionFinder(FunctionFinder parent, VarFinder varFinder, CppFunc currentFunc){
        this.parent=parent;
        this.varFinder=varFinder;
        this.func=currentFunc;
    }
    public void findFunctions(String[] tokens){
        for(index=0;tokens.length<index;index++){
            if(tokens[index].contentEquals("("))
                handleFunctionCall(index);
        }
        
    }
    
    /**
	 * Analyzes a function call.
	 * This method is recursive, meaning that if it finds another function call
	 * in the parameters, it will call itself to parse the inner function call.
	 * @param index The index in tokens if the opening parenthesis
	 * @return The index of the closing parenthesis
	 */
	private int handleFunctionCall(int ind)
	{
		// Store the function name
		String funcName = tokens[ind-1]; //Array out of bounds if first token is "(" ?
		
		//ParsedObjectManager.getInstance().currentFunc.addOperand(funcName);
		//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[index]);
		
		// Check if the function call is parameterless
		if(tokens[ind+1].equals(")"))
		{
                    if(varFinder.isDefined(funcName)){
                        Log.d(funcName+" is known variable, not function call...");
                    }else{
			Log.d("      (line: " + Extractor.lineno + ") Function call np > " + funcName);
			func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
                    }
			return ind + 1;
		}
                // Owners List should contain the owners of the function call eg myObj in "myObj->hello();"
		List<String> owners=new ArrayList<>(); 
		List<List<String>> params = new ArrayList<>();
		List<String> currentParam = new ArrayList<>();
		boolean even;
                int skipped=0;
                for(int j= 2; ind-j >= 0;j++){
                    if(tokens[ind-j].contentEquals("*"))
                        skipped++;
                    else{
                        if((j+skipped)%2==0)
                            even=true;
                        else even=false;
                        if(even){
                            switch(tokens[ind-j]){
                                case "->":
                                case ".":
                                    owners.add(0, tokens[ind-j]);
                                    break;
                                case "::":
                                    Log.d("Line:"+Extractor.lineno+ " contains :: when . or -> was expected");
                                    break;
                                default:
                                    break;
                            }
                        }else{
                            owners.add(0, tokens[ind-j]);
                        }
                    }
                }
                if(!owners.isEmpty()){
                    String str="";
                    for(String s:owners)
                        str+=s;
                    Log.d("Owner"+str);
                }
                
                // Loop through the parameters
		for(int j = ind + 1; j < tokens.length; ++j)
		{
			switch(tokens[j])
			{
			case ")":
				// Close the function call
				if(!currentParam.isEmpty())
				{
					params.add(currentParam);
					//handleParameter(currentParam);
				}
                                if(varFinder.isDefined(funcName)){
                                    Log.d(funcName+" is known variable, not function call...");
                                }else{
                                    Log.d("      (line: " + Extractor.lineno + ") Function call > " + funcName);
                                    func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
                                }
				return j;
			case "(":
				// Recurse through inner function calls
				// j = handleFunctionCall(j);
				//j = handleOpeningParenthesis(j);
				break;
			case ",":
				params.add(currentParam);
				//handleParameter(currentParam);
				currentParam = new ArrayList<>();
				break;
			default:
				currentParam.add(tokens[j]);
				break;
			}
			
		}
		
		
		// This should never happen, but if it does, this is the last one that was checked
		return tokens.length - 1;
	}
}
