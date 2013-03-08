package cppStructures;

import java.util.ArrayList;

/**
 * CppFunc.java
 * Represents a C++ function. Keeps track of the function's data and metrics:
 * - Halstead metrics -related data (operators, operands)
 * - Halstead metrics -related derived data (vocabulary, length, volume, difficulty, effort etc.)
 * - Cyclomatic complexity
 * 
 * @author Harri Pellikka
 */
public class CppFunc {

	private String type = "void";
	private String name = "";
	// private MetricsParam[] params;
	
	public String fileOfFunc = "";
	
	public int funcBraceCount = 0;
	
	// List of recognized lines, only for debugging
	public ArrayList<String> recognizedLines = new ArrayList<String>();
	
	// List of parameters
	public ArrayList<CppFuncParam> parameters = new ArrayList<CppFuncParam>();
	
	// Container for non-completely parsed lines (unknown references etc.)
	private ArrayList<String[]> nonCompleteLines = new ArrayList<String[]>();
	
	private ArrayList<MemberVariable> members = new ArrayList<MemberVariable>();
	
	// Halstead-related containers
	private ArrayList<String> operators = new ArrayList<String>();
	private ArrayList<String> operands = new ArrayList<String>();
	private ArrayList<String> uniqueOperators = new ArrayList<String>();
	private ArrayList<String> uniqueOperands = new ArrayList<String>();
	
	// Cyclomatic complexity-related containers
	private ArrayList<String> statements = new ArrayList<String>();
	
	// Cyclomatic complexity
	private int cyclomaticComplexity = 1;
	
	// Halstead-related metrics
	private int vocabulary = 0;
	private int length = 0;
	private double calculatedLength = 0.0;
	private double volume = 0.0;
	private double difficulty = 0.0;
	private double effort = 0.0;
	private double timeToProgram = 0.0;
	private double deliveredBugs = 0.0;
	private double level = 0.0;
	private double intContent = 0.0;
	
	/**
	 * Constructs a new CppFunc with the given return type and name
	 * @param type Return type of this function
	 * @param name Name of this function
	 */
	public CppFunc(String type, String name)
	{
		this.type = type;
		this.name = name;
	}

	/**
	 * Retrieves the return type of this function
	 * @return The return type of this function
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Increases cyclomatic complexity by one
	 */
	public void incCC()
	{
		cyclomaticComplexity++;
	}
	
	/**
	 * Retrieves the list of operators in this function
	 * @return List of operators in this function
	 */
	public ArrayList<String> getOperators()
	{
		return operators;
	}
	
	/**
	 * Retrieves the list of unique operators in this function
	 * @return List of unique operators in this function
	 */
	public ArrayList<String> getUniqueOperators()
	{
		return uniqueOperators;
	}
	
	/**
	 * Retrieves the name of this function
	 * @return The name of this function
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Retrieves the McCabe's cyclomatic complexity value
	 * @return The cyclomatic complexity of this function
	 */
	public int getCyclomaticComplexity()
	{
		return cyclomaticComplexity;
	}
	
	/**
	 * Adds the given member variable to this function's member variable list
	 * @param mv The member variable to add
	 */
	public void addMember(MemberVariable mv)
	{
		members.add(mv);
	}
	
	/**
	 * Retrieves the list of member variables
	 * @return The list of member variables
	 */
	public ArrayList<MemberVariable> getMembers()
	{
		return members;
	}
	
	/**
	 * Retrieves a member variable 'name'
	 * @param name The name of the member variable
	 * @return The member variable, or 'null' if the member variable was not found
	 */
	public MemberVariable getMember(String name)
	{
		for(MemberVariable mv : members)
		{
			if(mv.getName().equals(name)) return mv;
		}
		return null;
	}
	
	/**
	 * Checks whether or not the given member "name" is a member of this function
	 * @param name The name of the member
	 * @return 'true' if is a member, 'false' otherwise
	 */
	public boolean isMember(String name)
	{
		for(MemberVariable mv : members)
		{
			if(mv.getName().equals(name)) return true;
		}
		return false;
	}
	
	/**
	 * Adds the given operator to the list of operators, and if the
	 * operator is unique, to the list of unique operators.
	 * @param op The operator to add
	 */
	public void addOperator(String op)
	{
		boolean isUnique = true;
		for(String s : operators)
		{
			if(s.equals(op))
			{
				isUnique = false;
				break;
			}
		}
		operators.add(op);
		if(isUnique) uniqueOperators.add(op);
	}
	
