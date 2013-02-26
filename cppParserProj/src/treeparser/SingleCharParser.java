

package treeparser;

import treeparser.treeobject.ParsedObject;
import treeparser.treeobject.ParsedObjectLeaf;
import treeparser.treeobject.BaseParsedObject;
import treeparser.exception.ParseException;

/**
 * This class parses hard coded characters such as "'a'"
 * @author Tomi
 */
public class SingleCharParser extends Parser{

    public SingleCharParser(ParsedObject parent) {
        super(parent);
    }

    @Override
    public BaseParsedObject push(char c) throws ParseException {
        chars=chars+c;
        if(c=='\'' && chars.length()>2)
            return new ParsedObjectLeaf(parent,Type.CHARLITERAL ,chars);
        return null;
    }

    @Override
    public BaseParsedObject close() {
        return new ParsedObjectLeaf(parent,Type.CHARLITERAL,chars);
    }

}
