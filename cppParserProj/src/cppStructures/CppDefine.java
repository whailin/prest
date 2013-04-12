package cppStructures;

import java.util.ArrayList;

import cppParser.Extractor;

/**
 * Represents a #define statement
 *
 * @author Harri Pellikka 
 */
public class CppDefine {
	
	// Origin file of the #define statement
	private String file = null;
	
	// List of users for this #define (the origin file plus all files that #include this)
	private ArrayList<String> users = null;
	
	// Name of the define
	private String name = null;
	
	private ArrayList<String> params = null;
	
	// The definition in #define statement
	private String definition = null;
	
	/**
	 * Constructs a new CppDefine object without parameters
	 * @param name Name of the definition
	 * @param definition The raw definition string
	 */
	public CppDefine(String name, String definition)
	{
		this.name = name;
		this.definition = definition;
		file = Extractor.currentFile;
		users = new ArrayList<String>();
		users.add(file);
	}
	
	/**
	 * Constructs a new CppDefine with parameters
	 * @param name Name of the definition
	 * @param params List of parameters
	 * @param definition The raw definition string
	 */
	public CppDefine(String name, ArrayList<String> params, String definition)
	{
		this.name = name;
		this.params = params;
		this.definition = definition;
		file = Extractor.currentFile;
		users = new ArrayList<String>();
		users.add(file);
	}
	
	public void addUser(String file)
	{
		if(!users.contains(file)) users.add(file);
	}
	
	public ArrayList<String> getUsers()
	{
		return users;
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
	 * Retrieves the parameters of the definition
	 * @return List of parameters
	 */
	public ArrayList<String> getParameters()
	{
		return params;
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
