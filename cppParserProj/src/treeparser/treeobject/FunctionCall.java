

package treeparser.treeobject;

/**
 *  This class represents a function call that is found by function analyzer.
 *  Parameters and owners can be or contain other function calls which are not yet
 *  analyzed well enough to find out what are the final types of the parameters to
 *  accurately tell which function was called. eg this can't differentiate these two:
 *  hello("test");
 *  hello(1);
 * @author Tomi
 */
public class FunctionCall {
    public ParsedObject owners=null;
    public ParsedObjectLeaf functionName;
    public ParsedObject parameters;
    public FunctionCall(ParsedObjectLeaf name, ParsedObject parameters){
        this.functionName=name;
        this.parameters=parameters;
    }
    
    public FunctionCall(ParsedObject owners,ParsedObjectLeaf name, ParsedObject parameters){
        this.owners=owners;
        this.functionName=name;
        this.parameters=parameters;
    }
    
    public String toString(){
        String p1="";
        if(owners!=null)
            p1=owners.toSimpleString();
        String p2=functionName.getContent();
        String p3=parameters.toSimpleString();
        return p1+p2+p3;
    }
    
            

}
