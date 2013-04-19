package cppStructures;

import cppParser.ParsedObjectManager;
import cppParser.utils.Log;


public class CppClass extends CppScope {

	private int depthOfInheritance = -1;
	
	public CppClass(String name)
	{
		super(name);
        type=CLASS;
	}
	
	public CppClass(CppScope scope)
	{
		super(scope.name);
		this.braceCount = scope.braceCount;
		this.functions = scope.functions;
		this.members = scope.members;
		this.nameOfFile = scope.nameOfFile;
        type=CLASS;
	}
	
	public int getDepthOfInheritance()
	{
		if(depthOfInheritance == -1)
		{
			calculateDepthOfInheritance();
		}
		return depthOfInheritance;
	}
	
	/**
	 * Calculates the depth of inheritance tree for this
	 * class. The depth is the highest depth of inheritance,
	 * in case of multiple inheritance.
	 */
	public void calculateDepthOfInheritance()
	{
		// Log.d("DOI: " + this.name + " : " + this.nameOfFile);
		for(CppScope cs : this.parents)
		{
			if(this == cs)
			{
				// Log.d("CHILD SAME AS PARENT?");
				break;
			}
			
			if(cs instanceof CppClass)
			{
				if(((CppClass) cs).getDepthOfInheritance() + 1 > depthOfInheritance)
				{
					this.depthOfInheritance = ((CppClass) cs).getDepthOfInheritance() + 1;
				}
			}
		}
		
		if(depthOfInheritance == -1) depthOfInheritance = 0;
	}

	/**
	 * Sets the namespace of this class to 'ns'
	 * @param ns Name of the namespace
	 */
	public void setNamespace(String ns)
	{
		// Search for existing namespace
		for(CppScope cs : ParsedObjectManager.getInstance().getScopes())
		{
			if(cs instanceof CppNamespace)
			{
				CppNamespace cns = (CppNamespace)cs;
				if(cns.getName().equals(ns))
				{
					this.namespace = cns;
					return;
				}
			}
		}
		
		// Existing namespace not found, create a new one
		CppNamespace cns = new CppNamespace(ns);
		this.namespace = cns;
		ParsedObjectManager.getInstance().addNamespace(cns, false);
	}
}
