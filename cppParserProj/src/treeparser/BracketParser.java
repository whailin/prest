

package treeparser;

import treeparser.exception.ParseException;
import treeparser.treeobject.BaseParsedObject;
import treeparser.treeobject.ParsedObject;
import treeparser.treeobject.ParsedObjectLeaf;

/**
 * This class parses different types of brackets and creates tree with them
 * @author Tomi
 */
public class BracketParser extends Parser{
    private char bracketType, endBracket;
    private Parser parser=null;
    private boolean firstTime=true;
    private char lastChar=' ';
    public BracketParser(ParsedObject parent, char bracketType) {
        super(parent);
        this.bracketType=bracketType;
        this.currentObject=new ParsedObject(parent, Type.BRACKET,""+bracketType);
        
        selectEndBracket();
    }
    
    private void selectEndBracket() {
        switch(bracketType){
            case '{':
                endBracket='}';
                break;
            case '(':
                endBracket=')';
                break;
            case '[':
                endBracket=']';
                break;
            default:
                throw new Error("invalid bracket. Got "+ bracketType+" Expected: {, ( or [");
        }
    }
    
    @Override
    public BaseParsedObject push(char c) throws ParseException{
        if(parser!=null){
            BaseParsedObject obj=parser.push(c);
            if(obj!=null){
                parser=null;
                if(!(currentObject.getType()==Type.COMMENT || currentObject.getType()==Type.NEWLINE)) //Skip comments
                currentObject.addChild(obj);
            }
            lastChar=c;
            
        }else{
            if(!firstTime){
                if(c=='\n'){
                    addLeaf();
                    //currentObject.addChild(new ParsedObjectLeaf(currentObject,Type.NEWLINE,""+c));
                    
                }else if(lastChar=='/' && c=='/'){
                    eraseLastChar();
                    addLeaf();
                    parser=new CommentParser(currentObject, false);
                    push(lastChar);
                    push(c);
                }else if(lastChar=='/' && c=='*'){
                    eraseLastChar();
                    addLeaf();
                    parser=new CommentParser(currentObject, true);
                    push(lastChar);
                    push(c);
                }else if(c=='{' || c=='(' || c=='['){
                    addLeaf();
                    parser=new BracketParser(currentObject,c);
                    push(c);
                }else if(c=='\"'){
                    addLeaf();
                    parser=new StringParser(currentObject);
                    push(c);
                }else if(c=='\''){
                    addLeaf();
                    parser=new SingleCharParser(currentObject);
                    push(c);
                }else if(c==endBracket){
                    addLeaf();
                    currentObject.addChild(new ParsedObjectLeaf(currentObject,Type.SPECIAL,""+c));
                    return currentObject;
                }else{
                    chars=chars+c;
                }
                lastChar=c;    

                
            }else{
                
                lastChar=c;
                chars=chars+c;
                //First received character should always be {, ( or [
                currentObject.addChild(new ParsedObjectLeaf(currentObject,Type.SPECIAL,""+c)); 
                chars="";
                firstTime=false;
            }
        }
        return null;
    }
    
    private void addLeaf(){
        chars=chars.trim();
        if(!chars.isEmpty()){
            //System.out.println("Adding leaf:"+chars);
            ParsedObjectLeaf obj=new ParsedObjectLeaf(currentObject,Type.OTHER,this.chars);
            if(obj.isSplittable())
                currentObject.addChildren(obj.getSplittedList());
            else
                currentObject.addChild(obj);
            chars="";
        }
    }

    

    @Override
    public BaseParsedObject close() {
        if(parser!=null){
            BaseParsedObject b=parser.close();
            if(b!=null)
                currentObject.addChild(b);
        }
        chars=chars+endBracket;
        addLeaf();
        return currentObject;
        
    }

    private void eraseLastChar() {
        if(chars.length()<=1)
            chars="";
        else{
            chars=chars.substring(0, chars.length()-1);
        }
    }

}
