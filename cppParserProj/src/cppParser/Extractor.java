package cppParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import cppStructures.CppClass;
import cppStructures.CppFunc;
import cppStructures.CppNamespace;
import cppStructures.CppScope;

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
	private boolean ignoreStd = true;

	// Braces count when the current function was found
	private int funcBraceCount = 0;
	
	// Currently open braces count
	private int braceCount = 0;
	
	// Current line in the source file (may not reflect the actual processing)
	public static int lineno = 0; 
	
	// The sentence analyzer used to analyze each "raw" sentence
	private SentenceAnalyzer sentenceAnalyzer;
	
	/**
	 * Constructor
	 * @param file Single file or a folder to process
	 */
	public Extractor(String file)
	{
		this.file = file;
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
		}
		
		Log.d("Processing done. Dumping...");
		
		// Dump tree results to a file
		dumpTreeResults();
		
		Log.d("Dump done.");
		
		printResults();
		
		long duration = System.currentTimeMillis() - startTime;
		
		Log.d("Processing took " + duration / 1000.0 + " s.");
		System.out.println("Processing took " + duration / 1000.0 + " s.");
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
		ParsedObjectManager.getInstance().currentFunc = null;
		ParsedObjectManager.getInstance().currentScope = null;
		
		cppScopeStack.clear();
		braceCount = 0;
		funcBraceCount = 0;
		// this.lineDone = false;
		this.lineno = 0;
		
		try
		{
			lineno = 1;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "";
			String commentLine = "";
			boolean skipToNewLine = false;
			boolean isMultiLineComment = false;
			char c;
			while((c = (char)reader.read()) != (char)-1)
			{
				// Simply increase the line number
				if(c == '\n') lineno++;
				
				// Skips characters until the current comment ends
				if(skipToNewLine)
				{
					if(!isMultiLineComment)
					{
						if(c == '\r' || c == '\n')
						{
							skipToNewLine = false;
							ParsedObjectManager.getInstance().oneLineComments.add(commentLine);
							commentLine = "";
						}
					}
					else if(isMultiLineComment)
					{
						if(commentLine.endsWith("*/"))
						{
							skipToNewLine = false;
							isMultiLineComment = false;
							ParsedObjectManager.getInstance().multiLineComments.add(commentLine);
							commentLine = "";
						}
					}
					
					if(skipToNewLine) commentLine += c;
					
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
								skipToNewLine = true;
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
								skipToNewLine = true;
							}
							else
							{
								Log.d("Found a multi-line comment inside a string.");
								commentLine = "";
							}
						}
					}
					
					continue;
				}
				
				// Add a character to the "line"
				if(c != '\r' && c != '\n' && c != '\t')
				{
					line += c;
				}
				else if(line.length() > 0 && line.charAt(line.length() - 1) != ' ')
				{
					// Add a space (just one, even if there's multiple)
					line += ' ';
				}
				
				// If the line ends, start lexing it
				if(c == ';' || c == '{' || c == '}')
				{
					// lexLine(line);
					sentenceAnalyzer.lexLine(line);
					line = "";
				}
			}
			
			// Finally, close the reader
			reader.close();
			
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Prints some resulting information of the file
		
		// printTreeResults();
		
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
			for(CppScope cc : ParsedObjectManager.getInstance().getScopes())
			{
				writer.write(cc.getName() + " (file: " + cc.nameOfFile + ")\n");
				for(CppFunc mf : cc.getFunctions())
				{
					// Log.d("    - " + mf.getType() + " | " + mf.getName());
					writer.write(" - Function: " + mf.getType() + " " + mf.getName() + "\n");
				}
				writer.write("\n");
			}
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
	private void printTreeResults()
	{
		Log.d("Tree results");
		for(CppScope cs : ParsedObjectManager.getInstance().getScopes())
		{
			Log.d(" - " + cs.getName());
			for(CppFunc cp : cs.getFunctions())
			{
				Log.d("    - " + cp.getType() + " | " + cp.getName());
			}
		}
	}
	
	/**
	 * Prints information out to console
	 */
	private void printResults()
	{
		// Print #defines
		if(ParsedObjectManager.getInstance().defines.size() > 0)
		{
			Log.d("defines");
			for(String s : ParsedObjectManager.getInstance().defines) Log.d(" - " + s);
			Log.d();
		}
		
		// Print #includes
		if(ParsedObjectManager.getInstance().includes.size() > 0)
		{
			Log.d("includes");
			for(String s : ParsedObjectManager.getInstance().includes) Log.d(" - " + s);
			Log.d();
		}
		
		// Print class names
		if(ParsedObjectManager.getInstance().classes.size() > 0)
		{
			Log.d("classes");
			for(String s : ParsedObjectManager.getInstance().classes)	Log.d(" - " + s);
			Log.d();
		}
		
		// Print single-line comments
		if(ParsedObjectManager.getInstance().oneLineComments.size() > 0)
		{
			Log.d("oneline comments");
			for(String s : ParsedObjectManager.getInstance().oneLineComments)	Log.d(" - " + s);
			Log.d();
		}
		
		// Print multi-line comments
		if(ParsedObjectManager.getInstance().multiLineComments.size() > 0)
		{
			Log.d("multiline comments");
			for(String s : ParsedObjectManager.getInstance().multiLineComments) Log.d(" - " + s);
			Log.d();
		}
	}
	
}
