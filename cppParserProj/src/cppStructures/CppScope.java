package cppStructures;

import java.util.ArrayList;

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
	
	public int braceCount = 0;
	public String nameOfFile = "";
	
	public ArrayList<MemberVariable> members = new ArrayList<MemberVariable>();
	public ArrayList<CppFunc> funcs = new ArrayList<CppFunc>();
	
	public CppScope(String name)
	{
		this.name = name;
	}
	
	public void addMember(MemberVariable mv)
	{
		members.add(mv);
	}
	
	public void addFunc(CppFunc func)
	{
		funcs.add(func);
	}
	
	public ArrayList<MemberVariable> getMembers()
	{
		return members;
	}
	
	public ArrayList<CppFunc> getFuncs()
	{
		return funcs;
	}

	public boolean hasFunc(CppFunc mf)
	{
		String mfName = mf.getName();
		for(CppFunc mem : funcs)
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
}
