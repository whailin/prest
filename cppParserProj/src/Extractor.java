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

/**
 * Extractor.java
 * Provides lexical analysis and metrics extraction of C++ source and header files.
 * 
 * @author Harri Pellikka
 */
public class Extractor
{
	// List of allowed file extensions
	private String[] allowedExtensions = new String[] {".cpp", ".h", ".cxx"};
	
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
	
	private boolean isValidExtension(String s)
	{
		for(int i = 0; i < allowedExtensions.length; ++i)
		{
			if(s.endsWith(allowedExtensions[i])) return true;
		}
		return false;
	}

	/**
	 * Searches recursively for files in the given folder and all the subfolders.
	 * 
	 * @param folder The root folder to search from
	 * @return List of files in the given folder and all the subfolders
	 */
	private ArrayList<String> getFilesFrom(String folder)
	{
		ArrayList<String> files = new ArrayList<String>();
		File f = new File(folder);
		File[] entries = f.listFiles();
		for(File entry : entries)
		{
			if(entry.isFile())
			{
				String s = entry.getPath();
				if(isValidExtension(s)) files.add(s);
			}
			else if(entry.isDirectory())
			{
				ArrayList<String> subFiles = getFilesFrom(entry.getPath());
				for(String s : subFiles)
				{
					if(isValidExtension(s)) files.add(s);
				}
			}
		}
		
		return files;
	}
	
	private ArrayList<String> sortFiles(ArrayList<String> files)
	{
		ArrayList<String> ordered = new ArrayList<String>();
		
		for(int i = 0; i < files.size(); ++i)
		{
			String s = files.get(i);
			if(s.charAt(s.lastIndexOf('.') + 1) == 'c')
			{
				ordered.add(s);
			}else{
				ordered.add(0, s);
			}
		}
		
		return ordered;
	}
	
	/**
	 * Starts processing the files
	 */
	public void process()
	{
		System.out.println("Processing started.");
		System.out.print("Finding files and sorting... ");
		long startTime = System.currentTimeMillis();
		
		// this.process(file);
		ArrayList<String> listOfFiles = new ArrayList<String>();
		File f = new File(file);
		if(f.isDirectory())
		{
			listOfFiles = getFilesFrom(file);
		}else{
			listOfFiles.add(file);
		}
		
		// Order so that .h files are before .cpp/.cxx files
		listOfFiles = sortFiles(listOfFiles);
		
		System.out.println("done in: " + (double)(System.currentTimeMillis() - startTime) / 1000.0 + " s.");
		
		for(String s : listOfFiles)
		{
			System.out.println("Processing " + s + " ...");
			process(s);
		}
		
		System.out.println("Processing done. Dumping...");
		
		dumpTreeResults();
		
		System.out.println("Dump done.");
		
		long duration = System.currentTimeMillis() - startTime;
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
					// System.out.println("    - " + mf.getType() + " | " + mf.getName());
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
		System.out.println("Tree results");
		for(CppScope cc : cppScopes)
		{
			System.out.println(" - " + cc.getName());
			for(CppFunc mf : cc.getFuncs())
			{
				System.out.println("    - " + mf.getType() + " | " + mf.getName());
			}
		}
	}
	
	private void printResults()
	{
		// Print #defines
		if(defines.size() > 0)
		{
			System.out.println("defines");
			for(String s : defines) System.out.println(" - " + s);
			System.out.println();
		}
		
		// Print #includes
		if(includes.size() > 0)
		{
			System.out.println("includes");
			for(String s : includes) System.out.println(" - " + s);
			System.out.println();
		}
		
		// Print class names
		if(classes.size() > 0)
		{
			System.out.println("classes");
			for(String s : classes)	System.out.println(" - " + s);
			System.out.println();
		}
		
		// Print single-line comments
		if(oneLineComments.size() > 0)
		{
			System.out.println("oneline comments");
			for(String s : oneLineComments)	System.out.println(" - " + s);
			System.out.println();
		}
		
		// Print multi-line comments
		if(multiLineComments.size() > 0)
		{
			System.out.println("multiline comments");
			for(String s : multiLineComments) System.out.println(" - " + s);
			System.out.println();
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
				System.out.println("SCOPE " + cppScopeStack.peek().getName() + " START (line: " + lineno + ")");
			}else
			{
				if(!cppScopeStack.isEmpty()) System.out.println("SCOPE " + cppScopeStack.peek().getName() + " PART (line: " + lineno + ")");
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
		System.out.println("Found a scope that is a class > " + cc.name);
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
						System.out.println("CLASS " + cppScopeStack.peek().getName() + " START (line: " + lineno + ")");
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
			System.out.println("   FUNCTION " + currentFunc.getName() + " END (line: " + lineno + ")");
			currentFunc = null;
		}
		// if(!cppClassStack.isEmpty() && classBraceCount == braceCount)
		if(!cppScopeStack.isEmpty() && cppScopeStack.peek().braceCount == braceCount)
		{
			if(cppScopeStack.peek() instanceof CppClass) System.out.println("CLASS " + cppScopeStack.peek().getName() + " END (line: " + lineno + ")");
			else if(cppScopeStack.peek() instanceof CppNamespace)System.out.println("NAMESPACE " + cppScopeStack.peek().getName() + " END (line: " + lineno + ")"); 
			else System.out.println("SCOPE " + cppScopeStack.peek().getName() + " END (line: " + lineno + ")");
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
					System.out.println("NAMESPACE " + ns.name + " START (line: " + lineno + ")");
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
						System.out.println("   FUNCTION " + tokens[i+1] + " START (line: " + lineno + ")");
						
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
					// System.out.println(" - Mem.Line: " + line);
				}
			}
			else
			{
				// System.out.println(" - Func.Line: " + line.trim());
				if(tokens[tokens.length - 1].equals(";"))
				{
					boolean isReturn = false;
					
					if(tokens[0].equals("return"))
					{
						// System.out.println("        - Return statement");
					}
				}
			}
		}
		
		if(currentFunc != null)
		{
			// if(!line.contains(currentFunc))// System.out.println(" - " + line);
			/*
			// System.out.print("   ");
			for(int i = 0; i < tokens.length; ++i)
			{
				// System.out.print(tokens[i]);
				if(i < tokens.length - 1) // System.out.print(" | ");
				else // System.out.println();
			}
			*/
		}
	}
}
