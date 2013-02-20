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
	
	// Array lists for misc. stuff found from source code
	ArrayList<String> oneLineComments = new ArrayList<String>();
	ArrayList<String> multiLineComments = new ArrayList<String>();
	ArrayList<String> defines = new ArrayList<String>();
	ArrayList<String> includes = new ArrayList<String>();
	ArrayList<String> classes = new ArrayList<String>();
	
	// List of found classes
	// private ArrayList<CppClass> cppClasses = new ArrayList<CppClass>();
	private ArrayList<CppScope> cppScopes = new ArrayList<CppScope>();
	
	// Current class under processing
	// private Stack<CppClass> cppClassStack = new Stack<CppClass>();
	private Stack<CppScope> cppScopeStack = new Stack<CppScope>();
	private CppScope currentScope = null;
	// private CppClass currentCppClass = null;
	
	// If 'true', all "std"-starting stuff is ignored
	private boolean ignoreStd = true;
	
	// List of "splitters" that are used to tokenize a single line of source code
	private String[] splitterChars = new String[] {" ", "(", ")", "{", "}", "->", ";", ",", "=", "+",
            "-", "*", "/", "::"};

	// Current function under processing
	private CppFunc currentFunc = null;
	
	// Braces count when the current function was found
	private int funcBraceCount = 0;
	
	// Currently open braces count
	private int braceCount = 0;
	
	// Current line in the source file (may not reflect the actual processing)
	private int lineno = 0; 
	
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
		
		FileLoader fileLoader = new FileLoader(this.file);
		
		Log.d("done in: " + (double)(System.currentTimeMillis() - startTime) / 1000.0 + " s.");
		Log.d("Found " + fileLoader.getFiles().size() + " files.");
		
		for(String s : fileLoader.getFiles())
		{
			Log.d("Processing " + s + " ...");
			process(s);
		}
		
		Log.d("Processing done. Dumping...");
		
		dumpTreeResults();
		
		Log.d("Dump done.");
		
		long duration = System.currentTimeMillis() - startTime;
		Log.d("Processing took " + duration + " ms.");
		System.out.println("Processing took " + duration + " ms.");
	}
	
	private String currentFile = "";
	
	/**
	 * Processes the given file.
	 * Processing includes tasks such as constructing internal format lines,
	 * tokenizing them and creating a structure tree of the found data.
	 */
	private void process(String file)
	{
		currentFile = file;
		currentFunc = null;
		currentScope = null;
		cppScopeStack.clear();
		braceCount = 0;
		funcBraceCount = 0;
		this.lineDone = false;
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
							oneLineComments.add(commentLine);
							commentLine = "";
						}
					}
					else if(isMultiLineComment)
					{
						if(commentLine.endsWith("*/"))
						{
							skipToNewLine = false;
							isMultiLineComment = false;
							multiLineComments.add(commentLine);
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
							// Skip until new line
							isMultiLineComment = false;
							skipToNewLine = true;
						}
					}
					if(commentLine.startsWith("/*"))
					{
						isMultiLineComment = true;
						skipToNewLine = true;
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
					lexLine(line);
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
		// printResults();
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
			for(CppScope cc : cppScopes)
			{
				writer.write(cc.getName() + " (file: " + cc.nameOfFile + ")\n");
				for(CppFunc mf : cc.getFuncs())
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
		for(CppScope cc : cppScopes)
		{
			Log.d(" - " + cc.getName());
			for(CppFunc mf : cc.getFuncs())
			{
				Log.d("    - " + mf.getType() + " | " + mf.getName());
			}
		}
	}
	
	private void printResults()
	{
		// Print #defines
		if(defines.size() > 0)
		{
			Log.d("defines");
			for(String s : defines) Log.d(" - " + s);
			Log.d();
		}
		
		// Print #includes
		if(includes.size() > 0)
		{
			Log.d("includes");
			for(String s : includes) Log.d(" - " + s);
			Log.d();
		}
		
		// Print class names
		if(classes.size() > 0)
		{
			Log.d("classes");
			for(String s : classes)	Log.d(" - " + s);
			Log.d();
		}
		
		// Print single-line comments
		if(oneLineComments.size() > 0)
		{
			Log.d("oneline comments");
			for(String s : oneLineComments)	Log.d(" - " + s);
			Log.d();
		}
		
		// Print multi-line comments
		if(multiLineComments.size() > 0)
		{
			Log.d("multiline comments");
			for(String s : multiLineComments) Log.d(" - " + s);
			Log.d();
		}
	}

	private void setCurrentScope(String scopeName, boolean addToStack)
	{
		boolean found = false;
		for(CppScope cc : cppScopes)
		{
			if(cc.getName().equals(scopeName))
			{
				if(addToStack) cppScopeStack.push(cc);
				currentScope = cc;
				found = true;
				break;
			}
		}
		
		if(!found)
		{
			
			CppScope cc = new CppScope(scopeName);
			cc.nameOfFile = currentFile;
			cppScopes.add(cc);
			currentScope = cc;
			if(addToStack)
			{
				cppScopeStack.push(cc);
				Log.d("SCOPE " + cppScopeStack.peek().getName() + " START (line: " + lineno + ")");
			}else
			{
				if(!cppScopeStack.isEmpty()) Log.d("SCOPE " + cppScopeStack.peek().getName() + " PART (line: " + lineno + ")");
			}
			
		}
	}
	
	private boolean lineDone = false;
	
	private void lexDefine(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("#define"))
			{
				defines.add(tokens[i+1]);
				i++;
			}
		}
	}
	
	private void lexInclude(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("#include"))
			{
				includes.add(tokens[i+1]);
				i++;
			}
		}
	}
	
	private void currentScopeToClass()
	{
		CppScope cs = cppScopeStack.pop();
		CppClass cc = new CppClass(cs);
		Log.d("Found a scope that is a class > " + cc.name);
		cppScopeStack.push(cc);
	}
	
	private void lexClass(String[] tokens)
	{
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("class"))
			{
				
				for(int j = i + 1; j < tokens.length; ++j)
				{
					if(tokens[j].equals(":") || tokens[j].equals("{"))
					{
						setCurrentScope(tokens[j-1], true);
						if(!(cppScopeStack.peek() instanceof CppClass)) currentScopeToClass();
						cppScopeStack.peek().braceCount = braceCount;
						Log.d("CLASS " + cppScopeStack.peek().getName() + " START (line: " + lineno + ")");
						break;
					}
				}
			}
		}
	}
	
	private void lexEndBrace()
	{
		braceCount--;
		if(currentFunc != null && funcBraceCount == braceCount)
		{
			Log.d("   FUNCTION " + currentFunc.getName() + " END (line: " + lineno + ")");
			currentFunc = null;
		}
		// if(!cppClassStack.isEmpty() && classBraceCount == braceCount)
		if(!cppScopeStack.isEmpty() && cppScopeStack.peek().braceCount == braceCount)
		{
			if(cppScopeStack.peek() instanceof CppClass) Log.d("CLASS " + cppScopeStack.peek().getName() + " END (line: " + lineno + ")");
			else if(cppScopeStack.peek() instanceof CppNamespace)Log.d("NAMESPACE " + cppScopeStack.peek().getName() + " END (line: " + lineno + ")"); 
			else Log.d("SCOPE " + cppScopeStack.peek().getName() + " END (line: " + lineno + ")");
			cppScopeStack.pop();
		}
	}
	
	/**
	 * Checks if the given tokens form a classname::function(); -type of line
	 * @param tokens The tokens that form the line
	 * @param di The index of found "::" token
	 * @return true if the line is of form classname::function(), false otherwise
	 */
	private boolean isFuncWithBody(String[] tokens, int di)
	{
		if(di > 0)
		{
			for(int i = 0; i < tokens[di-1].length(); ++i)
			{
				char c = tokens[di-1].charAt(i);
				if(c == '=') return false;
			}
		}
		
		if(tokens.length > di + 2)
		{
			if(tokens[di + 2].equals("("))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isFuncDecl(String[] tokens, int di)
	{
		return false;
	}
	
	/**
	 * Does lexical analysis (tokenizing) of a given line of code
	 */
	private void lexLine(String line)
	{
		lineDone = false;
		
		// Split the line into tokens
		String[] tokens = StringSplitter.split(line, splitterChars, true);
		
		lexDefine(tokens);
		lexInclude(tokens);
		lexClass(tokens);
		
		for(int i = 0; i < tokens.length; ++i)
		{
			if(tokens[i].equals("{"))
			{
				braceCount++;
				continue;
			}
			else if(tokens[i].equals("}"))
			{
				lexEndBrace();
				continue;
			}
			else if(tokens[i].equals("namespace"))
			{
				if(!tokens[i+1].equals("{"))
				{
					CppNamespace ns = new CppNamespace(tokens[i+1]);
					ns.braceCount = braceCount;
					ns.nameOfFile = currentFile;
					Log.d("NAMESPACE " + ns.name + " START (line: " + lineno + ")");
					cppScopes.add(ns);
					cppScopeStack.add(ns);
				}
			}
			
			else if(currentFunc == null)
			{
				// Either a classname::function or a classname::member found
				if(tokens[i].equals("::") && i > 0)
				{
					if(ignoreStd && tokens[i-1].equals("std")) continue;
					
					// Check if tokens form a function with a body
					if(isFuncWithBody(tokens, i))
					{
						Log.d("   FUNCTION " + tokens[i+1] + " START (line: " + lineno + ")");
						
						// setCurrentCppClass(tokens[i-1]);
						// setCurrentScope(tokens[i-1]);
						setCurrentScope(tokens[i-1], false);
						
						String currentFuncName = tokens[i+1];
						
						String returnType = tokens[0];
						if(returnType.equals(tokens[i-1]) && i == 1)
						{
							if(currentFuncName.startsWith("~")) returnType = "dtor";
							else returnType = "ctor";
						}else{
							if(i > 1)
							{
								for(int j = 1; j < i - 1; ++j)
								{
									returnType += (tokens[j].equals("*") ? "" : " ") + tokens[j];
								}
							}
						}
	
						currentFunc = new CppFunc(returnType, currentFuncName);
						currentScope.addFunc(currentFunc);
						funcBraceCount = braceCount;
						lineDone = true;
					}
					else if(isFuncDecl(tokens, i))
					{
						
					}
				}
			}
		}
		
		if(lineDone)
		{
			return;
		}
		
		if(!cppScopeStack.isEmpty())
		{
			if(currentFunc == null)
			{
				if(!line.trim().equals("}"))
				{
					// Log.d(" - Mem.Line: " + line);
				}
			}
			else
			{
				// Log.d(" - Func.Line: " + line.trim());
				if(tokens[tokens.length - 1].equals(";"))
				{
					boolean isReturn = false;
					
					if(tokens[0].equals("return"))
					{
						// Log.d("        - Return statement");
					}
				}
			}
		}
		
		if(currentFunc != null)
		{
			// if(!line.contains(currentFunc))// Log.d(" - " + line);
			/*
			// System.out.print("   ");
			for(int i = 0; i < tokens.length; ++i)
			{
				// System.out.print(tokens[i]);
				if(i < tokens.length - 1) // System.out.print(" | ");
				else // Log.d();
			}
			*/
		}
	}
}
