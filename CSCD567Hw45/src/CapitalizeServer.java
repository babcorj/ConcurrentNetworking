import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;

public class CapitalizeServer {

	private final static int MAXJOBS = 50;
	private static PrintWriter out;
	private static char clientIDs[] = new char[MAXJOBS];

	public static void main(String[] args) throws Exception {
		ServerSocket listener = null;
		try{
			listener = new ServerSocket(9898);
			listener.setSoTimeout(250);
		} catch(BindException e){
			System.out.println("Server is already running");
			return;
		}
		System.out.println("The capitalization server is running.");
		initClientIDs();
		SharedQueue<Job> myQueue = new SharedQueue<>(MAXJOBS);
		ThreadPool threadPool = new ThreadPool(myQueue, clientIDs);
		ThreadManager threadManager = new ThreadManager(myQueue, threadPool);
		threadManager.start();
		threadPool.setThreadManager(threadManager);
		Job job = null;
		Socket deniedSocket = null;
		try {
			while (!threadPool.isStopped() && threadManager.isAlive()) {
				if(!myQueue.isFull()){
					try{
						job = new Job(listener.accept(), newClientID());            			
						myQueue.enqueue(job);
					} catch(SocketTimeoutException e){
						continue;
					}
				}
				else{
					try{
						deniedSocket = listener.accept();
					} catch(SocketTimeoutException e){
						//            			e.printStackTrace();
						continue;
					}
					out = new PrintWriter(deniedSocket.getOutputStream(), true);
					// Send a denied access message to the client.
					System.out.println("_________________SERVER________________ ");
					System.out.println("  Denied client: Maximum jobs running   ");
						   out.println("");
						   out.println("");
						   out.println("");		
						   out.println("--- Access denied: Maximum jobs running ---");
					try{
						out.close();
						deniedSocket.close();
						Thread.sleep(100);
					} catch(IOException e){
						e.printStackTrace();
						System.err.println("Failed to close socket from server");
					} catch(InterruptedException ie){
						ie.printStackTrace();
					}
				}
			}
		} finally {
			System.out.println("Server is shutting down...");
			listener.close();
			System.out.println("Successfully closed server socket at " +
					new Date().toString());
		}
	}

	private static void initClientIDs(){
		for(int i=0; i < clientIDs.length; i++){
			clientIDs[i] = '0';
		}
	}

	private synchronized static int newClientID(){
		for(int i=0; i < clientIDs.length; i++){
			if(clientIDs[i] == '0'){
				clientIDs[i] = '1';
				return i;
			}
		}
		return -1;
	}
}