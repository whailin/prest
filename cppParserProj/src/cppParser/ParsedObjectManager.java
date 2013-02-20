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
	
	private ArrayList<CppScope> scopes = new ArrayList<CppScope>();
	
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
}
