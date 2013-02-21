package cppStructures;
import java.util.ArrayList;


public class CppClass extends CppScope {

	
	
	public CppClass(String name)
	{
		super(name);
	}
	
	public CppClass(CppScope scope)
	{
		super(scope.name);
		this.braceCount = scope.braceCount;
		this.functions = scope.functions;
		this.members = scope.members;
		this.nameOfFile = scope.nameOfFile;
	}
}
