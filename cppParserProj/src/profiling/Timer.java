package profiling;



public class Timer {
	
	long  time;
	
	public void startTimer(){
		time=System.currentTimeMillis();
		
	}
	
	
	public long stopTimer(){	
			long stopTime=System.currentTimeMillis()-time;
			return stopTime;
	}
	
}
