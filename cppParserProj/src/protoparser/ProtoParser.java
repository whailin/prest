
package protoparser;


import java.io.*;
import java.util.List;
import treeparser.FunctionAnalyzer;
import treeparser.treeobject.FunctionCall;
import treeparser.treeobject.ParsedObject;
import treeparser.treeobject.TreeFactory;

/**
 *
 * @author Tomi
 */
public class ProtoParser {
   
    
    public static void main(String[] args) throws Exception{
       String dest= "H:\\cpp\\simple\\main.cpp";
       
       File file=new File(dest);
        try {
           ParsedObject obj=loadFile(file);
           /* The parsed file should only contain 
            * the body of the function that is to be analyzed eg
            * if function is void hello(){
            * int blaa=1;
            * }
            * file should contain
            * {
            * int blaa=1;
            * }
            * 
            * alternatively the body can be passed as a string to the TreeFactory:
            * TreeFactory tf=new TreeFactory();
            * String str="{hello();}
            * ParsedObject obj=tf.stringToTree(str);
            */
           //obj is root node which was supposed to take more than just the function body. 
           //we have to take the function body here, the array should not contain anything else
           List<FunctionCall> fcs=FunctionAnalyzer.findFunctionCalls((ParsedObject)obj.getChildren().get(0)); 
           
           System.out.println("Number of calls:"+fcs.size());
           for(FunctionCall f:fcs)                  //this loop lists all the methods that were called
                System.out.println(f.toString()); 
	    
	} catch (FileNotFoundException e) {e.printStackTrace();}
                
    }
    public static ParsedObject loadFile(File file) throws FileNotFoundException{
        TreeFactory tf=new TreeFactory();
        ParsedObject obj=null;
        BufferedReader br=new BufferedReader(new FileReader(file));

        boolean EOF=false;
        try{
        	while(!EOF){
                        String line=br.readLine();
        		if(line==null){
        			EOF=true;
        		}else{
                            int length=line.length();
                            for(int x=0;x<length;x++){
                                char character=line.charAt(x);
                                tf.pushChar(character);
                            }
        			tf.pushChar('\n');	
        		}
        	}
                obj=tf.eof();
                
                
                
                
        	
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }finally{
        	try {
				br.close();
			} catch (IOException e) {}
        }
        return obj;

    }

    

   /* private static void doTreeWalk() {
        System.out.println("Doing tree walk");
        //root.printDetails();
        List<BaseParsedObject> list=root.getChildren();
        Iterator<BaseParsedObject> it=list.iterator();
        System.out.println(list.size());
        int i=0;
        while(it.hasNext()){
                BaseParsedObject obj=it.next();
                System.out.println(i++ +":"+obj.getName());
                /*if(obj instanceof ParsedObject)
                    findFunctionCalls((ParsedObject)obj);*/
       // }
        
    
        
        
   // }
    
    
    
    
    

 /*   private static void listObjects(ParsedObject root) {
        int sentences=0;
        System.out.println("Listing");
        ArrayList<Iterator<BaseParsedObject>> iters=new ArrayList();
        BaseParsedObject current;
        Iterator<BaseParsedObject> i=root.getChildren().iterator();
        iters.add(i);
        while(iters.size()>0){
            while(i.hasNext()){
                
                BaseParsedObject obj=i.next();
                //System.out.println("\n"+obj.getType());
                if(obj instanceof ParsedObject){
                    if(obj.getType()==Type.SIMPLESENTENCE){
                        
                    
                        System.out.print(obj.toString());
                    }else{

                            System.out.println(((ParsedObject)obj).toSimpleString());
                            i=((ParsedObject) obj).getChildren().iterator();
                            iters.add(i);
                    }
                }
                
                
            }
            if(iters.size()==1){
                iters.remove(iters.size()-1);
            }else if((iters.size()-1)>0){
                iters.remove(iters.size()-1);
                i=iters.get(iters.size()-1);
            }
        }
        
    }*/
}
