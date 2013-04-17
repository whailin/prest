

package cppParser;

import cppMetrics.LOCMetrics;
import cppParser.utils.Log;
import cppStructures.CppClass;
import cppStructures.CppScope;
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
    private static final String separator=",";
    private String outputDir;
    private BufferedWriter writer;
    

    public ResultExporter(String outputDir){
        this.outputDir=outputDir;
        if(!outputDir.isEmpty()){
            char c=outputDir.charAt(outputDir.length()-1);
            if(c!='\\' || c!='/')
                this.outputDir+="\\";
        }
        this.outputDir=outputDir;
    }
    public void exportAll(){
        Log.d("Exporting");
        try {
            exportFileMetrics();
            //exportFunctionMetrics();
            //exportNamespaces();
            exportClassMetrics();
        } catch (IOException ex) {
            Logger.getLogger(ResultExporter.class.getName()).log(Level.SEVERE, null, ex);
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
        writer.close();
    }
    
    //Known members of the namespace
    public void exportNamespaces() throws IOException{
        writer=new BufferedWriter(new FileWriter(outputDir+"Namespaces.csv"));
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
        writer.write("filename,physicalLOC,executableLOC,emptyLines,commentOnlyLines,commentedCodeLines,commentLinesTotal");
        for(LOCMetrics l:list){
            // Log.d("Filename "+l.file);
            writer.write("\n"); 
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
        writer.write("projectPhysicalLOC,projectExecutableLOC,projectEmptyLines,projectCommentOnlyLines,projectCommentedCodeLines,projectCommentLinesTotal");
        writer.write("\n");
        writer.write((projectLevelMetrics.codeOnlyLines+projectLevelMetrics.commentedCodeLines) + separator
                    +projectLevelMetrics.logicalLOC+separator
                    +projectLevelMetrics.emptyLines+separator
                    +projectLevelMetrics.commentLines + separator
                    +projectLevelMetrics.commentedCodeLines + separator
                    +(projectLevelMetrics.commentLines+projectLevelMetrics.commentedCodeLines));
    }

    private void writeClassMetrics(BufferedWriter writer) throws IOException{
        writer.write("className,directParents,children,numberOfChildren,depthOfInheritance,weightedMethodsPerClass");
        String parents, children;
        for(CppScope cc : ParsedObjectManager.getInstance().getScopes()){
            if(cc instanceof CppClass){
                parents="\"";
                children="\"";
                CppClass c=(CppClass) cc;
                if(c.parents.size()==0)
                    parents="";
                else{
                    int x=0;
                    for (CppScope p:c.parents){
                        parents+=p.getName();
                        if(x<(c.parents.size()-1))
                            parents+="\"";
                        x++;   
                    }
                    parents="\"";
                }
                if(c.children.size()==0)
                    parents="";
                else{
                    int x=0;
                    for (CppScope p:c.children){
                        children+=p.getName();
                        if(x<(c.children.size()-1))
                            children+="\"";
                        x++;    
                    }
                    children+="\"";
                }
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

}
/*
private void dumpTreeResults()
	{
		BufferedWriter writer;
		try
		{
            
			writer = new BufferedWriter(new FileWriter("treedump.txt"));
			for(CppScope cc : objManager.getScopes())
			{
				if(cc instanceof CppNamespace)
				{
					writer.write("Namespace: ");
				}
				else if(cc instanceof CppClass)
				{
					writer.write("Class: ");
				}
				writer.write(cc.getName() + " (file: " + cc.nameOfFile + ")\n");
				for(CppScope cs : cc.children)
				{
					writer.write("  Parent of " + cs.getName() + "\n");
				}
				for(CppScope cs : cc.parents)
				{
					writer.write("  Child of " + cs.getName() + "\n");
				}
				
				if(cc instanceof CppClass)
				{
					writer.write("  Child count: " + cc.children.size() + "\n");
					writer.write("  Depth of inheritance: " + ((CppClass) cc).getDepthOfInheritance() + "\n");
					writer.write("  Weighted methods per class: " + cc.getFunctions().size() + "\n");
				}
				
				// Dump functions
				writer.write("  FUNCTIONS\n");
				for(CppFunc mf : cc.getFunctions())
				{
					writer.write("    " + mf.getType() + " | " + mf.getName() + " (");
					for(int i = 0; i < mf.parameters.size(); ++i)
					{
						writer.write(mf.parameters.get(i).type + " | " + mf.parameters.get(i).name);
						if(i < mf.parameters.size() - 1) writer.write(", ");
					}					

					writer.write(")\n");
					
					writer.write("      File: " + mf.fileOfFunc + "\n");
					
					writer.write("      Operator count = " + mf.getOperatorCount() + "\n");
					for(String s : mf.getOperators())
					{
						writer.write("        " + s + "\n");
					}
					
					writer.write("      Operand count = " + mf.getOperandCount() + "\n");
					for(String s : mf.getOperands())
					{
						writer.write("        " + s + "\n");
					}
					
					writer.write("      Unique Operator count = " + mf.getUniqueOperatorCount() + "\n");
					for(String s : mf.getUniqueOperators())
					{
						writer.write("        " + s + "\n");
					}
					
					writer.write("      Unique Operand count = " + mf.getUniqueOperandCount() + "\n");
					for(String s : mf.getUniqueOperands())
					{
						writer.write("        " + s + "\n");
					}
					
					writer.write("      Vocabulary = " + mf.getVocabulary() + "\n");
					writer.write("      Length = " + mf.getLength() + "\n");
					writer.write("      Volume = " + mf.getVolume() + "\n");
					writer.write("      Difficulty = " + mf.getDifficulty() + "\n");
					writer.write("      Effort = " + mf.getEffort() + "\n");
					writer.write("      Programming time = " + mf.getTimeToProgram() + "\n");
					writer.write("      Deliver bugs = " + mf.getDeliveredBugs() + "\n");
					writer.write("      Level = " + mf.getLevel() + "\n");
					writer.write("      Intelligent content = " + mf.getIntContent() + "\n");
					writer.write("      Cyclomatic complexity = " + mf.getCyclomaticComplexity() + "\n");
					writer.newLine();
					
				}
				
				// Dump variables
				writer.write("  VARIABLES\n");
				for(MemberVariable mv : cc.getMembers())
				{
					writer.write("    " + mv.getType() + " | " + mv.getName() + "\n");
				}
				
				writer.write("\n");
			}
			writeLOCmetrics(writer);			
						
			
			
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
            
		}
	}*/
    /*private void writeLOCmetrics(BufferedWriter writer) throws IOException{
        List<LOCMetrics> list=ParsedObjectManager.getInstance().getLocMetrics();
        for(LOCMetrics l:list){
            writer.write("\n");
            writer.write("LOC metrics for file: "+file);
			writer.write("Physical lines of code: " + (l.codeOnlyLines+l.commentedCodeLines) + "\n");
            writer.write("Logical lines of code: " + (l.logicalLOC) + "\n");
			writer.write("Empty lines:"+ l.emptyLines+ "\n");
            writer.write("Comment only lines: " +l.commentLines + "\n");
            writer.write("Commented code lines: " + l.commentedCodeLines + "\n");
			writer.write("Total comment lines: " + (l.commentLines+commentedCodeLines) + "\n");
	
            
        }
    }*/


/*
 * File: LOCMetrics
 * Function: Halstead & complexity
 * Namespace: known namespace member functions/variables
 * Class: OO metrics
 */