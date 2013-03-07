

package treeparser.treeobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import treeparser.SentenceFormer;
import treeparser.Type;

/**
 *
 * @author Tomi
 */
public class ParsedObject extends BaseParsedObject{
    protected ArrayList<BaseParsedObject> childObjects=new ArrayList<BaseParsedObject>();
    public ParsedObject(ParsedObject parent, String name, Type type) {
        super(parent, name, type);
        content="";

    }
    
    public ParsedObject(ParsedObject parent, Type type, String content) {
        super(parent, createName(parent), type);
        this.content=content;
    }
    
    @Override
    public void printDetails(){
        String spaces="";
        int parents=this.getNumberOfParents();
        for(int x=0;x<parents+1;x++)
            spaces+=" ";
        System.out.print("\n"+spaces);
        Iterator<BaseParsedObject> it=childObjects.iterator();
        while(it.hasNext()){
            BaseParsedObject obj=it.next();
           
            //if(obj instanceof ParsedObjectLeaf){
                obj.printDetails();
            //}else if(obj instanceof ParsedObject){
             /*   ParsedObject po=(ParsedObject)obj;
                if
            }*/
        }
        
    }
    
    
    
    
    public void addChild(BaseParsedObject obj){
        childObjects.add(obj);
    }
    
    public void addChildren(Collection<? extends BaseParsedObject> obj){
        childObjects.addAll(obj);
    }
    public int getNumberOfChildren(){
        return childObjects.size();
    }
    
    public List<BaseParsedObject> getChildren(){
        return childObjects;
    }
    
    public void removeChildren(){
        childObjects.clear();
    }
    /**
     * Method replaces given BaseParsedObject with a list of objects, does nothing 
     * if target object is not found in the list where it's supposed to be replaced
     */
    public void replaceLeaf(BaseParsedObject target, List<BaseParsedObject> replacer){
        int index=childObjects.indexOf(target);
        if(index!=-1){
            childObjects.remove(target);
            childObjects.addAll(index, replacer);
        }
        
    }
    
    
    private static void listObjects(ParsedObject root) {
        System.out.println("Listing");
        ArrayList<Iterator<BaseParsedObject>> iters=new ArrayList();
        BaseParsedObject current;
        Iterator<BaseParsedObject> i=root.getChildren().iterator();
        iters.add(i);
        while(iters.size()>0){
            while(i.hasNext()){
                
                BaseParsedObject obj=i.next();
                //System.out.println(obj.getType());
                if(obj.getType()==Type.SENTENCE)
                    System.out.print("Sentence found");
                if(obj.getType()==Type.SIMPLESENTENCE)
                    System.out.print(obj.toString());
                if(obj instanceof ParsedObjectLeaf){
                    System.out.print(obj.toString());
                }else{
                    i=((ParsedObject) obj).getChildren().iterator();
                    iters.add(i);
                }
                
                
            }
            if(iters.size()==1){
                iters.remove(iters.size()-1);
            }else if((iters.size()-1)>0){
                iters.remove(iters.size()-1);
                i=iters.get(iters.size()-1);
            }
        }
        
    }

    @Override
    public void printCode() {
        String str="";
        if(!childObjects.isEmpty()){
        for(BaseParsedObject obj:childObjects){
            if(obj.getType()==Type.BRACKET){
                str+=obj.getContent()+"...";
            }
            if(obj instanceof ParsedObjectLeaf){
                str+=" "+obj.content;
            }
            
        }
        System.out.println(str);
        }
    }
    
    @Override
    public String toString(){
        String str="";
        for(BaseParsedObject o:childObjects)
            str+=o.toString();
        return str;
    }
    
    public String toSimpleString(){
        String str="";
        for(BaseParsedObject o:childObjects){
            if(o instanceof ParsedObject){
                ParsedObject obj=(ParsedObject)o;
                if(obj.getType()==Type.SIMPLESENTENCE)
                    str+=obj.toString();
                else if(obj.getType()==Type.BRACKET){
                    int size=obj.getChildren().size();
                    str+=obj.getChildren().get(0).getContent();
                    if(size>=2)str+="...";
                    str+=obj.getChildren().get(size-1).getContent();
                }
            }else
                str+=o.toString();
        }
        return str;
    }

    
    

    
    
    
    
}
