

package cppParser.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Tomi
 */
public class LLOCCounter{
    private static final int BEGIN=0, SKIPTONEXT=1, SKIPPARENTHESIS=2, SKIPTOCOLON=3, SKIPTOBRACKET=4, FOR=5;
    private int mode=BEGIN;
    private String currentForStatement;
    
    private int parenthesisDepth=0;
    
    //Special cases for counting lloc in function bodies
    private static final String[] special={"case","catch","class","default","do", "else","for", "if","private","protected","public","struct", "switch" , "try","union", "while"}; 
    private static final List<String> specialCases=new ArrayList<>(Arrays.asList(special)); 
    private String file;
    private int lloc=0;
    private int index;
    /**
     * This method checks logical lines of code when the tokens are not in function body
     * @param tokens 
     */
    public void processSentence(String[] tokens){
        
    }
    /**
     * This method checks logical lines of code when the tokens are in function body
     * @param tokens 
     */
    public void processSentenceInFuncBody(String[] tokens){
        String next;
        for(index=0;index<tokens.length;index++){//TBD bracket skipping
            next=null;
            if((index+1)<tokens.length){
                next=tokens[index+1];
            }
            chooseAction(tokens[index],next);
        }  
    }
    
    private void chooseAction(String token, String next){
        switch(mode){
            case BEGIN:
                takeFirstTokens(token, next);
                break;
            case FOR:
                handleFor(token, next);
                break;
            case SKIPTONEXT:
                skipToNextStatement(token);
                break;
            case SKIPTOCOLON:
                skipColon(token);
                break;
            case SKIPPARENTHESIS:
                skipParenthesis(token);
                break;
            case SKIPTOBRACKET:
                skipToBracket(token);
                break;
            
        }
    }
    private void takeFirstTokens(String token, String next) {
        if(token.contentEquals("{"))
            return;
        else if(token.contentEquals("{"))
            return;
        if(Collections.binarySearch(specialCases, token)>=0){
            handleSpecialCase(token, next);
        }else{
            switch(token){
                case ";":
                    reset(); //empty statements are not counted
                    break;
                case "case":
                case "default":
                    mode=SKIPTOCOLON;
                    break;
                default:
                    addLloc();
                    mode=SKIPTONEXT;
            }
           
        }
    }
    
    
    private void handleSpecialCase(String token, String next) {
        switch(token){
            case "for":
                addLloc();
                mode=FOR;
                currentForStatement="";
                parenthesisDepth=0;
                break;
            case "try":
            case "do":
                break; //Do and try are not counted separately
            case "switch":
            case "catch":
            case "while":
            case "if":
                addLloc();
                mode=SKIPPARENTHESIS;
                break;
            case "default":
            case "case":
                addLloc();
                mode=SKIPTOCOLON;
                break;
            case "else":
                if(next.equals("if")){
                    addLloc();
                    skip();
                    mode=SKIPPARENTHESIS;
                }
                break;
            case "private":
            case "protected":
            case "public":
                addLloc();
                skip();
                break;
            case "class":
            case "struct":
            case "union":
                addLloc();
                mode=SKIPTOBRACKET;
                
                
                
        }
    }
    private void handleFor(String token, String next){
        switch(token){
            case ";":
                if(!currentForStatement.isEmpty()){
                    currentForStatement="";
                    addLloc();
                }
                break;
            case "(":
                parenthesisDepth++;
                break;
            case ")":
                parenthesisDepth--;
                if(parenthesisDepth==0){
                    if(!currentForStatement.isEmpty())
                        addLloc();
                    reset();
                }
                break;
            default:
                currentForStatement+=token;
        }
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
    
    public void addLloc(){
        lloc++;
    }

    public int getLloc() {
        return lloc;
    }

    public void setLloc(int lloc) {
        this.lloc = lloc;
    }

    private void skip(){
        index++;
    }

    private void reset() {
        mode=BEGIN;
        
    }

    private void skipToNextStatement(String token) {
        if(token.contentEquals(";"))
            reset();
    }

    private void skipParenthesis(String token) {
        switch(token){
            case "(":
                parenthesisDepth++;
                break;
            case ")":
                parenthesisDepth--;
                if(parenthesisDepth<=0)
                    reset();
                break;
                
        }
    }

    private void skipColon(String token) {
        if(token.contentEquals(":"))
            reset();
    }

    private void skipToBracket(String token) {
        switch(token){
            case ";":
            case "{":
                reset();
            //case ":":
        }
    }

   

    
    

}
