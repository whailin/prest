

package treeparser.treeobject;

/**
 *
 * @author Tomi
 */
public class Variable {
    public ParsedObject owners=null;
    public ParsedObjectLeaf type=null;
    public String name=null;
    public String pointers="";//This strings should have '*'s or '&'s 
    public String arrays=null;
    public String template=null;
    
    public String toString(){
        String str="";
        if(owners!=null){
            
            str+=owners.toString();
        }
        if(type!=null){
            str+=type.toString();
            if(template!=null)
                str+=template;
            str+=" ";
        }
        if(pointers!=null)
            str+=pointers;
        
        if(name==null)
            str+="!no name!";
        else
            str+=name;
        if(arrays!=null)
            str+=arrays;
                    
        
        return str;
    }
    
}
