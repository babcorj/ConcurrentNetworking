import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class ParallelClient {
	private static int commandTracker = 0;
	private static String command[] = {"hello world", "add,4,5", "sub,4,5", "mul,10,9", "div,256,8",
			"add,3,", "div,4,0", "div,0,4", "nocommand,4,5"};

	public static void main(String[] args) throws Exception {
		int nClients = 20;

		if(args.length > 2){
			usage();
			return;
		}
		else if(args.length == 1 || args.length == 2){
			try{
				nClients = Integer.parseInt(args[0]);
			} catch(NumberFormatException e){
				usage();
				return;
			}
		}
		if(args.length == 2){
			command = new String[1];
			command[0] = args[1];
		}
		// Get the server address from console.
		String serverAddress = "localhost";

		// Make connection and initialize streams
		IndividualClient myClients[] = new IndividualClient[nClients];
		try{
			for(int i=0; i < nClients; i++){
				myClients[i] = new IndividualClient(new Socket(serverAddress, 9898));
			}
		} catch(ConnectException e){
			System.err.println(e);
			System.out.println("Server may not be running");
			System.exit(-1);
		}
		// Send request to server
		try{
			for(int i=0; i < nClients; i++){
				myClients[i].sendRequest();
			}
		} catch(SocketException e){
			System.out.println("Access... Denied! Psshaw!");
		}
		return;
	}
	
	private static void usage(){
		System.out.println("Usage:______________________________________________________");
		System.out.println("ParallelClient nClients serviceCommand");
		System.out.println("ParallelClient nClients (uses default commands)");
		System.out.println("ParallelClient (uses default number of clients and commands)");
		System.out.println("____________________________________________________________");
	}
	
	private static String getCommand(){
		int index = commandTracker++ % command.length;
		return command[index];
	}

	static class IndividualClient{
		Socket mySocket;
		BufferedReader in;
		PrintWriter out;
		
		public IndividualClient(Socket theSocket){
			mySocket = theSocket;
			try {
				in = new BufferedReader(
						new InputStreamReader(mySocket.getInputStream()));
				out = new PrintWriter(mySocket.getOutputStream(), true);
			} catch (IOException e) {
//				e.printStackTrace();
				System.out.println("Could not connect to server");
			}
		}
		
		public void sendRequest() throws SocketException {			
			String response = "";

			try {
				for(int i=0; i<3; i++){
					//System.out.println(in.readLine());
					in.readLine();
				}
				out.println(ParallelClient.getCommand());
				response = in.readLine();
				if (response == null || response.equals("")) {
					System.out.println("client to terminate.");
					System.exit(0);
				}
				in.close();
				out.close();
			} catch (IOException ex) {
//				ex.printStackTrace();
				System.out.println("Access denied");
				System.exit(0);
			}
			System.out.println(response);
			System.out.println("Thanks for playing!\n");
		}
	}
}
