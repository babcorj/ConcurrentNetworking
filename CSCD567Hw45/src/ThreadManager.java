import java.util.Date;

/**
 * 
 */

/**
 * @author Ryan Babcock
 *
 */
public class ThreadManager extends Thread {

	private final int UPDATE_CHECK = 5000,
					  L1 = 0, L2 = 10, L3 = 20, //low threshold values
					  H1 = 10, H2 = 20, H3 = 50,//high threshold values
					  T1 = 5, T2 = 10, T3 = 20; //thread values
	private int lowThreshold, highThreshold;
	private ThreadPool myThreadPool;
	private SharedQueue<Job> jobQueue;
	private boolean stopped = false;
	
	public ThreadManager(SharedQueue<Job> queue, ThreadPool theThreadPool){
		jobQueue = queue;
		myThreadPool = theThreadPool;
		lowThreshold = L1;
		highThreshold = H1;
	}
	
	public void run(){
		System.out.println("Thread Manager running...");
		while(!stopped){
			try{
				synchronized(this){
					wait(UPDATE_CHECK);
//					System.out.println("Thread Manager: Update check...");
				}
			} catch(InterruptedException e){}
			
			if(jobQueue.size() < lowThreshold){
				System.out.println("_____________________________THREAD MANAGER_____________________________");
				lowerThreshold();
			}
			else if(jobQueue.size() >= highThreshold){
				System.out.println("_____________________________THREAD MANAGER_____________________________");
				raiseThreshold();
			}
		}
		myThreadPool.stopPool();
	}

	public void lowerThreshold(){
		int newThreadCount = 0;
		switch(lowThreshold) {
			case L2:
				lowThreshold = L1;
				highThreshold = H1;
				newThreadCount = T1;
				break;
			case L3:
				lowThreshold = L2;
				highThreshold = H2;
				newThreadCount = T2;
		}
		System.out.println("Number of threads after decrease: " + newThreadCount +
				"\nThreadManager halved number of threads at " + new Date().toString() +
				"\n________________________________________________________________________");
		myThreadPool.decreaseThreadsInPool(newThreadCount);
	}
	
	public void raiseThreshold(){
		int newThreadCount = 0;
		switch(lowThreshold) {
			case L1:
				lowThreshold = L2;
				highThreshold = H2;
				newThreadCount = T2;
				break;
			case 10:
				lowThreshold = L3;
				highThreshold = H3;
				newThreadCount = T3;
		}
		myThreadPool.increaseThreadsInPool(newThreadCount);
		System.out.println("Number of threads after increase: " + newThreadCount +
				"\nThreadManager doubled number of threads at " + new Date().toString() +
				"\n________________________________________________________________________");
	}
	
	public void stopRunning(int clientID){
		System.out.println("ThreadManager received kill signal from client #" +
				clientID + " at " + new Date().toString());
		stopped = true;
	}
}
