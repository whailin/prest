package cppMetrics;

public class CyclomaticComplexityMetric implements Metrics {

	@Override
	public void calculateMetrics() {
		// TODO Auto-generated method stub

	}

	@Override
	public Result getResults() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int calculateVGComplexity(int edges, int nodes, int exitNodes){
		return (edges - nodes + exitNodes);
	}
	
	public int calculateVCComplexity(int edges, int nodes, int exitNodes){
		return (edges - nodes + 2*exitNodes);
	}

}
