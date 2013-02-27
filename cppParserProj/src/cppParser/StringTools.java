package cppParser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A collection of parsing and lexing -related string tools
 * 
 * @author Harri Pellikka 
 */
public class StringTools
{
	
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
}
