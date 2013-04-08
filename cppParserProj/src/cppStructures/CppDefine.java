package cppStructures;

import cppParser.Extractor;

/**
 * Represents a #define statement
 *
 * @author Harri Pellikka 
 */
public class CppDefine {
	
	// File of the #define statement
	private String file = null;
	
	// Name of the define
	private String name = null;
	
	// The definition in #define statement
	private String definition = null;
	
	/**
	 * Constructs a new CppDefine objects
	 * @param name Name of the definition
	 * @param definition The raw definition string
	 */
	public CppDefine(String name, String definition)
	{
		this.name = name;
		this.definition = definition;
		file = Extractor.currentFile;
	}
	
	/**
	 * Retrieves the filename the #define statement was found in
	 * @return The filename of the #define statement
	 */
	public String getFile()
	{
		return file;
	}
	
	/**
	 * Retrieves the name of the #define statement
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Retrieves the raw definition in a string form
	 * @return String representation of the definition
	 */
	public String getDefinition()
	{
		return definition;
	}
}
