

package treeparser.treeobject;

import treeparser.*;
import treeparser.exception.ParseException;

/**
 *
 * @author Tomi
 */
public class TreeFactory {
    private boolean firstTime=true;
    private char lastChar='0';
    private Parser parser;
    private ParsedObject root=new ParsedObject(null,"",Type.ROOT);
    private String chars="";
    public void pushChar(char c) throws ParseException {
        if(firstTime){
            firstTime=false;
            lastChar=c;
        }else{
            if(parser!=null){
                BaseParsedObject obj=parser.push(lastChar);
                if(obj!=null){
                    
                    root.addChild(obj);
                    parser=null;
                }
                
            }else{
                
                if(lastChar=='/' && c=='/'){
                    addLeaf();
                    parser=new CommentParser(root, false);
                    parser.push(lastChar);
                }else if(lastChar=='/' && c=='*'){
                    addLeaf();
                    parser=new CommentParser(root, true);
                    parser.push(lastChar);
                }else if(lastChar=='{' || lastChar=='(' || lastChar=='['){
                    addLeaf();
                    parser=new BracketParser(root,lastChar);
                    parser.push(lastChar);
                }else if(lastChar=='\"'){
                    addLeaf();
                    parser=new StringParser(root);
                    parser.push(lastChar);
                }else if(lastChar=='\''){
                    addLeaf();
                    parser=new SingleCharParser(root);
                    parser.push(lastChar);
                }else
                    chars=chars+lastChar;

            }
            
            lastChar=c;
        }
    }
    private void addLeaf(){
        chars=chars.trim();
        if(!chars.isEmpty()){
            ParsedObjectLeaf obj=new ParsedObjectLeaf(root,Type.OTHER,chars);
            if(obj.isSplittable())
                root.addChildren(obj.getSplittedList());
            else
                root.addChild(obj);
            chars="";
        }
    }
    public ParsedObject eof() throws ParseException{
        System.out.println("End of file");
        if(parser!=null){
            BaseParsedObject obj=parser.push(lastChar);
            if(obj!=null)
                root.addChild(obj);
            else{
                BaseParsedObject obj2=parser.close();
                if(obj2!=null)
                    root.addChild(obj2);
            }
        }
        addLeaf();
        ParsedObject temp=root;
        root=new ParsedObject(null,"",Type.ROOT);
        return temp;
        
        
    }
    
    public ParsedObject stringToTree(String str)throws ParseException{
        ParsedObject temp=root;
        root=new ParsedObject(null,"",Type.ROOT);
        for(int x=0;x<str.length();x++){
            pushChar(str.charAt(x));
        }
        ParsedObject temp2=eof();
        root=temp;
        return temp2;
    }
}
