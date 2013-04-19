package profiling;

import cppParser.utils.Log;
import java.util.ArrayList;
import java.util.List;

/*
 * This class can keep track of several timers at same time, eg same timer needs 
 * to add start times before the addStop() is called
 */
public class MultiTimer {
	private List<Long> startTimes=new ArrayList<Long>();
	private List<Long> stopTimes=new ArrayList<Long>();
	
	public synchronized void addStart(){
		startTimes.add(System.currentTimeMillis());
	}
	public synchronized  void addStop(){
		stopTimes.add(System.currentTimeMillis());
	}
	
	public List<Long> getTimes(){
		if(startTimes.size()!=stopTimes.size())
			Log.d("Missing timer values");
		List<Long> times=new ArrayList<Long>(startTimes.size());
		try{
			for(int i=0;i<startTimes.size();i++){
				times.add(stopTimes.get(i)-startTimes.get(i));
			}
		}catch(IndexOutOfBoundsException e){
			Log.d("Missing stop values");
		}
		return times;
	}
}
