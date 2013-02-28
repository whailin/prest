

package treeparser;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import treeparser.exception.ParseException;
import treeparser.treeobject.BaseParsedObject;
import treeparser.treeobject.FunctionCall;
import treeparser.treeobject.ParsedObject;
import treeparser.treeobject.ParsedObjectLeaf;

/**
 *
 * @author Tomi
 */
public class FunctionAnalyzer {
    
    
    public static List<FunctionCall> findFunctionCalls(ParsedObject object){
        List<FunctionCall> fcs=new ArrayList();
        return findFunctionCalls(object,fcs);
    }
    /*
     * Found function calls are collected to the functions list.
     * This method is called recursively so it's private. 
     */
    private static List<FunctionCall> findFunctionCalls(ParsedObject object, List<FunctionCall> functions){
         List<FunctionCall> fcs=functions;
        if(object.getType()==Type.BRACKET){
            //ParsedObject sentence=new ParsedObject(null,"Sentence",Type.SENTENCE);
            List<BaseParsedObject> list=object.getChildren();
            Iterator<BaseParsedObject> it=list.iterator();
            String bracket;
            boolean firstTime=true;
            
            String lastWord=null;
            
            while(it.hasNext()){
                
                BaseParsedObject obj=it.next();
                
                if(firstTime){
                    /*if(obj instanceof ParsedObjectLeaf){
                        bracket=object.getContent();
                    }else
                        System.out.println("Expected bracket leaf not found:"+ obj.getContent());*/
                    firstTime=false;
                }else if(!canIgnore(obj)){

                    if(obj instanceof ParsedObjectLeaf){
                        ParsedObjectLeaf pol=(ParsedObjectLeaf) obj;
                        if(pol.getType()==Type.WORDTOKEN){
                            lastWord=pol.getContent();
                            if(Constants.isKeyword(lastWord))
                                lastWord=null;
                        }else
                            lastWord=null;
                    }else if(obj instanceof ParsedObject){
                        
                        ParsedObject po=(ParsedObject)obj;
                        char b=getBracketType(po);
                        if(b=='('){
                            if(lastWord!=null){
                                FunctionCall f=createFunctionCall(po.getParent(),po);
                                if(f!=null)
                                    fcs.add(f);
                                else
                                    System.out.println(lastWord+" called");
                            }
                        }else if(b=='{'){
                            findFunctionCalls(po, fcs);
                        }
                        lastWord=null;
                    }
                }
            }
            
        }
        return fcs;
        
    }
    
    private static boolean canIgnore(BaseParsedObject obj) {
        Type t=obj.getType();
        if(t==Type.COMMENT)
            return true;
        else if(t==Type.NEWLINE)
            return true;
        return false;
    }
    
    private static char getBracketType(ParsedObject obj){
            
            if(obj.getChildren().get(0) instanceof ParsedObjectLeaf)
                return ((ParsedObjectLeaf)obj.getChildren().get(0)).getContent().charAt(0);
            else throw new Error("Not a bracket object");
        
    }
/**
 * This method picks all attributes from ParsedObject to form one function call
 * @param obj ParsedObject that contains all components that are needed to create FunctionCall
 * (function name, parameters, owner of the function)
 * @param parameters parameter ParsedObject is used to find where is the last piece of the function call
 * 
 * example of ParsedObject obj:
 * {
 *  int a;
 *  abc::cd::hello(a); //contains ParsedObjectLeaf "abc", "::", "hello" and ParsedObject parameters="(a)".
 * }
 * createFunctionCall(obj,parameters) would return FunctionCall with ParsedObject owner={"abc","::", "cd"}, name="hello" and parameters=(a)
 */
    private static FunctionCall createFunctionCall(ParsedObject obj, ParsedObject parameters) {
        int i=obj.getChildren().indexOf(parameters);
        ParsedObject po=new ParsedObject(null,"FunctionCall",Type.OTHER);
        ParsedObjectLeaf name=null;
        BaseParsedObject temp;
        int lastRef=0;
        if(i==-1)
            throw new UnsupportedOperationException("Function parameters not found in obj");
        else{
            if((i-1>=0)){
                if(obj.getChildren().get(i-1) instanceof ParsedObjectLeaf){
                    name=(ParsedObjectLeaf)obj.getChildren().get(i-1);
                    
                    
                }else{ 
                    System.out.println("null returned");
                    return null;
                }
            }
            for(int x=1;(i-x)<0;x++){//This loop looks back from name of the function to find out how many owners it has
                temp=obj.getChildren().get(i-x);
                if(temp instanceof ParsedObjectLeaf){
                    ParsedObjectLeaf pol=(ParsedObjectLeaf)temp;
                    if(isEndOfSentence(pol)){
                        //end=x;
                        break;
                    }else if(isReferenceOperator(pol))
                        lastRef=x; //Last owner is before this
                }
            }
            if(lastRef>0){
                int a=lastRef;
                if((i-lastRef-1)>=0){ //Check if the owner before first :: is function or attribute
                    a++;
                    temp=obj.getChildren().get(i-a);
                    
                    if(temp.getType()==Type.BRACKET){
                        if((i-a-1)>=0){
                            a++;
                        }
                    }                 
                }else{                    
                    //throw new ParseException("Not valid code, expected function call or variable before ::, ->, or .");
                }
 
               for(;a>0;a--){
                   temp=obj.getChildren().get(i-a);
                   po.addChild(temp);
               }
            }

            return new FunctionCall(po,name,parameters);
        }
        
    }
    
    private static boolean isReferenceOperator(ParsedObjectLeaf leaf){
        String str=leaf.getContent();
        if(str.contentEquals("::"))
            return true;
        else if(str.contentEquals("."))
            return true;
        else if(str.contentEquals("->"))
            return true;
        return false;
    }
    private static boolean isEndOfSentence(ParsedObjectLeaf leaf){
        String str=leaf.getContent();
        if(str.contentEquals(","))
            return true;
        else if(str.contentEquals("="))
            return true;
        return false;
    }


}
