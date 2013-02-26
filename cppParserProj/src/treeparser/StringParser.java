

package treeparser;

import treeparser.treeobject.ParsedObject;
import treeparser.treeobject.ParsedObjectLeaf;
import treeparser.treeobject.BaseParsedObject;
import treeparser.exception.ParseException;
import treeparser.exception.StringParseException;

/**
 * This class parses strings
 * @author Tomi
 */
public class StringParser extends Parser{
    private char lastChar='0';
    private boolean firstTime=true;
    public StringParser(ParsedObject parent){
        super(parent);
        
    }
    @Override
    public BaseParsedObject push(char c) throws ParseException{
        if(!firstTime){
            if(c=='\n')
                throw new StringParseException("Expected \" but line ended when parsing:"+this.chars);
             else
                chars=chars+c;
            if(c=='\"'&& lastChar!='\\'){
                
                return new ParsedObjectLeaf(this.parent, Type.STRING, this.chars);
            }
           
        }else{
           chars=chars+c;
           firstTime=false;
        }
        lastChar=c;
        return null;
    }

    @Override
    public BaseParsedObject close() {
        chars=chars+"\"";
        return new ParsedObjectLeaf(this.parent, Type.STRING, this.chars);
    }
    

}
