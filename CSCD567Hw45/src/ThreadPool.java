import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;

//and the main server thread
/**
 * 
 */

/**
 * @author Ryan Babcock
 *
 */
public class ThreadPool {	
	private int actualNumberThreads = 5;
	private int workerThreadIDs[] = new int[20];
	private ArrayList<Job> jobsRunning = new ArrayList<>();
	private ArrayList<WorkerThread> holders;
	private boolean stopped = false;
	private SharedQueue<Job> jobQueue;
	private ThreadManager threadManager;
	private char[] clientIDs;

	public ThreadPool(SharedQueue<Job> theMonitor, char[] clientNumbers){
		jobQueue = theMonitor;
		holders = new ArrayList<>();
		clientIDs = clientNumbers;
		startPool();
	}

	public void setThreadManager(ThreadManager manager){
		threadManager = manager;
	}
	
	public void startPool() {
		System.out.println("Starting ThreadPool...");
		for(int i=0; i < workerThreadIDs.length; i++){
			workerThreadIDs[i] = -1;
		}
		int threadSize = actualNumberThreads;
		System.out.print("Starting Thread #");
		WorkerThread newThread = null;
		for(int i=0; i < threadSize; i++){
			newThread = new WorkerThread(getThreadID());
			newThread.start();
			holders.add(newThread);
			System.out.print("\b"+i);
			try{
				Thread.sleep(150);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		System.out.println("\nAll threads started");
	}

	public synchronized void increaseThreadsInPool(int newThreadLength) {
		//double the threads in pool according to threshold
		if(isStopped()) return;
		int threadsToAdd = newThreadLength - holders.size();
		WorkerThread newThread = null;

		for(int i=0; i < threadsToAdd; i++){
			newThread = new WorkerThread(getThreadID());
			newThread.start();
			holders.add(newThread);
		}
//		System.out.println("Threads after increase: " + holders.size()+"\n");
	}

	public synchronized void decreaseThreadsInPool(int newThreadLength) throws IllegalArgumentException {
		//halve the threads in pool according to threshold
		if(isStopped()) return;
		if(newThreadLength >= holders.size()){
			throw new IllegalArgumentException("ThreadPool length is not smaller than new length");
		}
		int threadsToRemove = holders.size() - newThreadLength;
		WorkerThread threadToRemove = null;
		for(int i=0; i < threadsToRemove; i++){
			threadToRemove = holders.remove(0);
			workerThreadIDs[threadToRemove.getID()] = -1;
			threadToRemove.end();
			threadToRemove.interrupt();
		}
//		System.out.println("Threads after decrease: "+holders.size()+"\n");
	}

	public synchronized void stopPool() {
		stopped = true;
		//Stop all threads
		System.out.println("Removing "+holders.size()+" threads...");
		WorkerThread threadToRemove = null;
		while(holders.size() != 0){
			threadToRemove = holders.remove(0);
			threadToRemove.end();
			threadToRemove.interrupt();
		}
		//Stop jobs in jobQueue
		System.out.println("Removing "+jobQueue.size()+" jobs from queue...");
		while(jobQueue.size() != 0){
			jobQueue.dequeue().end();
		}
		//Stop jobs in progress
		System.out.println("Removing "+jobsRunning.size()+" running jobs...");
		while(jobsRunning.size() != 0){
			try{
			jobsRunning.remove(0).end();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		if(holders.size() + jobQueue.size() + jobsRunning.size() == 0){
			System.out.println("All jobs and threads ended successfully at " +
					new Date().toString());
		}
		else {
			System.out.println("Threads remaining = " + holders.size());
			System.out.println("Jobs remaining = " + (jobQueue.size()+jobsRunning.size()));
		}
	}

	public boolean isStopped(){
		return stopped;
	}
	
	private int getThreadID(){
		for(int i=0; i < workerThreadIDs.length; i++){
			if(workerThreadIDs[i] == -1){
				workerThreadIDs[i] = i;
				return i;
			}
		}
		return 99;
	}
	
    private class WorkerThread extends Thread {
        private boolean myStop = false;
        private int id;

        public WorkerThread(int identity){
        	id = identity;
        }
        
        public void run() {
        	Job myJob = null;
        	while(!myStop){
        		try{
        			try{
        				Thread.sleep(2000);
        			} catch(InterruptedException e){
        				continue;
        			}
        			try{
        				myJob = jobQueue.dequeue();
        				clientIDs[myJob.getClientNumber()] = '0';
        			} catch(NoSuchElementException e){ // thrown when thread is waiting in dequeue()
        												// Normal behavior
//        				System.err.println(e);
//        				System.err.print("THREAD #"+id+":");
//        				e.printStackTrace();
        				continue;
        			}
        			jobsRunning.add(myJob);
        			String result = "";
        			try{
        				result = myJob.process(id);
        			} catch(IOException e){
        				continue;
        			}
	        		myJob.end();
	        		jobsRunning.remove(myJob);
	        		if(result.equalsIgnoreCase("KILL")){
	        			threadManager.stopRunning(myJob.getClientNumber());
	        		}
	        		else if(result.equals(null)){
	        			throw new IOException();
	        		}
	        		System.out.println("WorkerThread #"+id+" processed service request '" +
	        			myJob.received() + "' ----> '" + result + "' at " + new Date().toString());
        		} catch(IOException e){
        			e.printStackTrace();
	        		System.out.println("WorkerThread #"+id+" ended service with client #" +
        					myJob.getClientNumber() + " at " + new Date().toString());
	        		continue;
	        	}
        	}//end while
        	System.out.println("Successfully ended thread #"+id+" at " +
        			new Date().toString());
        }
        
        public void end(){
        	myStop = true;
        }
        
        public int getID(){
        	return id;
        }
    }
}
