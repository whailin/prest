

package treeparser;

import java.util.ArrayList;
import java.util.List;
import treeparser.treeobject.BaseParsedObject;
import treeparser.treeobject.ParsedObject;
import treeparser.treeobject.ParsedObjectLeaf;

/**
 *
 * @author Tomi
 */
public class SentenceFormer {
    ParsedObject parent;
    static int x=0;
    private ArrayList<BaseParsedObject> list=new ArrayList<BaseParsedObject>();
    private Type type=Type.SIMPLESENTENCE;

    public SentenceFormer(ParsedObject parent) {
        this.parent=parent;
    }
    public SentenceFormer() {
        this.parent=null;
    }
    public ParsedObject push(ParsedObject obj){
        if(obj.getType()==Type.COMMENT)
            return null;
        //System.out.print("Adding "+obj.getType());
        list.add(obj);
        
        if(obj.getContent().contentEquals("{")){
            type=Type.SENTENCE;
            //obj.formSentences();
            if(list.get(list.size()-2) instanceof ParsedObject){
                ParsedObject temp=(ParsedObject)list.get(list.size()-2);
                if(temp.getContent().contentEquals("(")){
                    temp=createSentence("Sentence",list);
                    list.clear();
                    type=Type.SIMPLESENTENCE;
                   return temp;
                }    
            }
        }
        return null;
    }
    
    public ParsedObject push(BaseParsedObject obj){
        if(obj instanceof ParsedObjectLeaf)
            return push((ParsedObjectLeaf)obj);
        else if(obj instanceof ParsedObject)
            return push((ParsedObject)obj);
        else throw new Error ("Unexpected object type");
    }
    
    public ParsedObject push(ParsedObjectLeaf obj){
        if(obj.getType()==Type.COMMENT)
            return null;
        list.add(obj);
        if(obj.getContent().contentEquals(";")){
            ParsedObject temp=createSentence("Sentence", list);
            list.clear();
            return temp;
        }
        return null;
    }
/**
 * This should be called if the sentence former did not return a sentence after last token
 * it returns null if there was no tokens to form sentences when the method was called
 * @return 
 */
    public ParsedObject noMoreTokens() {
        if(list.isEmpty())
            return null;
        else{
            ParsedObject temp=createSentence("Sentence", list);
            list.clear();
            return temp;
        }
    }
    
    private ParsedObject createSentence(String name, List<BaseParsedObject> list){
        ParsedObject obj=new ParsedObject(parent,name,type);
        obj.addChildren(list);
        System.out.println("Sentence created:");
        obj.printCode();
        //System.out.println("");
        return obj;
    }

    
}
