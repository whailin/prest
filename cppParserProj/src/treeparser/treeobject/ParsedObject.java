

package treeparser.treeobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import treeparser.SentenceFormer;
import treeparser.Type;
// import com.thoughtworks.xstream.XStream;

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
    /**
     * This method goes through the list of children and forms sentences from them
     */
    public void formSentences(){
        ArrayList<BaseParsedObject> list=new ArrayList<BaseParsedObject>();
        if(content.contentEquals("{")){
            int i=0;
            SentenceFormer sf=new SentenceFormer(this);
            for(BaseParsedObject obj:childObjects){
                if (i==0){
                    list.add(obj);
                }else if( i==(childObjects.size()-1)){//ignoring brackets
                    BaseParsedObject o=sf.noMoreTokens();
                    if(o!=null)
                        list.add(o);
                    list.add(obj);
                }else{
                    BaseParsedObject newObj=null;
                    if(obj instanceof ParsedObject){
                        ((ParsedObject)obj).formSentences();
                        newObj=sf.push((ParsedObject)obj);
                    }else if(obj instanceof ParsedObjectLeaf){
                        newObj=sf.push((ParsedObjectLeaf)obj);
                        
                    }
                    if(newObj!=null){
                        newObj.printCode();
                        System.out.println("New obj:"+newObj.getType());
                        list.add(obj);
                    }
                }
                i++;
            }
            childObjects.clear();
            childObjects.addAll(list);
            for(BaseParsedObject o:list)
                System.out.println("List:"+o.getType());
            listObjects(this);
            System.out.println("formed "+list.size()+" "+childObjects.size());
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

    
    

    
    
    
    
}
