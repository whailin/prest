

package cppParser.utils;

import cppParser.Extractor;
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
    /** Different modes
     * RESET When currently analyzed part of sentence is definetly not a function 
     * call this mode is set. Mode is changed back to BEGIN once right kind of token 
     * has been processed
     * BEGIN This is the default mode which expects the tokens to contain a function 
     * name or owner of a function eg Owner::func(); It switches to RESET or PARAMETERS 
     * depending on the tokens
     * PARAMETERS This mode expects the tokens to contain parameters that are given 
     * to the function call.
     * ANOTHER This mode comes after closing parenthesis of a function call. It waits 
     * for another function call eg getMyObj()->getName(); The new function call will 
     * get the previous function call to be it's owner.
     */
    public static final int RESET=0, BEGIN=1, PARAMETERS=2, ANOTHER=3;
    private int mode=BEGIN;
    private String token, next;
    private boolean foundPtr=false, 
            parameter=false; //Are the tokens given to the FunctionFinder parameters for function call?
    //When function calls are parsed recursively(function call as parameter) 
    //all tokens must be also collected in this list
    private List<ParameterToken> currentParameter=new ArrayList<>(); 
    
    private int parenthesisDepth=0; // used for finding parameters
    
    private int index, skip=0;
    private List<ParameterToken> owners=new ArrayList<>();
    private List<String> parameterTokens=new ArrayList<>();
    
    private String currentOwner="";
    private FunctionCall currentFc;
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
        parameter=true;
        
    }
    /**
     * Method converts the string to StringToken and puts it to currentParameter List if parameter==true
     * @param token 
     */
    private void addToken(String token){
        if(parameter)
            currentParameter.add(new StringToken(token));
    }
    
    private void addToken(ParameterToken token){
        if(parameter)
            currentParameter.add(token);
    }
    private List<ParameterToken> getCurrentParameter(){return currentParameter;}
    public void findFunctions(String[] tokens){
        //this.tokens=tokenizeLiterals(tokens);
        this.tokens=tokens;
        for(index=0;tokens.length>index;index++){
            token=tokens[index];
            if(tokens.length>(index+1))
                next=tokens[index+1];
            else 
                next=null;
            pushTokens(token, next);
        }

        
    }
    
    private void pushTokens(String token, String next) {
        //Log.d("t: "+token+" n: "+next+" "+mode);
        if(skip>0){
            skip--;
            return;
        }
        //if(parameter) Log.d("PT: "+token+" "+next+" "+ mode);
        switch(mode){
            case BEGIN:
                FunctionCall fc=lookForFirstPart(token, next);
                if(fc!=null)
                    currentFc=fc;
                break;
            case PARAMETERS:
                parseParameters(token, next);
                break;
            case RESET:
                checkForReset(token, next);
            case ANOTHER:
                waitForAnotherFunctionCall(token, next);
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
        //List<String> owners = getOwners(index);
        List<List<ParameterToken>> params = new ArrayList<>();
        List<ParameterToken> currentParam = new ArrayList<>();
                // Check if the function call is parameterless
        if(index < tokens.length - 1 && tokens[index+1].equals(")"))
        {
            if(varFinder.isDefined(funcName)){
                Log.d(funcName+" is known variable, not function call...");
            }else{
                Log.d("      (line: " + Extractor.lineno + ") Function call np > " + funcName);
                // func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
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
                                
                if(varFinder.isDefined(funcName))
                {
                    Log.d(funcName+" is known variable, not function call...");
                    fc = null;
                }
                else
                {
                    fc=new FunctionCall(owners,funcName);
                    Log.d("      (line: " + Extractor.lineno + ") Function call > " + fc.toString());
                    // func.recognizedLines.add("      (line: " + Extractor.lineno + ") Function call > " + funcName);
                }
                //skip(skip);
                return fc;
            case "(":
                // Recurse through inner function calls
                // j = handleFunctionCall(j);
                if(isFuncCall(j)){
                    fc = handleFunctionCall(true); 
                    if(fc != null)
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
   
     
    /*private void skip(){
            if(parent==null)
                index++;
            else
                parent.skip();
    }*/
    private void skip(){
        skip++;
    }

    private boolean isFuncCall(int ind) {
        if((ind-1)<0) return false;
        if(Constants.isKeyword(tokens[ind-1]))return false;
        //if(isKnownType()) return false; //TBD
        return true;
    }
    
    
    private FunctionCall lookForFirstPart(String token, String next)
    {
        
        if(next == null){
            addToken(token);
            reset();
            return null;
        }
        if(token.contentEquals(";")){
            addToken(token);
            reset();
            return null;
        }
        else
        {
            if(Constants.isWordToken(token))
            {
                if(Constants.isKeyword(token)){
                    mode=RESET;
                    addToken(token);
                    return null;
                }
                String temp=null;
                switch(next){
                    case "::":
                    case ".":
                    case "->":
                        temp=next;
                        foundPtr=false;
                        skip();
                        break;
                    case "(":
                        mode=PARAMETERS;
                        foundPtr=false;
                        skip();
                        break;
                    case "*":
                        foundPtr=true;
                        break;
                    default:
                        mode=RESET;
                        return null;
                }
                if(mode==PARAMETERS){
                    if(temp!=null)
                        owners.add(new StringToken(temp));
                    return new FunctionCall(owners, token);
                }else{
                    currentOwner+=token;
                    if(temp!=null){
                        owners.add(new StringToken(currentOwner));
                        owners.add(new StringToken(temp));
                        currentOwner="";
                        
                    }
                }
                

            }else if(token.contentEquals("*")){
                currentOwner+=token;
                foundPtr=true;
                
            }
            return null; 
        }
    }
/**
 * This method looks at given tokens and combines string literals to one token
 * @param tokens
 * @return returns same array that was given as parameter if no literals were found else
 * it returns new array where Strings are combined
 */
    private String[] tokenizeLiterals(String[] tokens) {
        //Log.d("Tokenizing");
        List<String> list=new ArrayList<>(tokens.length);
        boolean containsLiterals=false;
        boolean found=false;
        String prev, current=null, combined="";
        for(int x=0;x<tokens.length;x++){
            prev=current;
            current=tokens[x];
            if(found){
                if(current.contentEquals("\"")){
                    if(prev!=null){
                        if((prev.charAt(prev.length()-1)=='\\'))
                            ;//Log.d("Found \\");//found=false; //end of string literal
                        else
                            found=false;
                    }else found=false;//end of string literal
                }
                if(found){
                    combined+=" "+current;
                    //Log.d("comb:"+combined);
                }
                else {
                    combined+=current;
                    //Log.d("C:"+combined);
                }
            }else{
                if(current.charAt(0)=='\''){//handle char literals
                    //Log.d("p: "+prev+" c: "+current);
                    switch(current.length()){
                        case 1:
                            
                            //If the char between '' is splitter token then there's two separate tokens to combine with first ' token
                            combined=current+tokens[x+1]+tokens[x+2];
                            x+=2;
                            containsLiterals=true;
                            list.add(combined);
                            continue;
                        case 2:
                            if(current.charAt(1) =='\\'){
                                combined=current+tokens[x+1]+tokens[x+2];
                                x+=2;
                                containsLiterals=true;
                                list.add(combined);
                                continue;
                            }else Log.d("Unexpected token:"+current+" Expected char literal"); //This should not happen
                                    
                        case 3:
                        case 4:
                            list.add(current);
                            continue;
                        default: 
                            Log.d("Unexpected token:"+current+" Expected char literal"); //This should not happen
                    }
                }
                if(current.contentEquals("\"")){
                    //Log.d("Found \"");
                    combined="\"";
                    found=true;
                }
            }
            
            
        }
        if(!containsLiterals)return tokens;
        return null;
        
    }

    
    
    private void reset(){
        mode=BEGIN;
        owners=new ArrayList<>();
        parameterTokens=new ArrayList<>();
        currentOwner="";
        foundPtr=false;
        parenthesisDepth=0;
        skip=0;
    }

    private void parseParameters(String token, String next) {
        switch (token) {
            case ")":
                if(parenthesisDepth==0){
                    currentFc.parameters.add(parseParameter(parameterTokens));
                    // Log.d("Found FC: "+ currentFc.toString());
                    if(next==null)
                        addToken(new FunctionCallToken(currentFc));
                    mode=ANOTHER;
                }else{ 
                    parenthesisDepth--;
                    parameterTokens.add(token);
                }
                break;
            case "(":
                parenthesisDepth++;
                parameterTokens.add(token);
                break;
            case ",":
                if(parenthesisDepth==0)
                    currentFc.parameters.add(parseParameter(parameterTokens));
                break;
            default:
                parameterTokens.add(token);
        }
    }
    
    private List<ParameterToken> parseParameter(List<String> tokens){
        FunctionFinder ff=new FunctionFinder(this, varFinder, func);
        
        
        List<ParameterToken> parameter=new ArrayList<>();
        
        for(int i=0;i<tokens.size();i++){
            String currentToken=tokens.get(i);
            String nextToken;
            if(i==(tokens.size()-1))
                nextToken=null;
            else nextToken=tokens.get(i+1);
            ff.pushTokens(currentToken, nextToken);
        }
        return ff.getCurrentParameter();
        
    }

    private void checkForReset(String token, String next) {
        addToken(token);
        if(StringTools.isOperator(token))
            reset();
    }

    private void waitForAnotherFunctionCall(String token, String next) {
        switch(token){
            case "->":
            case ".":
            case "::":
                FunctionCall temp=currentFc;
                reset();
                owners.add(new FunctionCallToken(temp));
                owners.add(new StringToken(token));
                break;
            default:
                addToken(token);
                reset();
       }
    }
    

    
}
