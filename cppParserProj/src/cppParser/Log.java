package cppParser;

/**
 * Static logger class for writing verbose information
 * during the processing.
 * 
 * @author Harri Pellikka
 */
public class Log {

	public static boolean isSilent = true;
	
	public static void d()
	{
		d("");
	}
	
	public static void d(String s)
	{
		if(!isSilent)
		{
			System.out.println(s);
		}
	}
	
}
