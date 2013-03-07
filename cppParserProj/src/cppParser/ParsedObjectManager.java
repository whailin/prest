package cppParser;

import java.util.ArrayList;

import cppStructures.*;

/**
 * A singleton object manager for keeping record of
 * the CPP structures created in the parsing process.
 * 
 * @author Harri Pellikka
 */
public class ParsedObjectManager {

	private static ParsedObjectManager instance = new ParsedObjectManager();
	
	// Reference to the function currently under processing
	public CppFunc currentFunc = null;
	
	// Reference to the class or namespace currently under processing
	public CppScope currentScope = null;
	
	public String currentNameSpace = "";
	
	// List of scopes found
	private ArrayList<CppScope> scopes = new ArrayList<CppScope>();
	
	// Array lists for misc. stuff found from source code
	ArrayList<String> oneLineComments = new ArrayList<String>();
	ArrayList<String> multiLineComments = new ArrayList<String>();
	ArrayList<String> defines = new ArrayList<String>();
	ArrayList<String> includes = new ArrayList<String>();
	ArrayList<String> classes = new ArrayList<String>();
	
	/**
	 * Retrieves the singleton instance
	 * @return The singleton instance
	 */
	public static ParsedObjectManager getInstance()
	{
		return instance;
	}
	
	/**
	 * Private constructor. Called only once by the class itself.
	 */
	private ParsedObjectManager()
	{
		
	}

	public ArrayList<CppScope> getScopes()
	{
		return scopes;
	}
	
	public CppClass addClass(String name)
	{
		CppClass newClass = null;
		for(CppScope cs : scopes)
		{
			if(cs instanceof CppClass)
			{
				if(cs.getName().equals(name))
				{
					Log.d("Found an existing scope " + name);
					newClass = (CppClass)cs;
					break;
				}
			}
		}
		
		if(newClass == null)
		{
			newClass = new CppClass(name);
			scopes.add(newClass);
		}
		
		return newClass;
	}
}
