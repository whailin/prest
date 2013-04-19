package profiling;

public class StatTracker {
	private String name;
	private int samples=0;
	private long sumOfTimes=0;
	private int min=Integer.MAX_VALUE, max=0;
	public StatTracker(String name){
		this.name=name;
	}
	
	public void addTime(int time){
        if(time>0){
			samples++;
			sumOfTimes+=time;
			if(min>time)
				min=time;
			if(max<time)
				max=time;
		}
	}
	
	public String getName(){
		return name;
	}
	public double getAverage(){
		if(samples==0)return 0;
		return (double)sumOfTimes/samples;
	}
	
	public int getMin(){
		return min;
	}

    public long getSumOfTimes() {
        return sumOfTimes;
    }
	
	public int getMax(){
		return max;
	}
	public long getSum(){
		return sumOfTimes;
	}
	
	public int getSamples(){
		return samples;
	}
}
