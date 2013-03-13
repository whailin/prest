package cppParser.utils;

import cppParser.Log;
import java.util.ArrayList;
import java.util.List;
import cppParser.utils.Constants;
import treeparser.exception.ParseException;
import treeparser.treeobject.Variable;


/**
 *
 * @author Tomi
 */
public class VarFinder{
    private static final boolean silenced=false;
    public VarFinder(){
        variables=new ArrayList<>();
    }
    private VarFinder(List<Variable> variables){
        this.variables=variables;
        isRecursive=true;
    }
    private List<Variable> variables;
    private VarFinder recursive=null;
    private boolean isRecursive=false;
            
    private static final int TYPE=0,NAME=1,ARRAY=2,EQUALS=3, RESET=4, TEMPLATE=5;
    private boolean foundStringLiteral=false;
    private int mode=TYPE;
    /*
     * Mode determines how tokens are handled.
     * 
     * TYPE(default mode) is used for taking token
     * that define variable type eg unsigned int or std::string
     * NAME is used to take tokens that tell name of the variable or variables
     * ARRAY is used to check if the variable is array or not.
     * RESET is used when it's known that current statement/sentence will not contain variable
     * declarations. It should switch back to TYPE when valid declarations can be received 
     * eg after ";" token.
     * TEMPLATE checks if the variable contains template eg 
     * 
    */
    private boolean primitive=false;
    
    private String currentType="", currentName="", currentArray="";
    
    
    private int i=0; //Current index in the token array
    
    private int arrays=0; // This is for checking arrays inside arrays
    private String token,next;
    
    public void findVariables(String[] tokens){
        
        for(i=0;i<tokens.length;i++){
                
             token=tokens[i];
                
             if(i+1<tokens.length)
                    next=tokens[i+1];
             if(foundStringLiteral){
                 if(token.contentEquals("\\")) i++;
                 else if(token.contentEquals("\"")) foundStringLiteral=false;
                    
             }else{
                 if(token.contains("\""))
                        foundStringLiteral=true;
             }
             pushTokens(token, next);
             
        }
    }
    
    
    public boolean pushTokens(String token, String nextToken){
        this.token=token;
        this.next=nextToken;
        if(recursive!=null){
            if(recursive.pushTokens(token, nextToken)){
                recursive=null;
                if(mode==RESET)
                    reset();
            }
        }
        else if(token.contentEquals("(")||token.contentEquals("{")){
            //Log.d("Rec");
             recursive=new VarFinder(variables);
        }
        else if(token.contentEquals(")")||token.contentEquals("}")){
             return true;
        }else if(!foundStringLiteral){
                    decideAction();
                
        }
        return false;
    }
    /**
     * This method decides what to do with the next tokens.
     */
    private void decideAction(){
        //Log.d("token:"+token+" "+mode);
        switch(mode){
                    case TYPE:
                        try{
                            lookForType();
                        }catch(Exception e){}
                        break;
                    case NAME:
                        lookForNames();
                        break;
                    case RESET:
                        checkForReset();
                        break;
                    case ARRAY:
                        lookForArrays();
                        break;
                    case EQUALS:
                        waitForEndOfAssign();
                        break;
                }
    }
    
     /**
     * Method finds next token that contains ",". It counts tokens to next "," after index
     * and returns it. If statement ends( ";", bracket is found, or there are no more tokens) it returns -1;
     * @param index
     * @param obj
     * @return 
     */
    /*private int findNextComma(int index, ) {
        
        for(x=0;(index+x)<size;x++){
            obj1=obj.getChildren().get(index+x);
            if(obj1.getContent().contentEquals(","))
                return x;
            else if(obj1.getContent().contentEquals(";") ||
                    obj1.getContent().contentEquals(")") ||
                    obj1.getContent().contentEquals("]") ||
                    obj1.getContent().contentEquals("}"))
                return -1;
        }
        return -1;
    }*/

    private void checkForReset(){
        if(token.contentEquals(";"));
        else if(token.contentEquals("{"));
        else return;
        reset();
    }
    private void reset(){
        
        currentType="";
        currentName="";
        currentArray="";
        primitive=false;
        mode=TYPE;
        arrays=0;
    }

    private void lookForType() throws ParseException{
        
        if(next==null)
            return;
        if(token.contentEquals(";"))
            reset();
        else{
            //Log.d("lft:"+token);
            if(isWordToken(token)){
                //Log.d("lft:"+token);
                if(currentType.isEmpty())
                    currentType+=token;
                else
                    currentType+=" "+token;
                if(Constants.isPrimitiveType(token)){
                    primitive=true;
                }
                if(primitive){
                        //Log.d("found primitive type");
                        if(!Constants.isPrimitiveType(next)){
                            mode=NAME;
                        }else return;

                }
                if(next.contentEquals("::")){
                    this.i++;
                    currentType+=next;
                }else if(isWordToken(next)){
                    if(!primitive)
                        if(Constants.isKeyword(currentType)){
                            mode=RESET;
                            return;
                        }
                    mode=NAME;
                }else if(next.contentEquals("*") || next.contentEquals("&")){
                    if(!primitive)
                        if(Constants.isKeyword(currentType)){
                            mode=RESET;
                            return;
                        }
                    mode=NAME;
                }else
                    mode=RESET;
            }else{
                mode=RESET;
            }
        }
        
        
            
            
        
    }
    
/**
 * This method checks if the given token is a word that can be a name(variable, class...)
 */
    private boolean isWordToken(String token) {
        char c=token.charAt(0);
        if(!Constants.isValidNameChar(c))
            return false;
        else{
            if((c>='0')&&c<='9')
                return false;
        }
        return true;
    }

    private void lookForNames() {
        //Log.d("lfn:"+token+" "+next);
        if(token.contentEquals("*") ||token.contentEquals("&")){
            currentName+=token;
        }
        else if(isWordToken(token)){
            currentName+=token;
            if(next!=null){
                switch(next){
                    case "=":
                        mode=EQUALS;
                        i++;
                        break;
                    case "(":
                        reset();
                        //i++;
                        break;
                    case ")":
                        endOfDeclaration();;
                        break;
                    case ";":
                        endOfDeclaration();
                        break;
                    case ",":
                        createVariable();
                        i++;
                        break;
                    case "[":
                        mode=ARRAY;
                        arrays++;
                        currentArray+="[";
                        i++;
                        break;   
                    default:
                        Log.d("Found token "+next);
                }
            }else throw new Error("Unexpected nullpointer");
        }
    }
    
    private void lookForArrays(){
        currentArray+=token;
        if(token.contentEquals("]")){
            
            arrays--;
        }
        if(next!=null){
            if(next.contentEquals("[")){
                currentArray+="[";
                arrays++;
                i++;
            }else if(next.contentEquals(";")){
                endOfDeclaration();
                i++;
            }
            
        }
    }
    
    private void endOfDeclaration(){
        createVariable();
        reset();
    }

    private void createVariable() {
        if(!silenced)
            Log.d("Found variable "+currentType+" "+currentName+currentArray);
        currentName="";
        currentArray="";
    }

    private void waitForEndOfAssign() {
        //Log.d("wfeoa" + token);
        switch(token){
            case ";":
                endOfDeclaration();
                break;
            case ",":
                createVariable();
                mode=NAME;
                break;
        }
    }

}
   

