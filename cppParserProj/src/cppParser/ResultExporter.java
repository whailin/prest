

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
    //Includes LOC metrics, (structs and unions?)
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
    
    //Known members of the namespace
    public void exportNamespaces() throws IOException{
        writer=new BufferedWriter(new FileWriter(outputDir+"Namespaces.csv"));
        writeNamespaces(writer);
        writer.close();
    }
    
    //OO Metrics
    public void exportClassMetrics() throws IOException{
        writer=new BufferedWriter(new FileWriter(outputDir+"ClassMetrics.csv"));
        writeClassMetrics(writer);
        writer.close();
        
    }
    
    private void writeLOCMetrics(BufferedWriter writer) throws IOException{
        LOCMetrics projectLevelMetrics=new LOCMetrics();
        List<LOCMetrics> list=ParsedObjectManager.getInstance().getLocMetrics();
        writer.write("filename"+separator+
                "physicalLOC"+separator+
                "executableLOC"+separator+
                "emptyLines"+separator+
                "commentOnlyLines"+separator+
                "commentedCodeLines"+separator+
                "commentLinesTotal");
        for(LOCMetrics l:list){            writer.write("\n"); 
			writer.write("\""+l.file+"\""+separator + (l.codeOnlyLines+l.commentedCodeLines) + ","
                    +l.logicalLOC+separator
                    +l.emptyLines+separator
                    +l.commentLines + separator
                    +l.commentedCodeLines + separator
                    +(l.commentLines+l.commentedCodeLines));
            projectLevelMetrics.codeOnlyLines+=l.codeOnlyLines;
            projectLevelMetrics.commentLines+=l.commentLines;
            projectLevelMetrics.commentedCodeLines+=l.commentedCodeLines;
            projectLevelMetrics.emptyLines+=l.emptyLines;
            projectLevelMetrics.logicalLOC+=l.logicalLOC;
        }
        writer.write("\n");
        writer.write("\n");
        writer.write("projectPhysicalLOC"+separator+"projectExecutableLOC"+separator+"projectEmptyLines"+separator+"projectCommentOnlyLines"+separator+"projectCommentedCodeLines"+separator+"projectCommentLinesTotal");
        writer.write("\n");
        writer.write((projectLevelMetrics.codeOnlyLines+projectLevelMetrics.commentedCodeLines) + separator
                    +projectLevelMetrics.logicalLOC+separator
                    +projectLevelMetrics.emptyLines+separator
                    +projectLevelMetrics.commentLines + separator
                    +projectLevelMetrics.commentedCodeLines + separator
                    +(projectLevelMetrics.commentLines+projectLevelMetrics.commentedCodeLines));
    }

    private void writeClassMetrics(BufferedWriter writer) throws IOException{
        writer.write("className"+separator+"directParents"+separator+"children"+separator+"numberOfChildren"+separator+"depthOfInheritance"+separator+"weightedMethodsPerClass");
        String parents, children;
        for(CppScope cc : ParsedObjectManager.getInstance().getScopes()){
            if(cc instanceof CppClass){
                
                CppClass c=(CppClass) cc;
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
                writer.write(c.getName()+separator+
                        parents+separator+
                        children+separator+
                        c.children.size()+separator+
                        c.getDepthOfInheritance()+separator+
                        c.getFunctions().size()
                        );
                
            }
        }
                
    }

    private void writeNamespaces(BufferedWriter writer) throws IOException{
        String parameters="";
        for(CppScope scope:ParsedObjectManager.getInstance().getScopes()){
            if(scope.type==CppScope.NAMESPACE){
                writer.write("name"+separator+"numberOfVariables"+separator+"numberOfFunctions");
                writer.write("\n");
                writer.write(scope.name+separator+
                        scope.getMembers().size()+separator+
                        scope.getFunctions().size()
                        );
                writer.write("\n");
                writer.write("variablesType"+separator+"variableName");
                writer.write("\n");
                for(MemberVariable var:scope.getMembers())
                    writer.write(var.getType()+separator+var.getPointer()+var.getName());
                writer.write("\n");
                writer.write("returnType"+separator+"functionName"+separator+"parameters");
                writer.write("\n");
                for(CppFunc func:scope.getFunctions()){
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
                writer.write("\n");
            }
        }
    }

    private void writeFunctionMetrics(BufferedWriter writer) throws IOException{
        String parameters;
        writer.write(
                "returnType"+separator+
                "functionName"+separator+
                "parameters"+separator+
                "operatorCount"+separator+
                "operandCount"+separator+
                "uniqueOperatorCount"+separator+
                "uniqueOperandCount"+separator+
                "vocabulary"+separator+
                "length"+separator+
                "volume"+separator+
                "difficulty"+separator+
                "effort"+separator+
                "programmingTime"+separator+
                "deliverBugs"+separator+
                "level"+separator+
                "intelligentContent"+separator+
                "cyclomaticComplexity"+
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
                            func.getType()+separator+
                            func.getName()+separator+
                            parameters+separator+
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