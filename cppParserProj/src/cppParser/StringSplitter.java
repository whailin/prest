package cppParser;
import java.util.ArrayList;


public class StringSplitter
{
	public static String[] split(String src, String[] delims, boolean includeDelims)
	{
		if(src == null || src.length() == 0) return null;
		if(src.length() == 1 || delims == null || delims.length == 0) return new String[]{src};
		
		// char[] delimChars = delims.toCharArray();
		ArrayList<String> parts = new ArrayList<String>();
		
		String s = "";
		for(int i = 0; i < src.length(); ++i)
		{
			boolean shouldSplit = false;
			String includedDelim = null;
			
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
				s += src.charAt(i);
			}
		}
		
		return listToArray(parts);
	}

	private static String[] listToArray(ArrayList<String> list)
	{
		String[] retParts = new String[list.size()];
		for(int i = 0; i < retParts.length; ++i)
		{
			retParts[i] = list.get(i);
		}
		
		return retParts;
	}
}
