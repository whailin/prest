package cppStructures;

import java.util.ArrayList;

import cppParser.Log;

/**
 * CPP Scope
 * 
 * A scope can be either a namespace or a class (whatever 'foo' is in 'foo::bar').
 * All 'foos' like this are first stored as CppScope objects and as more information
 * is parsed, they are converted to CppClass (if they are classes)
 * 
 * @author Harri Pellikka
 *
 */
public class CppScope
{
	public String name = "_MAIN_";
	public String getName() { return name; }
	
	public ArrayList<CppScope> parents = null;
	public ArrayList<CppScope> children = null;
	public CppScope parentScope = null;	// Namespace, outer class etc.
	
	public int braceCount = 0;
	public String nameOfFile = "";
	
	protected ArrayList<MemberVariable> members = new ArrayList<MemberVariable>();
	protected ArrayList<CppFunc> functions = new ArrayList<CppFunc>();
	
	/**
	 * Constructs a new scope with the given name
	 * @param name Name of the scope
	 */
	public CppScope(String name)
	{
		this.name = name;
		this.children = new ArrayList<CppScope>();
		this.parents = new ArrayList<CppScope>();
	}
	
	/**
	 * Adds a new member variable to the scope
	 * @param mv Member variable to add
	 */
	public void addMember(MemberVariable mv)
	{
		members.add(mv);
	}
	
	/**
	 * Adds a new function to the scope
	 * @param func Function to add
	 */
	public void addFunc(CppFunc func)
	{
		Log.d(this.getName() + ": Trying to add function: " + func.getName() + " (Param count: " + func.parameters.size());
		
		if(!hasFunc(func)) functions.add(func);
	}
	
	/**
	 * Retrieves the list of member variables of this scope
	 * @return The list of member variables
	 */
	public ArrayList<MemberVariable> getMembers()
	{
		return members;
	}
	
	/**
	 * Retrieves the list of functions in this scope
	 * @return The list of functions
	 */
	public ArrayList<CppFunc> getFunctions()
	{
		return functions;
	}

	/**
	 * Checks whether or not a given function can be found in this scope
	 * @param mf The function to search for
	 * @return 'true' if the function was found, 'false' otherwise
	 */
	public boolean hasFunc(CppFunc mf)
	{
		// TODO Check parameter count and types of parameters for overloaded functions as well
		String mfName = mf.getName();
		for(CppFunc mem : functions)
		{
			if(mem.getName().equals(mfName))
			{
				// If the parameter count differs, skip the parameter check
				if(mem.parameters.size() != mf.parameters.size()) continue;
				
				int paramCount = mem.parameters.size();
				int matchingParams = 0;
				
				for(CppFuncParam cfp0 : mem.parameters)
				{
					for(CppFuncParam cfp1 : mf.parameters)
					{
						if(cfp0.type.equals(cfp1.type))
						{
							matchingParams++;
						}
					}
				}
				
				if(matchingParams == paramCount)
				{
					Log.d("Found an existing function > " + mf.getName());
					return true;
				}
			}
			
		}
		return false;
	}
	
	/**
	 * Checks whether or not a given member variable can be found in this scope
	 * @param mv The member variable to search for
	 * @return 'true' if the member variable was found, 'false' otherwise
	 */
	public boolean hasMember(MemberVariable mv)
	{
		String mvName = mv.getName();
		for(MemberVariable mem : members)
		{
			if(mem.getName().equals(mvName)) return true;
		}
		return false;
	}
	
	/**
	 * Adds a new child to this scope
	 * @param cc The child scope
	 */
	public void addChild(CppScope cc)
	{
		boolean canAdd = true;
		for(CppScope c : children)
		{
			if(c.getName().equals(cc.getName()))
			{
				canAdd = false;
				break;
			}
		}
		
		if(canAdd)
		{
			children.add(cc);
			cc.addParent(this);
		}
	}
	
	/**
	 * Adds a new parent for this scope
	 * @param cc The parent scope
	 */
	public void addParent(CppScope cc)
	{
		parents.add(cc);
	}
}
