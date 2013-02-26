

package treeparser;

import treeparser.treeobject.ParsedObject;
import treeparser.treeobject.BaseParsedObject;
import treeparser.exception.ParseException;

/**
 *
 * @author Tomi
 */
public abstract class Parser {
    public Parser(ParsedObject parent){
        this.parent=parent;
    }
    protected ParsedObject parent, currentObject;
    protected String chars="";
    public abstract BaseParsedObject push(char c) throws ParseException;
    public abstract BaseParsedObject close();

}
