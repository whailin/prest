

package treeparser.treeobject;

import treeparser.Type;

/**
 * This is an abstract class of the objects that the parse tree consist of. This tree
 * uses composite pattern.
 * @author Tomi
 */
public abstract class BaseParsedObject {
    protected ParsedObject parent;
    protected String name, content;
    protected Type type;
    
    public BaseParsedObject(ParsedObject parent) {
        this.parent = parent;
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BaseParsedObject(ParsedObject parent, String name, Type type) {
        this.parent = parent;
        this.name = name;
        this.type=type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParsedObject getParent() {
        return parent;
    }

    public void setParent(ParsedObject parent) {
        this.parent = parent;
    }
    
    public int getNumberOfParents(){
        if(parent==null) 
            return 0;
        else 
            return parent.getNumberOfParents()+1;
        
    }
    
    public String getInfo(){
        String str="";
        for(int x=0;x<getNumberOfParents();x++)
            str=str+" ";
        return str+"Name:"+name+ " ";
    }
    public void printDetails(){
        System.out.println(getInfo());
    }
    
    public abstract void printCode();
    public abstract String toString();
    
    protected static String createName(BaseParsedObject parent){
        if (parent==null)
            return "root";
        else if(parent instanceof ParsedObject){
            return parent.getName()+"-C:"+((ParsedObject)parent).getNumberOfChildren();
        }
        return parent.getName()+"-C:0";
    }
}
