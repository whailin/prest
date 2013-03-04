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
	
	public int braceCount = 0;
	public String nameOfFile = "";
	
	protected ArrayList<MemberVariable> members = new ArrayList<MemberVariable>();
	protected ArrayList<CppFunc> functions = new ArrayList<CppFunc>();
	
	public CppScope(String name)
	{
		this.name = name;
		this.children = new ArrayList<CppScope>();
		this.parents = new ArrayList<CppScope>();
	}
	
	public void addMember(MemberVariable mv)
	{
		members.add(mv);
	}
	
	public void addFunc(CppFunc func)
	{
		functions.add(func);
	}
	
	public ArrayList<MemberVariable> getMembers()
	{
		return members;
	}
	
	public ArrayList<CppFunc> getFunctions()
	{
		return functions;
	}

	public boolean hasFunc(CppFunc mf)
	{
		String mfName = mf.getName();
		for(CppFunc mem : functions)
		{
			if(mem.getName().equals(mfName)) return true;
		}
		return false;
	}
	
	public boolean hasMember(MemberVariable mv)
	{
		String mvName = mv.getName();
		for(MemberVariable mem : members)
		{
			if(mem.getName().equals(mvName)) return true;
		}
		return false;
	}
	
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
			cc.setParent(this);
		}
	}
	
	public void setParent(CppScope cc)
	{
		parents.add(cc);
	}
}
