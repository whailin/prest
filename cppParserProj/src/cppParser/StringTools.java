package cppParser;

import java.util.ArrayList;

/**
 * A collection of parsing and lexing -related string tools
 * 
 * @author Harri Pellikka 
 */
public class StringTools
{
	// List of "splitters" that are used to tokenize a single line of source code
	public static String[] delims = new String[] {" ", "(", ")", "{", "}", "->", ";", ",", "=", "+", "-", "*", "/", "::", ":", ".", "\""};
		
	
	// List of C++11 keywords, types (char, int, bool etc.) and type-related (signed, unsigned) words omitted
	public static String[] keywords_notypes = {"alignas", "alignof", "and", "and_eq", "asm", "auto", "bitand", 
		                                       "bitor", "break", "case", "catch", "class", "compl", "const",
		                                       "constexpr", "const_cast", "continue", "decltype", "default", 
		                                       "delete", "do", "dynamic_cast", "else", "enum", "explicit", 
		                                       "export", "extern", "false", "for", "friend", "goto", "if", 
		                                       "inline", "mutable", "namespace", "new", "noexcept", "not", 
		                                       "not_eq", "nullptr", "operator", "or", "or_eq", "private", 
		                                       "protected", "public", "register", "reinterpret_cast", "return", 
		                                       "sizeof", "static", "static_assert", "static_cast", "struct", 
		                                       "switch", "template", "this", "thread_local", "throw", "true", 
		                                       "try", "typedef", "typeid", "typename", "union", "using", 
		                                       "virtual", "volatile", "while", "xor", "xor_eq"};
	
	// List of Halstead operators
	public static String[] operators = {")", "}", "]", "+", "-", "*", "/", "%", ".", ",", "->", "==", "<=", ">=", "!=", "<<", ">>", "=", "<", ">", "&&", "&", "||", "|", "!", "^"};
	
	
	/**
	 * A simple non-regex-splitter that splits the given string into tokens with the given deliminators.
	 * 
	 * @param src Source string to split
	 * @param delims Array of deliminators
	 * @param includeDelims if 'true', the deliminators are included in the resulting array
	 * @return Array of tokens representing the original string
	 */
	public static String[] split(String src, String[] delims, boolean includeDelims)
	{
		if(delims == null) delims = StringTools.delims;
		
		// Bail out on trivial input
		if(src == null || src.length() == 0) return null;
		if(src.length() == 1 || delims == null || delims.length == 0) return new String[]{src};
		
		// Init the list of parts
		ArrayList<String> parts = new ArrayList<String>();
		
		// Loop through the input string
		String s = "";
		for(int i = 0; i < src.length(); ++i)
		{
			boolean shouldSplit = false;
			String includedDelim = null;
			
			// Check for deliminator
			for(int j = 0; j < delims.length; ++j)
			{
				int matched = 0;
				for(int k = 0; k < delims[j].length(); ++k)
				{
					if(i+k < src.length())
					{
						if(src.charAt(i+k) == delims[j].charAt(k))
						{
							matched++;
							if(matched == delims[j].length())
							{
								shouldSplit = true;
								includedDelim = delims[j];
								break;
							}
						}
					}
				}
				if(shouldSplit) break;
			}
			
			// If a delim was found, split the string
			if(shouldSplit)
			{
				if(s.length() > 0) parts.add(s);
				if(includeDelims && includedDelim != null)
				{
					if(src.charAt(i) != ' ')
					{
						parts.add(includedDelim);
						i += includedDelim.length() - 1;
						includedDelim = null;
					}
				}
				s = "";
			}else{
				// No delim was found yet, add the current char to the current string
				s += src.charAt(i);
			}
		}
		
		if(!s.equals(""))
		{
			parts.add(s);
		}
		
		// Finally, convert the ArrayList to a simple array and return it
		return listToArray(parts);
	}

	/**
	 * Converts an arraylist into an array
	 * @param list An arraylist to convert
	 * @return An array
	 */
	private static String[] listToArray(ArrayList<String> list)
	{
		String[] retParts = new String[list.size()];
		for(int i = 0; i < retParts.length; ++i)
		{
			retParts[i] = list.get(i);
		}
		
		return retParts;
	}

	/**
	 * Counts the amount of quotes (") in a given string
	 * @param line The string to count the quotes in
	 * @return The count of quotes in the string
	 */
	public static int getQuoteCount(String line) {
		int count = 0;
		for(int i = 0; i < line.length(); ++i)
		{
			if(line.charAt(i) == '"')
			{
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Checks whether or not a given string is a reserved keyword of C++.
	 * @param s The string to check
	 * @return 'True' if the string is a keyword, 'false' otherwise
	 */
	public static boolean isKeyword(String s)
	{
		for(int i = 0; i < keywords_notypes.length; ++i)
		{
			if(keywords_notypes[i].equals(s)) return true;
		}
		return false;
	}
	
	/**
	 * Checks whether or not a given string is an operator (according to Halstead).
	 * @param s The string to check
	 * @return 'true' if the string is an operator, 'false' otherwise
	 */
	public static boolean isOperator(String s)
	{
		for(int i = 0; i < operators.length; ++i)
		{
			if(operators[i].equals(s)) return true;
		}
		
		return false;
	}
}
