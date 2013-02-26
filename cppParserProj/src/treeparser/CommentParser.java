

package treeparser;

import treeparser.treeobject.ParsedObject;
import treeparser.treeobject.ParsedObjectLeaf;
import treeparser.treeobject.BaseParsedObject;
import treeparser.exception.ParseException;

/**
 * This class parses comments
 * @author Tomi
 */
public class CommentParser extends Parser{
    private char lastChar='0';
    private boolean commentBlock;

    public CommentParser(ParsedObject obj, boolean commentBlock) {
        super(obj);
        this.commentBlock=commentBlock;
    }

    @Override
    public BaseParsedObject push(char c) throws ParseException {
        if((c=='\r'||c=='\n') && !commentBlock)
            return new ParsedObjectLeaf(parent,Type.COMMENT,chars);
        chars=chars+c;
        if(chars.length()>3){
            if(lastChar=='*'&&c=='/')
                return new ParsedObjectLeaf(parent,Type.COMMENT,chars);
        }
        lastChar=c;
        return null;
            
    }

    @Override
    public BaseParsedObject close() {
        if(commentBlock)
            chars=chars+"*/";
        return new ParsedObjectLeaf(parent,Type.COMMENT,chars);
    }
    
}
