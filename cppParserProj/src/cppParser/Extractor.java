package cppParser;

import cppMetrics.LOCMetrics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import cppParser.utils.Log;
import cppParser.utils.MacroExpander;
import cppParser.utils.StringTools;
import cppStructures.CppClass;
import cppStructures.CppDefine;
import cppStructures.CppFile;
import cppStructures.CppFunc;
import cppStructures.CppNamespace;
import cppStructures.CppScope;
import cppStructures.MemberVariable;
import java.util.List;

/**
 * Extractor.java
 * Provides lexical analysis and metrics extraction of C++ source and header files.
 * 
 * @author Harri Pellikka
 */
public class Extractor
{
	enum Pass
	{
		PREPASS,
		MAINPASS,
		POSTPASS
	}
	
	enum Mode
	{
		PREPASS_ONLY,
		MAINPASS_ONLY,
		ALL_PASSES
	}
	
	private Pass currentPass = Pass.PREPASS;
	private Mode currentMode = Mode.ALL_PASSES;
	
	// Filename or folder to process
	private String file = "";
    //Directory for the output files;
    private String outputDir="";
	
	// File that is currently being processed
	public static String currentFile = "";
	
	// Current class stack under processing
	private Stack<CppScope> cppScopeStack = new Stack<CppScope>();
	
	// If 'true', all "std"-starting stuff is ignored
	// private boolean ignoreStd = true;
	
	// Current line in the source file (may not reflect the actual processing)
	public static int lineno = 0; 
	public int loc = 0;
	public int lloc = 0;
	public int ploc = 0;
	public int codeLines=0;
	public int emptyLines = 0;
    public int commentedCodeLines = 0;
	public int commentOnlyLines = 0;		//comment lines	
	
	
	// Reference to the singleton parsed object manager
	private ParsedObjectManager objManager;
	
	// The sentence analyzer used to analyze each "raw" sentence
	private SentenceAnalyzer sentenceAnalyzer;
	
	private PreprocessorPass prepassAnalyzer;
    private LOCMetrics locM;
    
	/**
	 * Constructor
	 * @param file Single file or a folder to process
	 */
	public Extractor(String file)
	{
		this.file = file;
		objManager = ParsedObjectManager.getInstance();
	}
    
    /**
	 * Constructor
	 * @param file Single file or a folder to process
     * @param outputDir Directory where metrics are stored
	 */
	public Extractor(String file, String outputDir)
	{
		this.file = file;
        this.outputDir=outputDir;
		objManager = ParsedObjectManager.getInstance();
	}
	
	/**
	 * Starts processing the files
	 */
	public void process()
	{
		Log.d("Processing started.");
		StringTools.setup();
		Log.d("Finding files and sorting... ");
		
		long startTime = System.currentTimeMillis();
		sentenceAnalyzer = new SentenceAnalyzer();
		prepassAnalyzer = new PreprocessorPass(this);
		
		FileLoader fileLoader = new FileLoader(this.file);
		
		Log.d("Files sorted in " + (double)(System.currentTimeMillis() - startTime) / 1000.0 + " s.");
		Log.d("Found " + fileLoader.getFiles().size() + " files.");
		
		// Execute the pre-pass if needed
		if(currentMode != Mode.MAINPASS_ONLY)
		{
			currentPass = Pass.PREPASS;
			
			// Parse preprocessor directives
			for(String s : fileLoader.getFiles())
			{
				CppFile cf = new CppFile(s);
				ParsedObjectManager.getInstance().addFile(cf);
				ParsedObjectManager.getInstance().setCurrentFile(cf);
				process(s);
			}
			
			// Expand #include paths
			for(CppFile cf : ParsedObjectManager.getInstance().getFiles())
			{
				cf.expandIncludes();
			}
			
			ParsedObjectManager.getInstance().setCurrentFile("");
			
			// Dump the #include tree for debuggin purposes
			dumpIncludeTree();
			
			// Calculate the pre-pass execution time
			long prepassDuration = System.currentTimeMillis() - startTime;
			Log.d("Found " + PreprocessorPass.defineCount + " #defines.");
			Log.d("Prepass done. (" + (double)(prepassDuration / 1000.0) + " s.)");
		}
		
		// Execute the main pass if needed
		if(currentMode != Mode.PREPASS_ONLY)
		{
			currentPass = Pass.MAINPASS;
			
			// Loop through the found files
			for(String s : fileLoader.getFiles())
			{
				ParsedObjectManager.getInstance().setCurrentFile(s);
                locM=new LOCMetrics();
                ParsedObjectManager.getInstance().addLocMetric(locM);
                sentenceAnalyzer.fileChanged(s, locM);
                
				process(s);
			}
			
			sentenceAnalyzer.lastFileProcessed();
			Log.d("Main pass done.");
		}
		
		// TODO Second pass: fix unknown references / types / ambiguities
        // Verify that no macro calls are in the operands
        // verifyToFile();
		
		// Debug dump
		// TODO REMOVE WHEN DONE
		dumpFunctions();
		dumpScopes();
		
		// Dump tree results to a file
		ResultExporter exp = new ResultExporter(outputDir);
        exp.exportAll();
		
		Log.d("Dump done.");
		
		// printTreeResults();
		// printResults();
		
		long duration = System.currentTimeMillis() - startTime;
		
		Log.d("Processing took " + duration / 1000.0 + " s.");
	}
	
