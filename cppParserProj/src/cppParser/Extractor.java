package cppParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

import cppStructures.CppClass;
import cppStructures.CppFunc;
import cppStructures.CppFuncParam;
import cppStructures.CppNamespace;
import cppStructures.CppScope;
import cppStructures.MemberVariable;

/**
 * Extractor.java
 * Provides lexical analysis and metrics extraction of C++ source and header files.
 * 
 * @author Harri Pellikka
 */
public class Extractor
{
	// Filename or folder to process
	private String file = "";
	
	// File that is currently being processed
	public static String currentFile = "";
	
	// Current class stack under processing
	private Stack<CppScope> cppScopeStack = new Stack<CppScope>();
	
	// If 'true', all "std"-starting stuff is ignored
	// private boolean ignoreStd = true;

	// Braces count when the current function was found
	// private int funcBraceCount = 0;
	
	// Currently open braces count
	// private int braceCount = 0;
	
	// Current line in the source file (may not reflect the actual processing)
	public static int lineno = 0; 
	
	public int loc = 0;
	public int lloc = 0;
	public int ploc = 0;
	public long cmtLineNo = 0;		//comment lines	
	
	// Reference to the singleton parsed object manager
	private ParsedObjectManager objManager;
	
	// The sentence analyzer used to analyze each "raw" sentence
	private SentenceAnalyzer sentenceAnalyzer;
	
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
	 * Starts processing the files
	 */
	public void process()
	{
		Log.d("Processing started.");
		Log.d("Finding files and sorting... ");
		
		long startTime = System.currentTimeMillis();
		sentenceAnalyzer = new SentenceAnalyzer();
		
		FileLoader fileLoader = new FileLoader(this.file);
		
		Log.d("done in: " + (double)(System.currentTimeMillis() - startTime) / 1000.0 + " s.");
		Log.d("Found " + fileLoader.getFiles().size() + " files.");
		
		// Loop through the found files
		for(String s : fileLoader.getFiles())
		{
			Log.d("Processing " + s + " ...");
			process(s);
			Log.d();
		}
		
		// TODO Second pass: fix unknown references / types / ambiguities
		
		Log.d("Processing done. Dumping...");
		
		// Dump tree results to a file
		dumpTreeResults();
		
		Log.d("Dump done.");
		
		// printTreeResults();
		// printResults();
		
		long duration = System.currentTimeMillis() - startTime;
		
		Log.d("Processing took " + duration / 1000.0 + " s.");
	}
	
	/**
	 * Processes the given file.
	 * Processing includes tasks such as constructing internal format lines,
	 * tokenizing them and creating a structure tree of the found data.
	 */
	private void process(String file)
	{
		currentFile = file;
		// currentFunc = null;
		// currentScope = null;
		objManager.currentFunc = null;
		ParsedObjectManager.getInstance().currentScope = null;
		// objManager.setDefaultScope();
		
		cppScopeStack.clear();
		// braceCount = 0;
		// funcBraceCount = 0;
		// this.lineDone = false;
		lineno = 0;
		
		Log.d("Analyzing file: " + file);
		
		if(file.endsWith("OgreMaterialSerializer.cpp"))
		{
			Log.d("dbg start");
		}
		
		try
		{
			lineno = 1;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			String commentLine = "";
			boolean skipComment = false;
			boolean isMultiLineComment = false;
			char c;
			boolean stringOpen = false;
			
			while((c = (char)reader.read()) != (char)-1)
			{
				if(c == '\n')
				{
					loc++;
					lineno++;
					
					// Preprocess lines should be processed on endline
					if(!stringOpen && line.startsWith("#"))
					{
						sentenceAnalyzer.lexLine(line);
						line = "";
						commentLine = "";
					}
				}
				
				// Skips characters until the current comment ends
				if(skipComment)
				{
					if(!isMultiLineComment)
					{
						if(c == '\r' || c == '\n')
						{
							skipComment = false;
							objManager.oneLineComments.add(commentLine);
							commentLine = "";
						}
					}
					else if(isMultiLineComment)
					{
						if(commentLine.endsWith("*/"))
						{
							skipComment = false;
							isMultiLineComment = false;
							objManager.multiLineComments.add(commentLine);
							commentLine = "";
						}
					}
					
					if(skipComment) commentLine += c;
					
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
								Log.d("Found a single-line comment inside a string.");
								commentLine = "";
							}
						}
							
						if(commentLine.startsWith("/*"))
						{
							if(StringTools.getQuoteCount(line) % 2 == 0)
							{
								isMultiLineComment = true;
								skipComment = true;
							}
							else
							{
								Log.d("Found a multi-line comment inside a string.");
								commentLine = "";
							}
						}
						cmtLineNo++;
					}					
					continue;
				}
				
				// Add a character to the "line"
				if(c != '\r' && c != '\n' && c != '\t')
				{
					if(c == '"')
					{
						line += "\"";
						stringOpen = !stringOpen;
					}
					else
					{
						line += c;
					}
					
				}
				else if(line.length() > 0 && line.charAt(line.length() - 1) != ' ')
				{
					// Add a space (just one, even if there's multiple)
					line += ' ';
				}
				
				// If the line ends, start lexing it
				if(!stringOpen &&  (c == ';' || c == '{' || c == '}'))
				{
					lloc++;
					// lexLine(line);
					sentenceAnalyzer.lexLine(line);
					// sentenceAnalyzer.lexLineHM(line);
					line = "";
					commentLine = "";
				}
			}
			
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
	 * Dumps the results into a text file
	 */
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
				}
				
				// Dump variables
				writer.write("  VARIABLES\n");
				for(MemberVariable mv : cc.getMembers())
				{
					writer.write("    " + mv.getType() + " | " + mv.getName() + "\n");
				}
				
				writer.write("\n");
			}
			
			writer.write("\n");
			writer.write("Total amount of lines: " + loc + "\n");
			writer.write("Logical lines of code: " + lloc + "\n");
			writer.write("Physical lines of code: " + ploc + "\n");
			
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Prints the results
	 */
	/*
	private void printTreeResults()
	{
		Log.d("Tree results");
		for(CppScope cs : objManager.getScopes())
		{
			Log.d(" - " + cs.getName());
			for(CppFunc cp : cs.getFunctions())
			{
				Log.d("    - " + cp.getType() + " | " + cp.getName());
			}
		}
	}
	*/
	
	/**
	 * Prints information out to console
	 */
	/*
	private void printResults()
	{
		// Print #defines
		if(objManager.defines.size() > 0)
		{
			Log.d("defines");
			for(String s : objManager.defines) Log.d(" - " + s);
			Log.d();
		}
		
		// Print #includes
		if(objManager.includes.size() > 0)
		{
			Log.d("includes");
			for(String s : objManager.includes) Log.d(" - " + s);
			Log.d();
		}
		
		// Print class names
		if(objManager.classes.size() > 0)
		{
			Log.d("classes");
			for(String s : objManager.classes)	Log.d(" - " + s);
			Log.d();
		}
		
		// Print single-line comments
		if(objManager.oneLineComments.size() > 0)
		{
			Log.d("oneline comments");
			for(String s : objManager.oneLineComments)	Log.d(" - " + s);
			Log.d();
		}
		
		// Print multi-line comments
		if(objManager.multiLineComments.size() > 0)
		{
			Log.d("multiline comments");
			for(String s : objManager.multiLineComments) Log.d(" - " + s);
			Log.d();
		}
	}
	*/
}
