package cppMetrics;

public class LOCMetrics implements Metrics {
	private long cmtLines = 0;			//comment lines
	private long logicalLOC = 0;		//logical lines of codes
	private long physicalLOC = 0;		//physical lines of codes
	private long LOC = 0;				//lines of codes
	
	@Override
	public void calculateMetrics() {
		// TODO Auto-generated method stub
	}

	public LOCMetrics(long cmtL, long lloc, long ploc, long loc){
		this.cmtLines = cmtL;
		this.logicalLOC = lloc;
		this.physicalLOC = ploc;
		this.LOC = loc;
	}
	
	public long getCmtLines() {
		return cmtLines;
	}

	public long getLloc() {
		return logicalLOC;
	}

	public long getPloc() {
		return physicalLOC;
	}

	public long getLoc() {
		return LOC;
	}

	@Override
	public Result getResults() {
		// TODO Auto-generated method stub
		return null;
	}	
		
}