	private void verifyToFile()
	{
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("verifydump.txt"));
			
			ArrayList<CppDefine> defines = new ArrayList<CppDefine>();
			for(CppFile cf : ParsedObjectManager.getInstance().getFiles())
			{
				defines.addAll(cf.getDefines());
			}
			
			for(CppScope cs : ParsedObjectManager.getInstance().getScopes())
			{
				for(CppFunc cf : cs.getFunctions())
				{
					for(String s : cf.getOperands())
					{
						if(MacroExpander.containsDefinition(s))
						{
							CppDefine cd = MacroExpander.getDefinition(s);
							Log.d("**** FOUND MACRO CALL -> " + s + " FILE: " + cf.fileOfFunc);
							writer.write("File: " + cf.fileOfFunc + " Function: " + cf.getName() + ": Macro call -> " + s + " Macro source: " + cd.getFile() + "\n");
						}
					}
				}
			}
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	long startTime = 0;
	
	private void process(String file)
	{
		currentFile = file;
		Log.d("Processing: " + file);
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			char c;
			String line = "";
			int lineno = 1;
			int rawExpandStartIndex = 0;
			boolean stringOpen = false, charOpen = false;
			boolean codeFound = false, commentFound = false;
			
			while((c = (char)reader.read()) != (char)-1)
			{
				// Handle spaces, carriage returns and tabs
				if(c == '\r') continue;
				if(c == ' ' && line.endsWith(" ")) continue;
				if(c == '\t')
				{
					if(line.length() > 0) line += " ";
					continue;
				}
				
				// Add the current char to the line
				if(c == '\\') line += "\\"; 
				else line += c;
				
				// Skip "empty" whitespaces
				if(line.equals("\n") || line.equals("\t") || line.equals(" "))
				{
					line = "";
					rawExpandStartIndex = 0;
					addLine(false, false);
					// Log.d("Line " + lineno);
					// lineno++;
					continue;
				}
				
				// Toggle string and char literals on / off
				if(c == '"' && !charOpen && !line.endsWith("\\\"")) stringOpen = !stringOpen;
				if(c == '\'' && !stringOpen && !line.endsWith("\\\'")) charOpen = !charOpen;
				
				if(!stringOpen && !charOpen)
				{
					if(c == '\n')
					{
						lineno++;
						if(!line.trim().isEmpty()) addLine(codeFound, commentFound);
					}
					
					// Handle single-line comments
					if(line.endsWith("//"))
					{
						processSingleLineComment(line, reader);
						
						if(!line.startsWith("//")) addLine(true, true);
						else addLine(false, true);
						
						line = "";
						rawExpandStartIndex = 0;
						continue;
					}
					
					// Handle multi-line comments
					if(line.endsWith("/*"))
					{
						processMultiLineComment(line.substring(line.indexOf("/*")), reader);
						if(!line.startsWith("/*"))
						{
							line = line.substring(0, line.indexOf("/*"));
							addLine(true, true);
						}
						else
						{
							addLine(false, true);
							line = "";
							rawExpandStartIndex = 0;
						}
						continue;
					}
					
					// Handle newline
					if(line.endsWith("\n"))
					{
						// Handle preprocessor directives
						if(line.startsWith("#"))
						{
							if(line.endsWith("\\\n"))
							{
								line = line.substring(0, line.length() - 2);
							}
							else
							{
								// Log.d("Found #: " + line);
								if(currentPass == Pass.PREPASS)
								{
									line = line.substring(0, line.length() - 1);
									prepassAnalyzer.process(line.trim());
								}
								line = "";
							}
							
							continue;
						}
						else
						{
							line = line.substring(0, line.length() - 1);
							line += " ";
						}
						addLine(true, false);
					}
					
					// Expand macros
					if(currentPass == Pass.MAINPASS)
					{
						if(!line.startsWith("#") && MacroExpander.shouldExpandRaw(c))
						{
							String beginLine = line.substring(0, rawExpandStartIndex);
							String expandable = line.substring(rawExpandStartIndex);
							expandable = MacroExpander.expandRaw(expandable);
							line = beginLine + " " + expandable;
							if(line.trim().endsWith(";")) line = line.trim();
							rawExpandStartIndex = line.length() - 1;
						}
					}
					
					// Handle end-of-sentence
					if(!line.startsWith("#"))
					{
						if(line.endsWith(";") || line.endsWith("{") || line.endsWith("}") || isVisibilityStatement(c, line))
						{
							// Log.d("Found sentence: " + line);
							if(currentPass == Pass.MAINPASS)
							{
								sentenceAnalyzer.lexLine(line.trim());
							}
							addLine(true, false);
							line = "";
							rawExpandStartIndex = 0;
							continue;
						}

					}
				}
			}
			
			resetLOCCounter();
			loc++;
			
			reader.close();
		}
		catch(Exception e)
		{
			
		}
	}
	
	private void processSingleLineComment(String line, BufferedReader reader) throws IOException
	{
		char c;
		while((c = (char)reader.read()) != '\n' && c != '\r' && c != (char)-1) line += c;
		// Log.d("Found single-line comment: " + line);
	}
	
	private void processMultiLineComment(String line, BufferedReader reader) throws IOException
	{
		char c;
		while(!line.endsWith("*/"))
		{
			c = (char)reader.read();
			line += c;
		}
		// Log.d("Found multi-line comment: " + line);
	}
	
	/**
	 * Processes the given file.
	 * Processing includes tasks such as constructing internal format lines,
	 * tokenizing them and creating a structure tree of the found data.
	 */
	private void process2(String file)
	{
		if(startTime > 0) Log.d("Duration: " + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		
		currentFile = file;
		Log.d("File: " + currentFile);
		objManager.currentFunc = null;
		ParsedObjectManager.getInstance().currentScope = null;
		
		cppScopeStack.clear();
		lineno = 0;
		
		try
		{
			lineno = 1;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			String commentLine = "";
			boolean commentFound=false; //Currently read line contains comments
            boolean codeFound=false;  //Currently read line contains code
			boolean skipComment = false;
			boolean isMultiLineComment = false;
			char c;
			boolean stringOpen = false;
			boolean charOpen = false;
			String currentToken = ""; // A 'token' string since the last whitespace char
			
			while((c = (char)reader.read()) != (char)-1)
			{
				// Token split and macro expansion
				if(currentPass == Pass.MAINPASS && !skipComment && line.length() > 0 && !stringOpen && !charOpen)
				{
					if(c == '\n' || c == ' ' || c == '\t' || c == ')')
					{
						/*
						if(!line.startsWith("#"))
						{
							currentToken = (line + c).trim();
							
							// String[] expanded = (new MacroExpander()).expand(currentToken);
							String[] expanded = (new MacroExpander()).expand(currentToken);
							line = "";
							for(String s : expanded) line += (line.length() > 0 ? " " : "") + s;
							while(line.endsWith(" ")) line = line.substring(0, line.length() - 1);
							while(line.startsWith(" ")) line = line.substring(1, line.length());
							c = ' ';
							
							// Expansion caused the sentence to end

							if(!stringOpen && !charOpen && (line.endsWith(";") || line.endsWith("{") || line.endsWith("}") || (isVisibilityStatement(c, line))))
							{
								lloc++;
								// lexLine(line);
								line.trim();
								
								if(!line.startsWith("#")) sentenceAnalyzer.lexLine(line);

								line = "";
								commentLine = "";
			                    codeFound=true;
			                    continue;
							}
							
						}
						*/
						currentToken = "";
					}
				}
				
				if(c == '\n')
				{
					loc++;
					lineno++;
					ploc++;
                    if(!line.trim().isEmpty())
                    {
                        codeFound=true;
                    }
                    
					addLine(codeFound,commentFound);
					
					// Handle preprocessor directives
					if(line.startsWith("#"))
					{
						line = line.trim();
						if(line.charAt(line.length() - 1) != '\\')
						{
							lloc++;
							switch(currentPass)
							{
							case PREPASS:
								prepassAnalyzer.process(line);
								break;
							}
							
							line = "";
							commentLine = "";
							
						}
						else
						{
							lloc++;
							line = line.substring(0, line.length() - 1);
							continue;
						}
					}
					else if(line.equals(";"))
					{
						line = "";
					}
					commentFound = false;
                    codeFound = false;
				}
				
				// Skips characters until the current comment ends
				if(skipComment)
				{
					if(!isMultiLineComment)
					{
						if(c == '\n')
						{
							skipComment = false;
							objManager.oneLineComments.add(commentLine);
							commentLine = "";
						}
					}
					else if(isMultiLineComment)
					{
						if(commentLine.endsWith("*") && c == '/')
						{
							skipComment = false;
							isMultiLineComment = false;
							objManager.multiLineComments.add(commentLine);
							commentLine = "";
						}
					}
					
					if(skipComment)
					{ 
						if(c != ' ' && c != '\n' && c != '\t')
						{
							commentFound=true;
						}
						
						commentLine += c;
					}					
					continue;
				}
				
				// Check if a comment line is about to start
				if(c == '/' || (c == '*' && commentLine.startsWith("/")))
				{
					commentLine += c;
					if(commentLine.length() > 1)
					{
						if(commentLine.startsWith("//"))
						{
							if(StringTools.getQuoteCount(line) % 2 == 0)
							{
								// Skip until new line
								isMultiLineComment = false;
								skipComment = true;
							}
							else
							{
								commentLine = "";
							}
						}
						else if(commentLine.startsWith("/*"))
						{
							if(StringTools.getQuoteCount(line) % 2 == 0)
							{
								isMultiLineComment = true;
								skipComment = true;
							}
							else
							{
								commentLine = "";
							}
						}
					}					
					continue;
				}
				else
				{
                    if(commentLine.length() == 1)
                    {
                        commentLine="";
                        line += "/";
                    }
                }
				
				// Add a character to the "line"
				if(c != '\r' && c != '\n' && c != '\t')
				{
					
					if(c == '"' && ((line.length() > 0 ? line.charAt(line.length() - 1) != '\\' : true) || (line.length() > 1 ? line.charAt(line.length() - 2) == '\\' : true)) && !charOpen)
					{
						line += "\"";
						stringOpen = !stringOpen;
					}
					else if(c == '\'' && ((line.length() > 0 ? line.charAt(line.length() - 1) != '\\' : true) || (line.length() > 1 ? line.charAt(line.length() - 2) == '\\' : true)) && !stringOpen)
					{
						line += "\'";
						charOpen = !charOpen;
					}
					else
					{
						if(line.length() == 0 && c == ' ')
						{
							
						}
						else
						{
							line += c;
							currentToken += c;
						}
					}
								
				}
				else if(line.length() > 0 && line.charAt(line.length() - 1) != ' ' && c != '\n')
				{
					// Add a space (just one, even if there's multiple)
					line += ' ';					
				}
				
				switch(currentPass)
				{
				case MAINPASS:
					// If the line ends, start lexing it
					// if(!stringOpen && !charOpen && (c == ';' || c == '{' || c == '}' || (isVisibilityStatement(c, line))))
					if(!stringOpen && !charOpen && (line.endsWith(";") || line.endsWith("{") || line.endsWith("}") || (isVisibilityStatement(c, line))))
					{
						lloc++;
						// lexLine(line);
						line.trim();
						
						if(!line.startsWith("#")) sentenceAnalyzer.lexLine(line);

						line = "";
						commentLine = "";
	                    codeFound=true;
					}
					break;
				case PREPASS:
					if(!stringOpen && !charOpen && (c == ';' || c == '{' || c == '}'))
					{
						if(line.startsWith("#"))
						{
							prepassAnalyzer.process(line);
						}
						lloc++;
						line = "";
						commentLine = "";
						codeFound = true;
					}
					break;
				}
			}
			
			addLine(codeFound,commentFound);
            resetLOCCounter();
			loc++;
			
			// Finally, close the reader
			reader.close();
			
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * This method counts line for physical loc metrics
	 * @param codeFound should be true if current line contains code
	 * @param commentFound should be true if current line contains comments
	 */
	private void addLine(boolean codeFound, boolean commentFound)
	{
        if(currentPass == Pass.MAINPASS)
        {
			if(codeFound)
			{
	            if(commentFound) commentedCodeLines++;
				else codeLines++;
			}
			else
			{
				if(commentFound) commentOnlyLines++;
				else emptyLines++;
			}
        }
	}
	
    private void resetLOCCounter()
    {
        if(currentPass == Pass.MAINPASS)
        {
        	locM.codeOnlyLines = codeLines;
        	locM.emptyLines = emptyLines;
        	locM.commentLines = commentOnlyLines;
        	locM.commentedCodeLines = commentedCodeLines;
        
        	codeLines = 0;
        	emptyLines = 0;
        	commentOnlyLines = 0;
        	commentedCodeLines = 0;
        }
    }
	
	/**
	 * Checks whether or not the line forms a 'visibility statement' found usually in headers
	 * @param c The latest char to add
	 * @param line The line formed so far
	 * @return 'true' if the line forms either "public", "protected" or "private" statement, 'false' otherwise
	 */
	private boolean isVisibilityStatement(char c, String line)
	{
		if(c != ':') return false;
		else
		{
			if(line.endsWith("public:") || line.endsWith("protected:") || line.endsWith("private:")) return true;
		}
		return false;
	}
	
	private void dumpIncludeTree()
	{
		BufferedWriter writer;
		try
		{
			writer = new BufferedWriter(new FileWriter("includetree.txt"));
			
			Log.d("Dumping include tree...");
			for(CppFile cf : ParsedObjectManager.getInstance().getFiles())
			{
				cf.dump(writer, new HashSet<CppFile>(), 0);
				writer.write("\n");
			}
			
			writer.close();
		}
		catch(Exception e)
		{
			
		}
	}
	
	/**
	 * Dumps functions into a text file
	 */
	private void dumpFunctions()
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("dump_functions.txt"));
			
			for(CppScope scope : ParsedObjectManager.getInstance().getScopes())
			{
				for(CppFunc func : scope.getFunctions())
				{
					writer.write(func.getType() + " " + func.getName() + "()" + " | " + func.fileOfFunc + "\n");
					writer.write("------------------------\n");
					writer.write("Operands:\n");
					for(String s : func.getOperands())
					{
						writer.write("    " + s + "\n");
					}
					writer.write("Operators:\n");
					for(String s : func.getOperators())
					{
						writer.write("    " + s + "\n");
					}
					writer.write("\n\n");
				}
				
			}
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Dumps namespaces into a text file
	 */
	private void dumpScopes()
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("dump_namespaces.txt"));
			
			writer.write("NAMESPACES\n**************\n");
			for(CppScope scope : ParsedObjectManager.getInstance().getScopes())
			{
				if(scope instanceof CppNamespace)
				{
					CppNamespace namespace = (CppNamespace)scope;
					writer.write(namespace.getName() + "\n");
					
					if(namespace.parents.size() > 0)
						{
						writer.write("Parents:\n");
						for(CppScope parent : namespace.parents)
						{
							writer.write("   " + parent.getName() + "\n");
						}
					}
					
					if(namespace.children.size() > 0)
					{
						writer.write("Children:\n");
						for(CppScope child : namespace.children)
						{
							writer.write("   " + child.getName() + "\n");
						}
					}
					writer.write("\n");
				}
				
			}
			writer.write("\n\n");
			
			writer.write("CLASSES\n**************\n");
			for(CppScope scope : ParsedObjectManager.getInstance().getScopes())
			{
				if(scope instanceof CppClass)
				{
					CppClass cppClass = (CppClass)scope;
					writer.write(cppClass.getName() + "(" + (cppClass.namespace != null ? cppClass.namespace.getName() : "__MAIN__") + ")" + "\n");
					
					
					if(cppClass.parents.size() > 0)
					{
						writer.write("Parents:\n");
						for(CppScope parent : cppClass.parents)
						{
							writer.write("   " + parent.getName() + "(" + (parent.namespace != null ? parent.namespace.getName() : "__MAIN__") + ")" + "\n");
						}
					}
					
					if(cppClass.children.size() > 0)
					{
						writer.write("Children:\n");
						for(CppScope child : cppClass.children)
						{
							writer.write("   " + child.getName() + "(" + (child.namespace != null ? child.namespace.getName() : "__MAIN__") + ")" + "\n");
						}
					}
					writer.write("\n");
				}
				
			}
			writer.write("\n\n");
			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
