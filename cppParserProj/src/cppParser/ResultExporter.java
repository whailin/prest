

package cppParser;

import cppMetrics.LOCMetrics;
import cppParser.utils.Log;
import cppStructures.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for reading the metric results from ParsedObjectManager and
 * exporting them to a file.
 * http://tools.ietf.org/html/rfc4180
 * @author Tomi
 */
public class ResultExporter {
    private static final String separator = ",";
    private String outputDir;
    private BufferedWriter writer;
    

    public ResultExporter(String outputDir){
        this.outputDir=outputDir;
        if(!outputDir.isEmpty()){
            char c=outputDir.charAt(outputDir.length()-1);
            if(c!='\\' || c!='/')
                this.outputDir+="\\";
            
        }
    }
    public void exportAll(){
        Log.d("Exporting to "+outputDir);
        try {
            exportFileMetrics();
            exportFunctionMetrics();
            exportNamespaces();
            exportClassMetrics();
        } catch (IOException ex) {
            Log.d("Error:" +ex.getMessage());
            
        }
    }
    //Includes LOC metrics for each file,
    public void exportFileMetrics()throws IOException{
        try {
            writer=new BufferedWriter(new FileWriter(outputDir+"FileMetrics.csv"));
            writeLOCMetrics(writer);
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(ResultExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //Includes functions/methods and their Halstead and complexity metrics
    public void exportFunctionMetrics() throws IOException{
        writer=new BufferedWriter(new FileWriter(outputDir+"FunctionMetrics.csv"));
        writeFunctionMetrics(writer);
        writer.close();
    }
    
    //Known namespaces
    public void exportNamespaces() throws IOException{
        writer=new BufferedWriter(new FileWriter(outputDir+"Namespaces.csv"));
        writeNamespaces(writer);
        writer.close();
    }
    
    //Classes and OO Metrics
    public void exportClassMetrics() throws IOException{
        writer=new BufferedWriter(new FileWriter(outputDir+"ClassMetrics.csv"));
        writeClassMetrics(writer);
        writer.close();
        
    }
    
    private void writeLOCMetrics(BufferedWriter writer) throws IOException{
        
        List<LOCMetrics> list=ParsedObjectManager.getInstance().getLocMetrics();
        writer.write("File name"+separator+
                "Physical LOC"+separator+
                "Executable LOC"+separator+
                "Empty Lines"+separator+
                //"commentOnlyLines"+separator+
                //"commentedCodeLines"+separator+
                "Comment Lines");
        for(LOCMetrics l:list){            
            writer.write("\n");
			writer.write("\""+l.file+"\""+separator 
                    +(l.codeOnlyLines+l.commentedCodeLines) + ","
                    +l.logicalLOC+separator
                    +l.emptyLines+separator
                    //+l.commentLines + separator
                    //+l.commentedCodeLines + separator
                    +(l.commentLines+l.commentedCodeLines));
        }
        writer.write("\n");
        
    }
    
    private LOCMetrics getProjectLevelLOCMetrics(){
        LOCMetrics projectLevelMetrics=new LOCMetrics();
        List<LOCMetrics> list=ParsedObjectManager.getInstance().getLocMetrics();
        for(LOCMetrics l:list){            
           
            projectLevelMetrics.codeOnlyLines+=l.codeOnlyLines;
            projectLevelMetrics.commentLines+=l.commentLines;
            projectLevelMetrics.commentedCodeLines+=l.commentedCodeLines;
            projectLevelMetrics.emptyLines+=l.emptyLines;
            projectLevelMetrics.logicalLOC+=l.logicalLOC;
        } 
        return projectLevelMetrics;
    }

    private void writeClassMetrics(BufferedWriter writer) throws IOException{
        writer.write(
                "File"+separator+
                "Namespace"+separator+
                "Class Name"+separator+
                "Direct parents"+separator+
                "Children"+separator+
                "Number of children"+separator+
                "Depth of Inheritance"+separator+
                "Weighted Methods per Class");
        String parents, children;
        for(CppScope cc : ParsedObjectManager.getInstance().getScopes()){
            if(cc instanceof CppClass){
                
                CppClass c=(CppClass) cc;
                String namespace="";
                CppNamespace ns=c.namespace;
                while (true){
                    if(ns!=null){
                        if(ns.name.contentEquals("__MAIN__")|| ns.name.isEmpty()) break;
                        else 
                            namespace=ns.name+"::"+namespace;
                        ns=ns.namespace;
                    }else 
                        break;
                    
                }
                if(c.parents.isEmpty())
                    parents="";
                else{
                    int x=0;
                    parents="\"";
                    
                    for (CppScope p:c.parents){
                        parents+=p.getName();
                        if(x<(c.parents.size()-1))
                            parents+=",";
                        x++;   
                    }
                    parents+="\"";
                }
                if(c.children.isEmpty())
                    children="";
                else{
                    int x=0;
                    children="\"";
                    for (CppScope p:c.children){
                            children+=p.getName();
                            if(x<(c.children.size()-1))
                                children+=",";
                            x++;    
                    }
                    children+="\"";
                
                }
                writer.write("\n");
                writer.write(
                        c.nameOfFile+separator+
                        namespace+separator+
                        c.getName()+separator+
                        parents+separator+
                        children+separator+
                        c.children.size()+separator+
                        c.getDepthOfInheritance()+separator+
                        c.getFunctions().size()
                        );
                
            }
        }
                
    }
    
    private void writeProjectLOC(BufferedWriter writer) throws IOException{
        LOCMetrics l=getProjectLevelLOCMetrics();
        writer.write(
                "Total Physical LOC"+separator+
                "Total Executable LOC"+separator+
                "Total Empty Lines"+separator+
                //"commentOnlyLines"+separator+
                //"commentedCodeLines"+separator+
                "Total Comment Lines");            
            writer.write("\n");
			writer.write("\""+l.file+"\""+separator 
                    +(l.codeOnlyLines+l.commentedCodeLines) + ","
                    +l.logicalLOC+separator
                    +l.emptyLines+separator
                    //+l.commentLines + separator
                    //+l.commentedCodeLines + separator
                    +(l.commentLines+l.commentedCodeLines));
        
    }
    private void writeNamespaces(BufferedWriter writer) throws IOException{
        String parameters="";
        writeProjectLOC(writer);
        writer.write(
                        //"File"+separator+
                        "\n"+
                        "Name"+separator+
                        "Number of Variables"+separator+
                        "Number of Functions");
                writer.write("\n");
        
        
        for(CppScope scope:ParsedObjectManager.getInstance().getScopes()){
            if(scope.type==CppScope.NAMESPACE){
                String name;
                if(scope.name.contains(","))
                    name="\""+scope.name+"\"";
                else
                    name=scope.name;
                writer.write(
                        //scope.nameOfFile+separator+
                        name+separator+
                        scope.getMembers().size()+separator+
                        scope.getFunctions().size()
                        );
                writer.write("\n");
                /*writer.write("variablesType"+separator+"variableName");
                writer.write("\n");
                for(MemberVariable var:scope.getMembers())
                    writer.write(var.getType()+separator+var.getPointer()+var.getName());
                writer.write("\n");
                writer.write("returnType"+separator+"functionName"+separator+"parameters");
                writer.write("\n");*/
                /*for(CppFunc func:scope.getFunctions()){
                    int i=0;
                    parameters="\"";
                    for(CppFuncParam param:func.parameters){
                        parameters+=param.type;
                        if(i<(func.parameters.size()-1))
                            parameters+=",";
                        i++;
                    }
                    parameters+="\"";
                    if(parameters.contentEquals("\"void\"") || parameters.contentEquals("\"\""))
                        parameters="";
                    writer.write(func.getType()+separator+
                            func.getName()+separator+
                            parameters
                            );
                    writer.write("\n");
                }
                writer.write("\n");*/
            }
        }
    }

    private void writeFunctionMetrics(BufferedWriter writer) throws IOException{
        String parameters;
        writer.write(
                "File"+separator+
                "Return type"+separator+
                "Function name"+separator+
                "Operator count"+separator+
                "Operand count"+separator+
                "Unique Operator Count"+separator+
                "Unique Operand Count"+separator+
                "Vocabulary"+separator+
                "Length"+separator+
                "Volume"+separator+
                "Difficulty"+separator+
                "Effort"+separator+
                "Programming time"+separator+
                "Deliver Bugs"+separator+
                "Level"+separator+
                "Intelligent content"+separator+
                "Cyclomatic Complexity"+
                "\n"
                );
        for(CppScope scope:ParsedObjectManager.getInstance().getScopes()){
        for(CppFunc func : scope.getFunctions())
				{
                    int i=0;
					parameters="\"";
                    for(CppFuncParam param:func.parameters){
                        parameters+=param.type;
                        if(i<(func.parameters.size()-1))
                            parameters+=",";
                        i++;
                    }
                    parameters+="\"";
                    if(parameters.contentEquals("\"void\"") || parameters.contentEquals("\"\""))
                        parameters="";		
					writer.write(
                            func.fileOfFunc+separator+
                            func.getType()+separator+
                            func.getName()+parameters+separator+
                            func.getOperatorCount()+separator+
                            func.getOperandCount()+separator+
                            func.getUniqueOperatorCount()+separator+
                            func.getUniqueOperandCount()+separator+
                            func.getVocabulary()+separator+
                            func.getLength()+separator+
                            func.getVolume()+separator+
                            func.getDifficulty()+separator+
                            func.getEffort()+separator+
                            func.getTimeToProgram()+separator+
                            func.getDeliveredBugs()+separator+
                            func.getLevel()+separator+
                            func.getIntContent()+separator+  
                            func.getCyclomaticComplexity()       
                            );
					writer.write("\n");
					
					
					
				}
        }
    }

    

}