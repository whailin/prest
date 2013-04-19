package profiling;

import profiling.ProfilingHelper;
import cppParser.utils.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class Stats{
	private static final HashMap<String, StatTracker> stats=new HashMap<String, StatTracker>();
	
	
	
	
	public static synchronized void addTime(String methodName, long time){
		addTime(methodName,(int) time);
	}
	
	public static synchronized void addTime(String methodName, int time){
		StatTracker st=stats.get(methodName);
		if(st==null){
			st=new StatTracker(methodName);
			stats.put(methodName, st);
		}
		st.addTime(time);
		
		
	}
	
	public static synchronized void dumpTimesToLog(){
		Iterator<StatTracker> it=stats.values().iterator();
		StatTracker st;
		while(it.hasNext()){
			st=it.next();
			Log.d(st.getName()+": Min:"+st.getMin()+"ms Average:"+st.getAverage()+"ms Max:"+st.getMax()+"ms"+ "Samples:"+ st.getSamples()+" Total:"+st.getSumOfTimes());
		}
	}
	
	public static synchronized void dumpTimesToFile(FileOutputStream f){

		
		OutputStreamWriter out=new OutputStreamWriter(f);
		Iterator<StatTracker> it=stats.values().iterator();
		StatTracker st;
		try{
			while(it.hasNext()){
				st=it.next();
				String line=st.getName()+": Min:"+st.getMin()+"ms Average:"+st.getAverage()+"ms Max:"+st.getMax()+"ms Samples:"+ st.getSamples();
				out.write((line+"\n"));
				Log.d(line);
			}
			
			
				
		}catch(IOException e){
			Log.d( e.getMessage());
		}finally{
			try{
				out.close();
			}catch(IOException e){}
		}
	}
	
	private static void fetchMultiTimer(MultiTimer t, String name){
		List<Long> times=t.getTimes();
		Iterator<Long> it=times.iterator();
		while(it.hasNext())
			addTime(name, it.next());
	}
	
	

}
