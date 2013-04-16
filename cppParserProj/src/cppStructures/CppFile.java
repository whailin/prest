package cppStructures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import cppParser.ParsedObjectManager;

/**
 * Represents a CPP file
 * @author Harri Pellikka
 */
public class CppFile {

	private String filename = null;
	private ArrayList<String> includes = null;
	private ArrayList<CppDefine> defines = null;
	
	public CppFile(String filename)
	{
		this.filename = filename;
		includes = new ArrayList<String>();
		defines = new ArrayList<CppDefine>();
	}
	
	public void addInclude(String include)
	{
		this.includes.add(include);
	}
	
	public ArrayList<String> getIncludes()
	{
		return includes;
	}
	
	public void addDefine(CppDefine cd)
	{
		this.defines.add(cd);
	}
	
	public ArrayList<CppDefine> getDefines()
	{
		return defines;
	}
	
	public String getFilename()
	{
		return filename;
	}
	
	/**
	 * Expands #includes so that the filenames become absolute paths
	 */
	public void expandIncludes()
	{
		ArrayList<String> expandedIncludes = new ArrayList<String>();
		
		for(String s : includes)
		{
			for(CppFile cf : ParsedObjectManager.getInstance().getFiles())
			{
				if(cf.getFilename().endsWith(s))
				{
					expandedIncludes.add(cf.getFilename());
					break;
				}
			}
		}
		
		includes = expandedIncludes;
	}

	
	public ArrayList<CppDefine> getDefinesRecursively(ArrayList<String> incs) {
		
		if(incs == null) incs = new ArrayList<String>();
		ArrayList<CppDefine> defs = new ArrayList<CppDefine>();
		incs.add(filename);
		
		// Add the current file's #defines
		for(CppDefine cd : defines) defs.add(cd);
		
		// Recurse through #includes
		for(String s : includes)
		{
			if(incs.contains(s)) continue;
			
			CppFile cf = ParsedObjectManager.getInstance().getFileByFilename(s);
			defs.addAll(cf.getDefinesRecursively(incs));
			
			incs.add(s);
		}
		
		
		
		/*
		for(String s : includes)
		{
			CppFile cf = ParsedObjectManager.getInstance().getFileByFilename(s);
			defs.addAll(cf.getDefinesRecursively(incs));
		}
		*/
		
		
		return defs;
	}
	
	public void dump(BufferedWriter writer, HashSet<CppFile> visited, int depth) throws IOException
	{
		visited.add(this);
		for(int i = 0; i < depth; ++i)
		{
			writer.write("   ");
		}
		writer.write(this.getFilename() + "\n");
		for(String s : includes)
		{
			CppFile next = ParsedObjectManager.getInstance().getFileByFilename(s);
			if(!visited.contains(next))
			{
				next.dump(writer, visited, depth + 1);
			}
		}
	}
}
