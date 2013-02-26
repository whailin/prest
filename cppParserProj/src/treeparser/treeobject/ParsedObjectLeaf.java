

package treeparser.treeobject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import treeparser.Constants;
import treeparser.Type;

/**
 *
 * @author Tomi
 */
public class ParsedObjectLeaf extends BaseParsedObject{
    private List<ParsedObjectLeaf> list=null;   //Sometimes the leaf can be splitted to smaller leafs, they are stored here
    public ParsedObjectLeaf(ParsedObject parent, String name, Type type, String content) {
        super(parent, name, type);
        this.content=content;
        if(type==Type.OTHER)
            list=split(content);
        

    }
    
    public ParsedObjectLeaf(String name, Type type, String content) {
        super(null,name, type);
        this.content=content;
        if(type==Type.OTHER)
            list=split(content);
        

    }
    
    public ParsedObjectLeaf(ParsedObject parent, Type type, String content) {
        
        super(parent,createName(parent), type);
        this.content=content;
        if(type==Type.OTHER)
            list=split(content);
        
        
    }
    
    public boolean isSplittable(){
        if(list==null)
            return false;
        else 
            return true;
    }
    public List<ParsedObjectLeaf> getSplittedList(){
        return list;
    }
    

    @Override
    public void printDetails(){
        
        if(type==Type.NEWLINE)
            System.out.print("");
        else if(list==null)
            System.out.print(content);
        /*else{
            System.out.println(spaces+"Leaf contains: "+list.size()+" objects");
            for(ParsedObjectLeaf obj:list){
                if(obj.type==Type.NEWLINE)
                    System.out.println(spaces+"Leaf contains newline");
                else
                    System.out.println(spaces+"Leaf contains: "+ obj.type+" "+obj.content);
            }
        }*/
    }
    
    /**
     * This method splits the code and separates preprocessor commands from other code
     * @param str
     * @return 
     */
    private final List<ParsedObjectLeaf> split(String str){
        //System.out.println("Splitting "+ str);
            List<ParsedObjectLeaf> l=new ArrayList<ParsedObjectLeaf>();
            String temp=str;
            if(temp.contains("#")){ //Preprocessor code found
                int index1=-1;
                int indexEnd=-1;
                for(int i=0;i<temp.length();i++){
                    if(index1==-1){
                        if(str.charAt(i)=='#')
                            index1=i;
                    }else{
                        if(str.charAt(i)=='\n' || str.charAt(i)=='\r'){
                            indexEnd=i;
                            break;
                        }
                    }
                }
                if(index1>0) 
                    l.addAll(parseNonPreProcessorCode(temp.substring(0, index1)));
                if(indexEnd==-1){ 
                    l.add(new ParsedObjectLeaf(parent, Type.PREPROCESSOR,temp.substring(index1)));
                }
                else if(indexEnd>0){
                    l.add(new ParsedObjectLeaf(parent, Type.PREPROCESSOR,temp.substring(index1,indexEnd)));
                    l.addAll(parseNonPreProcessorCode(temp.substring(indexEnd)));
                }
            }else
                l=parseNonPreProcessorCode(str);
            //printList(l);
            return l;
            //System.out.println("Splitting done");
    }
    
    private void printList(List<ParsedObjectLeaf> list){
        Iterator<ParsedObjectLeaf> it=list.iterator();
        while(it.hasNext()){
            ParsedObjectLeaf obj=it.next();
            if(obj.type!=Type.NEWLINE)
                System.out.println(obj.type+" "+obj.getContent());
        }
    }
    
    /**
     * This method further tokenizes the content in the leaf
     * @param str
     * @return 
     */
    private List<ParsedObjectLeaf> parseNonPreProcessorCode(String str){
        List<ParsedObjectLeaf> l=new ArrayList<ParsedObjectLeaf>();
        char c;
        String temp="";
        for(int x=0;x<str.length();x++){
            c=str.charAt(x);

            if(c==' ' || c=='\t'){
                if(!temp.isEmpty()){
                    l.add(new ParsedObjectLeaf(parent, Type.WORDTOKEN,temp));
                    temp="";
                }   
            }else if(c=='\n' || c=='\r'){
                
                if(!temp.isEmpty()){
                    l.add(new ParsedObjectLeaf(parent, Type.WORDTOKEN,temp));
                    temp="";
                }
                //l.add(new ParsedObjectLeaf(parent, Type.NEWLINE,"\n"));
            }else if(Constants.isSpecialChar(c)){
                
                if(!temp.isEmpty()){
                    l.add(new ParsedObjectLeaf(parent, Type.WORDTOKEN,temp));
                    temp="";
                }
                if(x<(str.length()-1)){
                    String twoChars=""+c+str.charAt(x+1);
                    if(Constants.isTwoCharOperator(twoChars)){
                        l.add(new ParsedObjectLeaf(parent, Type.SPECIAL,twoChars));
                        x++;
                    }else
                        l.add(new ParsedObjectLeaf(parent, Type.SPECIAL,""+c));
                }else
                    l.add(new ParsedObjectLeaf(parent, Type.SPECIAL,""+c));
                
            }else if(Constants.isValidNameChar(c)){
                
                temp=temp+c;
            }
        }
        if(!temp.isEmpty()){
            l.add(new ParsedObjectLeaf(parent, Type.WORDTOKEN,temp));
        } 
        return l;
    }
    @Override
    public String toString(){
        return content;
    }
    @Override
    public void printCode() {
        printDetails();
    }
   
    
    
    
    
}
