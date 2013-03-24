package cppMetrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LOCMetrics implements Metrics {
	private long cmtLines = 0;			//comment lines
	private int logicalLOC = 0;		//logical lines of codes
	private int physicalLOC = 0;		//physical lines of codes
	private int LOC = 0;				//lines of codes
	
	@Override
	public void calculateMetrics() {
		// TODO Auto-generated method stub
	}

	public LOCMetrics(long cmtL, int lloc, int ploc, int loc){
		this.cmtLines = cmtL;
		this.logicalLOC = lloc;
		this.physicalLOC = ploc;
		this.LOC = loc;
	}
	
	public LOCMetrics(String file) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		while (br.readLine()!=null)
			this.LOC ++;
	}
	
	public long getCmtLines() {
		return cmtLines;
	}

	public int getLloc() {
		return logicalLOC;
	}

	public int getPloc() {
		return physicalLOC;
	}

	public int getLoc() {
		return LOC;
	}

	@Override
	public Result getResults() {
		// TODO Auto-generated method stub
		return null;
	}	
		
}
