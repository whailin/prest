package cppParser.utils;

import cppParser.Log;
import cppParser.StringTools;
import java.util.ArrayList;
import java.util.List;
import cppParser.utils.Constants;
import treeparser.exception.ParseException;
import treeparser.treeobject.Variable;


/**
 * This class is responsible for finding variable declarations inside a c++ function
 * @author Tomi
 */
public class VarFinder
{
    private static final boolean silenced = false;
    private static final String[] delims = {"<", ">"};
    private List<Variable> variables;
    
    private VarFinder recursive=null;
    //private boolean isRecursive=false;
            
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
     * TEMPLATE picks template for variable eg std::vector<std::pair<std::string, int>> values; 
     * 
    */
    private boolean primitive=false;
    
    private String currentType="", currentName="", currentArray="", currentTemplate="", literal="";
    private int templateDepth=0;
    private boolean checkForOperator;
    
    private int i=0; //Current index in the token array
    
    private int arrays=0; // This is for checking arrays inside arrays
    private String token,next;
    
    public VarFinder()
    {
        variables=new ArrayList<>();
    }
    
    private VarFinder(List<Variable> variables){
        this.variables=variables;
        //isRecursive=true;
    }
    
    public void findVariables(String[] tokens){
        
        for(i=0;i<tokens.length;i++){
                
             token=tokens[i];
                
             if(i+1<tokens.length)
                    next=tokens[i+1];
             
             //Log.d("Root Pushing tokens "+token+" "+next+ " "+foundStringLiteral);
             
             //String literals are ignored
             if(foundStringLiteral){
                 if(next!=null){
                    if(token.charAt(token.length()-1)!='\\'){  //Well make sure that there's no escape char before "
                        if(next.contentEquals("\"")){
                            i++;
                            foundStringLiteral=false;

                        }
                    }
                 }
             }else{
                 if(token.contains("\"")){
                     if(next!=null){
                        if(next.charAt(0)=='\''){
                            foundStringLiteral=false;
                            return;
                        }
                     }
                     foundStringLiteral=true;
                     if(next.contains("\"")){
                         i++;
                         foundStringLiteral=false;
                     }
                 }else{
                     
                     pushTokens(token, next);
                 }
             }
             
             
        }
    }
    
    
    public boolean pushTokens(String token, String nextToken){
        //Log.d("Pushing tokens "+token+" "+nextToken+ " "+foundStringLiteral);
        this.token=token;
        this.next=nextToken;
        if(recursive!=null){
            if(recursive.pushTokens(token, nextToken)){
                recursive=null;
                if(mode==RESET){
                    reset();}
            }
        }else if(token.contentEquals("(")||token.contentEquals("{")){
            //Log.d("new Rec");
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
                        lookForNames(token,next);
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
                    case TEMPLATE:
                        pushTokenForTemplate(token,true);
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
        //Log.d("cReset "+token);
        if(token.contentEquals(";"));
        else if(token.contentEquals("{"));
        else return;
        reset();
    }
    private void endOfDeclaration(){
       //Log.d("eod");
        createVariable();
        reset();
    }
    private void reset(){
        templateDepth=0;
        currentType="";
        currentName="";
        currentArray="";
        currentTemplate="";
        primitive=false;
        mode=TYPE;
        arrays=0;
        checkForOperator=false;
        literal="";
    }
    
    private void createVariable() {
        if(!(currentType.isEmpty()|| currentName.isEmpty())) 
            // TBD sometimes some variables pop that have no type or name... They should not make it here
            if(!silenced)
                Log.d("Found variable "+currentType+currentTemplate+" "+currentName+currentArray);
        currentName="";
        currentArray="";
        currentTemplate="";
        templateDepth=0;
        checkForOperator=false;
        literal="";
    }
    

    

    private void lookForType() throws ParseException{
        
        if(next==null)
            return;
        if(token.contentEquals(";"))
            reset();
        else if(token.contains("<")){
            
            String[] splitted=StringTools.split(token, delims, true);
            mode=TEMPLATE;
            if(!splitted[0].contentEquals("<"))
                currentType+=splitted[0];
            else pushTokenForTemplate(splitted[0], false);
            for(int a=1;a<splitted.length;a++)
                    pushTokenForTemplate(splitted[a], false);
        }else{
            //Log.d("lft:"+token);
            if(isWordToken(token)){
                if(canSkip(token))
                    return;
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
                }else if(next.contains("<"));
                else
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

    private void lookForNames(String token, String next) {
        
        //Log.d("lfn:"+token+" "+next);
        if(token.contentEquals("*") ||token.contentEquals("&")){
            currentName+=token;
        
        }else if(isWordToken(token)){            
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
                        i++;
                        reset();
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
    
    private void pushTokenForTemplate(String token, boolean needsSplitting) {
        
        if(needsSplitting){
            String[] tokens=StringTools.split(token, delims, true);
            for(int a=0;a<tokens.length;a++)
                 pushTokenForTemplate(tokens[a],false);      
        }else{
            //Log.d("ptft: "+token);
            if(token.contentEquals(";")){
                reset();
            }else if(token.contentEquals("<")){
                if(checkForOperator){
                    //Log.d("not template "+currentTemplate);
                    reset();
                    return;
                }
                templateDepth++;
                checkForOperator=true;
                currentTemplate+=token;
                return;
            }else checkForOperator=false;
            if(checkForOperator){
                if(token.contentEquals("=")){
                   reset();
                }
            }
            if(templateDepth>0){
                currentTemplate+=token;
                if(token.contentEquals(">")){
                    templateDepth--;
                    
                }
            }else{
                mode=NAME;
                lookForNames(token, this.next);
            }
        }
    }
/**
 * Method checks if given token can be skipped in the variable declaration:
 * returns true for if token is one of following: const, extern, mutable, register or thread_local
 * @param token
 * @return 
 */
    private boolean canSkip(String token) {
        switch(token){
            case "const":
                return true;
            case "extern":
                return true;
            case "mutable":
                return true;
            case "register":
                return true;
            case "thread_local":
                return true;
            default:
                return false;
        }
    }

}
   

