package cppParser;

import cppMetrics.LOCMetrics;
import java.util.ArrayList;
import java.util.Stack;

import cppParser.utils.Log;
import cppStructures.*;

/**
 * A singleton object manager for keeping record of
 * the CPP structures created in the parsing process.
 * 
 * @author Harri Pellikka
 */
public class ParsedObjectManager
{

	// If 'true', the same class can be implemented in multiple ways (via PP directives)
	private boolean allowMultipleVariantsOfClass = true;
	
	private static ParsedObjectManager instance = new ParsedObjectManager();
	
	// Reference to the function currently under processing
	public CppFunc currentFunc = null;
	
	// Reference to the class or namespace currently under processing
	public CppScope currentScope = null;
	
	// public String currentNameSpace = "";
	public CppNamespace currentNamespace = null;
	
	private CppFile currentFile = null;
	
	// List of files found in the target folder
	private ArrayList<CppFile> files = new ArrayList<CppFile>();
	
	// List of #defines found in the pre-pass
	// private ArrayList<CppDefine> defines = new ArrayList<CppDefine>();
	
	
	// List of scopes found
	private ArrayList<CppScope> scopes = new ArrayList<CppScope>();
	private Stack<CppScope> cppScopeStack = new Stack<CppScope>();
	
	private CppScope defaultScope = new CppScope("DEFAULT");
	
	// Array lists for misc. stuff found from source code
	ArrayList<String> oneLineComments = new ArrayList<String>();
	ArrayList<String> multiLineComments = new ArrayList<String>();
	// ArrayList<String> defines = new ArrayList<String>();
	ArrayList<String> includes = new ArrayList<String>();
	ArrayList<String> classes = new ArrayList<String>();
    
    ArrayList<CppType> knownTypes=new ArrayList<>();
    ArrayList<LOCMetrics> locMetrics=new ArrayList<>();
	
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
    public void addLocMetric(LOCMetrics loc)
    {
        locMetrics.add(loc);
    }
    
    public ArrayList<LOCMetrics> getLocMetrics()
    {
        return locMetrics;
    }
    
    public ArrayList<CppType> getKnownTypes()
    {
        return knownTypes;
    }
    
    public void addKnownType(CppType type)
    {
        for(CppType ct:knownTypes)
        {
            if(ct.typeName.contentEquals(type.typeName))
            {
                if(ct.parent.contentEquals(type.parent))
                {
                    return;
                }
            }
        }

        knownTypes.add(type);
    }

	public void setDefaultScope()
	{
		currentScope = defaultScope;
	}
	
	public ArrayList<CppScope> getScopes()
	{
		return scopes;
	}
	
	public void addFile(CppFile file)
	{
		this.files.add(file);
	}
	
	public ArrayList<CppFile> getFiles()
	{
		return files;
	}
	
	public CppFile getFileByFilename(String filename)
	{
		for(CppFile cf : files)
		{
			if(cf.getFilename().equals(filename))
				return cf;
		}
		return null;
	}
	
	public void setCurrentFile(CppFile cf)
	{
		this.currentFile = cf;
	}
	
	public void setCurrentFile(String name)
	{
		for(CppFile cf : files)
		{
			if(cf.getFilename().equals(name))
			{
				currentFile = cf;
				return;
			}
		}
		
		currentFile = null;
	}
	
	public CppFile getCurrentFile()
	{
		return currentFile;
	}
	
	
	public CppClass addClass(String name, CppNamespace namespace)
	{
		assert(name != null);
		
		CppClass newClass = null;
		for(CppScope cs : scopes)
		{
			if(cs instanceof CppClass)
			{
				CppClass cClass = (CppClass)cs;
				if(cClass.getName().equals(name))
				{
					if(namespace != null && cClass.namespace != null)
					{
						if(namespace.equals(cClass.namespace))
						{
							newClass = cClass;
							break;
						}
					}
					else
					{
						newClass = cClass;
						break;
					}
				}
			}
		}
		
		if(newClass == null)
		{
			newClass = new CppClass(name);
			newClass.namespace = namespace;
			scopes.add(newClass);
            addKnownType(new CppType(name,CppType.CLASS));
		}
		
		// if(newClass.namespace != null) Log.d("Added class: " + newClass.getName() + " | " + newClass.namespace.getName());
		// else Log.d("Added class: " + newClass.getName());
		
		return newClass;
	}
    
    public CppScope addStruct(String name)
    {
        assert(name != null);
		
		CppScope newStruct = null;
		for(CppScope cs : scopes)
		{
			if(cs.type == CppScope.STRUCT)
			{
				if(cs.getName().equals(name))
				{
					// Log.d("Found an existing scope " + name);
					newStruct = cs;
					break;
				}
			}
		}
		
		if(newStruct == null)
		{
			newStruct = new CppScope(name);
            newStruct.type = CppScope.STRUCT;
			scopes.add(newStruct);
            addKnownType(new CppType(name, CppType.STRUCT));
		}
		
		return newStruct;
    }
    
    public CppScope addUnion(String name)
    {
        assert(name != null);
		
		CppScope newUnion = null;
		for(CppScope cs : scopes)
		{
			if(cs.type == CppScope.UNION)
			{
				if(cs.getName().equals(name))
				{
					// Log.d("Found an existing scope " + name);
					newUnion = cs;
					break;
				}
			}
		}
		
		if(newUnion == null)
		{
			newUnion = new CppScope(name);
            newUnion.type = CppScope.UNION;
			scopes.add(newUnion);
            addKnownType(new CppType(name, CppType.UNION));
		}
		
		return newUnion;
    }

	public void addNamespace(CppNamespace ns, boolean addToStack) 
	{
		if(ns == null)
		{
			throw new NullPointerException("Tried to add namespace that is null.");
		}
		if(ns.getName() == null) throw new NullPointerException("Tried to add namespace which name is null.");
		
		for(CppScope cs : scopes)
		{
			if(cs instanceof CppNamespace)
			{
				if(cs.getName().equals(ns.getName())) return;
			}
		}
		
		// Set the parent namespace, if there is one
		if(addToStack && currentNamespace != null)
		{
			// ns.addParent(currentNamespace);
			currentNamespace.addChild(ns);
		}
		
		scopes.add(ns);
		if(addToStack)
		{
			this.cppScopeStack.push(ns);
			currentNamespace = ns;
		}
	}

	/**
	 * Stores the given function
	 * @param func Function to store
	 * @param b If 'true', set the new function as the current function
	 */
	public void addFunction(CppFunc func, boolean b)
	{
		func = currentScope.addFunc(func);
		if(b) currentFunc = func;
	}

	public Stack<CppScope> getCppScopeStack()
	{
		return this.cppScopeStack;
	}

	public CppNamespace getNamespace(String ns)
	{
		for(CppScope scope : scopes)
		{
			if(scope instanceof CppNamespace)
			{
				if(scope.getName().equals(ns)) return (CppNamespace)scope;
			}
		}
		return null;
	}
}
