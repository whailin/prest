

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
    private static final int BEGIN=0, SKIPTOTOKEN=1, SKIP=2, FOR=3, SWITCH=4;
    private int mode=BEGIN;
    private int oldMode;
    private String skipTo;
    private int forStatement;
    private String currentForStatement;
    
    private int parenthesisDepth=0;
    private int skipBrackets=0;
    
    //Special cases for counting lloc in function bodies
    private static final String[] special={"asm","catch","class","do", "else","for", "if","struct", "switch" , "throw", "try","union", "while"}; 
    private List<String> specialCases=new ArrayList<>(Arrays.asList(special)); 
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
        if(false){//Disabled until this thing starts working
        String next;
        for(index=0;index<tokens.length;index++){//TBD bracket skipping
            next=null;
            if((index+1)<tokens.length){
                next=tokens[index+1];
            }
            chooseAction(tokens[index],next);
            
        }
        }
            
        
    }
    
    private void chooseAction(String token, String next){
        switch(mode){
            case BEGIN:
                takeFirstTokens(token, next);
                break;
            case FOR:
                handleFor(token, next);
        }
    }
    private void takeFirstTokens(String token, String next) {
        if(Collections.binarySearch(specialCases, token)>=0){
            handleSpecialCase(token, next);
        }
    }
    
    private void handleSpecialCase(String token, String next) {
        switch(token){
            case "for":
                addLloc();
                mode=FOR;
                forStatement=0;
                currentForStatement="";
                skip();
                break;
            case "if":
            case "else":
                if(next.equals("if"))addLloc();
                break;
                
        }
    }
    private void handleFor(String token, String next){
        switch(token){
            case ";":
                if(!currentForStatement.isEmpty()){
                    currentForStatement="";
                    addLloc();
                }
                forStatement++;
                break;
            case "(":
                parenthesisDepth++;
                break;
            case ")":
                if(parenthesisDepth==0){
                    if(forStatement>0)
                        addLloc();
                    else if(!currentForStatement.isEmpty())
                        addLloc();
                    if(next!=null)
                        if(next.contentEquals("{"))skipBrackets++;
                }else{
                    parenthesisDepth--;
                    if(parenthesisDepth==0);
                }
                break;
            default:
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
    

    
    

}
