
public class MemberVariable {

	private String type = "";
	private String name = "";
	private String value = "";
	private boolean valueSet = false;
	
	public MemberVariable(String type, String name)
	{
		this.type = type;
		this.name = name;
	}
	
	public MemberVariable(String type, String name, String value)
	{
		this.type = type;
		this.name = name;
		this.value = value;
		valueSet = true;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
		valueSet = true;
	}
	
	public boolean isValueSet()
	{
		return valueSet;
	}
}
