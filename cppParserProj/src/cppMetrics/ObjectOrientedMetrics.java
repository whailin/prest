package cppMetrics;

public class ObjectOrientedMetrics implements Metrics {
	

	@Override
	public void calculateMetrics() {
		// TODO Auto-generated method stub

	}

	@Override
	public Result getResults() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * WMC is calculated with sum of the complexity of methods inside a class. 
	 * For defining the complexity of a method WMC does not put limit on how it’s 
	 * calculated so any metric could be used eg. cyclomatic complexity.
	 */
	public int calculateWMC(){
		
		return 0;
	}
	/**
	 * DIT is the length from parent class on top of the inheritance tree to 
	 * child class that has the largest number of superclasses.
	 * @param parentClassName
	 * @return
	 */
	public int calculateDIT(String parentClassName){
		//
		
		return 0;
	}
	
	public int calculateNOC(String className){
		//NOC is the number of immediate children that the class has.
		return 0;
	}
	/**
	 * RFC is calculated with sum of number of methods in the class and number of 
	 * different methods that are called in, but are not from the class which RFC is 
	 * calculated.
	 */
	public int calculateRFC(){
		return 0;
	}
	
	/**
	 * CBO is the number of dependencies to other classes that the class has. 
     * Class is dependant of another class if it uses any methods or variables 
     * of the other class.
	 * @param className
	 * @return
	 */
	public int calculateCBO(String className){
		return 0;
	}
	/**
	 * LCOM is a number of method pairs with no similarity,|P|, minus number of methods,|Q|. 
	 * Method pairs are considered to be non-similar if they don’t use a single same instance variable.
	 * LCOM = |P| - |Q|, when |P|>|Q|, otherwise LCOM=0.
	 * @param nonSimilarMethods P
	 * @param similarMethods Q
	 * @return
	 */
	public int calculateLCOM(int nonSimilarMethods, int similarMethods){
		int p=Math.abs(nonSimilarMethods);
		int q=Math.abs(similarMethods);
		if(p>q){
			return p-q;
		}else
			return 0;
			
	}

}
