

package cppParser.utils;

import cppParser.Extractor;
import cppParser.Log;
import cppParser.utils.parameter.FunctionCallToken;
import cppParser.utils.parameter.ParameterToken;
import cppParser.utils.parameter.StringToken;
import cppStructures.CppFunc;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tomi
 */
public class FunctionFinder {
    private int index;
    private VarFinder varFinder;
    private FunctionFinder parent=null;
    private String[] tokens;
    private CppFunc func;
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
        this.tokens=tokens;
        for(index=0;tokens.length>index;index++){
            if(tokens[index].contentEquals("(")){
                if(isFuncCall(index))
                    handleFunctionCall(false);
            }
        }
        
    }

    /**
	 * Analyzes a function call.
	 * This method is recursive, meaning that if it finds another function call
	 * in the parameters, it will call itself to parse the inner function call.
	 * @param index The index in tokens if the opening parenthesis
	 * @return The index of the closing parenthesis
	 */
	private FunctionCall handleFunctionCall(boolean recursive)
	{
		// Store the function name
            //Log.d("hfc");
		String funcName = tokens[index-1]; //Array out of bounds if first token is "(" ?
		
		//ParsedObjectManager.getInstance().currentFunc.addOperand(funcName);
		//ParsedObjectManager.getInstance().currentFunc.addOperator(tokens[index]);
		
		
                // Owners List should contain the owners of the function call eg myObj in "myObj->hello();"
		List<String> owners=getOwners(index);
		List<List<ParameterToken>> params = new ArrayList<>();
		List<ParameterToken> currentParam = new ArrayList<>();
                // Check if the function call is parameterless
		if(tokens[index+1].equals(")"))
		{
                    if(varFinder.isDefined(funcName)){
                        Log.d(funcName+" is known variable, not function call...");
                    }else{
			Log.d("      (line: " + Extractor.lineno + ") Function call np > " + funcName);
//			func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
                    }
                    skip();
                    return new FunctionCall(funcName);
		}
                // Loop through the parameters
                int skip=0;
                FunctionCall fc;
		for(int j = index + 1; j < tokens.length; ++j)
		{
                    skip();
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
                                    fc= null;
                                }else{
                                    fc=new FunctionCall(owners,funcName);
                                    Log.d("      (line: " + Extractor.lineno + ") Function call > " + fc.toString());
//                                    func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
                                }
                                //skip(skip);
				return fc;
			case "(":
				// Recurse through inner function calls
				// j = handleFunctionCall(j);
                                if(isFuncCall(j)){
                                    
                                    fc=handleFunctionCall(true); 
                                    if(fc!=null)
                                        currentParam.add(new FunctionCallToken(fc));
                                }
				break;
			case ",":
				params.add(currentParam);
				//handleParameter(currentParam);
				currentParam = new ArrayList<>();
				break;
			default:
				currentParam.add(new StringToken(tokens[j]));
				break;
			}
			
		}

		return null;
	}
        
    private List<String> getOwners(int ind) {
        List<String> owners=new ArrayList<>();
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
                                case "::":
                                    owners.add(0, tokens[ind-j]);
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
        }
        return owners;
    }
    private void skip(int skips){
            for(int i=0;i<skips;i++)
                skip();
    }  
    private void skip(){
            if(parent==null)
                index++;
            else
                parent.skip();
    }

    private boolean isFuncCall(int ind) {
        if((ind-1)<0) return false;
        if(Constants.isKeyword(tokens[ind-1]))return false;
        //if(isKnownType()) return false; //TBD
        return true;
    }

    
}