	/**
	 * Adds the given operand to the list of operands, and if the
	 * operand is unique, to the list of unique operands.
	 * @param od The operand to add
	 */
	public void addOperand(String od)
	{
		boolean isUnique = true;
		for(String s : operands)
		{
			if(s.equals(od))
			{
				isUnique = false;
				break;
			}
		}
		operands.add(od);
		if(isUnique) uniqueOperands.add(od);
		
	}
	
	/**
	 * Retrieves the operator count of the function
	 * @return The operator count
	 */
	public int getOperatorCount()
	{
		return operators.size();
	}
	
	/**
	 * Retrieves the operand count of the function
	 * @return The operand count
	 */
	public int getOperandCount()
	{
		return operands.size();
	}
	
	/**
	 * Retrieves the unique operator count of the function
	 * @return The unique operator count
	 */
	public int getUniqueOperatorCount()
	{
		return uniqueOperators.size();
	}
	
	/**
	 * Retrieves the unique operand count of the function
	 * @return The unique operand count
	 */
	public int getUniqueOperandCount()
	{
		return uniqueOperands.size();
	}
	
	/**
	 * Retrieves the vocabulary of the function (unique operators + unique operands)
	 * @return The vocabulary of the function
	 */
	public int getVocabulary()
	{
		if(vocabulary == 0)		
			vocabulary = getUniqueOperatorCount() + getUniqueOperandCount();		
		return vocabulary;
	}
	
	/**
	 * Retrieves the length of the function (operators + operands)
	 * @return The length of the function
	 */
	public int getLength()
	{
		if(length == 0)		
			length = getOperatorCount() + getOperandCount();		
		return length;
	}
	
	/**
	 * Retrieves the calculated length of the function (uops * log2(uops) + uods * log2(uods))
	 * @return The calculated length of the function
	 */
	public double getCalculatedLength()
	{
		if(calculatedLength == 0.0)
		{
			int uopcount = getUniqueOperatorCount();
			int uodcount = getUniqueOperandCount();
			calculatedLength = uopcount * (Math.log(uopcount) / Math.log(2)) + uodcount * (Math.log(uodcount) / Math.log(2));
		}
		return calculatedLength;
	}
	
	/**
	 * Retrieves the volume of the function (length * log2(vocabulary))
	 * @return The volume of the function
	 */
	public double getVolume()
	{
		if(volume == 0.0)
		{
			int length = getLength();
			int vocab = getVocabulary();
			volume = (double)length * (Math.log((double)vocab) / Math.log(2));
		}
		return volume;
	}
	
	/**
	 * Retrieves the difficulty of the function (uops / 2 * ods / uods)
	 * @return The difficulty of the function
	 */
	public double getDifficulty()
	{
		if(difficulty == 0.0)
		{
			difficulty = ((double)getUniqueOperatorCount() / 2.0) * ((double)getOperandCount() / (double)getUniqueOperandCount());
		}
		return difficulty;
	}
	
	/**
	 * Retrieves the effort value (difficulty * volume)
	 * @return The effort value
	 */
	public double getEffort()
	{
		if(effort == 0.0)
		{
			effort = getDifficulty() * getVolume();
		}
		return effort;
	}
	
	/**
	 * Retrieves the time-to-program value (effort / 18)
	 * @return The time-to-program value
	 */
	public double getTimeToProgram()
	{
		if(timeToProgram == 0.0)
		{
			timeToProgram = getEffort() / 18.0;
		}
		return timeToProgram;
	}
	
	/**
	 * Retrieves the delivered bugs estimate (effort*2/3 / 3000)
	 * @return The delivered bugs estimate value
	 */
	public double getDeliveredBugs()
	{
		if(deliveredBugs == 0.0)
		{
			deliveredBugs = Math.pow(getEffort(), 2.0/3.0) / 3000.0;
		}
		return deliveredBugs;
	}

	/**
	 * Retrieves the level of the function (inverse of difficulty)
	 * @return The level value
	 */
	public double getLevel()	{
		if (level == 0.0)
			level = 1.0 / getDifficulty();
		return level;
	}
	
	/**
	 * Retrieves the intelligent content of the function (volume / difficulty)
	 * @return The intelligent content value
	 */
	public double getIntContent()	{
		if (intContent == 0.0)
			intContent = getVolume() / getDifficulty();
		return intContent;
	}

	/**
	 * Sets the cyclomatic complexity of the function
	 * @param i The complexity value
	 */
	public void setCyclomaticComplexity(int i)
	{
		if(i > 1) this.cyclomaticComplexity = i;
	}

	/**
	 * Retrieves the array of statements in this function
	 * @return Array of statements
	 */
	public ArrayList<String> getStatements() {
		return this.statements;
	}

}
