import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;

/**
 * 
 */

/**
 * @author csfaculty
 *
 */
public class Job {
	private Socket socket;
	private int clientNumber;
	private BufferedReader in;
	private PrintWriter out;
	private String receivedCommand;

	public Job(Socket theSocket, int num) throws IOException {
		clientNumber = num;
		socket = theSocket;
    	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        // Send a welcome message to the client.
        if(num == -1){
        	out.println("Access denied: Maximum jobs running");
        } else{
	        out.println("Hello, you are client #" + clientNumber + ".");
	        out.println("Enter a line with only a period to quit\n");
        }
        System.out.println("Job created #"+clientNumber);
	}
	
	public int getClientNumber(){
		return clientNumber;
	}
	
	public String received(){
		return receivedCommand;
	}
	
	public String process(int tid) throws IOException {
		receivedCommand = in.readLine();
		System.out.println("Processing client #"+clientNumber+
				": "+receivedCommand + " from Thread #"+tid);
    	String output = receivedCommand.toUpperCase();

        if(output.equals("KILL")){
            out.println(output);
        	return output;
    	}
        if (output == null || output.equals(".")) {
        	return null;
        }
        if(isAFunction(output)){
        	String result = "";
        	int num1 = getFirstNumber(output),
        	    num2 = getSecondNumber(output);
            switch(output.substring(0,3)){
            case "ADD":
            	result = add(num1,num2);
            	break;
            case "SUB":
            	result = subtract(num1,num2);
            	break;
            case "MUL":
            	result = multiply(num1,num2);
            	break;
            case "DIV":
            	result = divide(num1,num2);
            	break;
            }
            out.println(result);
            return result;
        }
    	out.println(output);
		return output;
	}
	
	public void end(){
		try{
			socket.close();
			in.close();
			out.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private boolean isAFunction(String input){
		if(input.length() < 7){
			return false;
		}
		String mySubstring = input.substring(0,3);
		if(!mySubstring.equals("ADD") && 
				!mySubstring.equals("SUB") &&
				!mySubstring.equals("MUL") &&
				!mySubstring.equals("DIV")){
			return false;
		}
		char comma1 = input.charAt(3);
		if(comma1 != ','){
			return false;
		}
		int i = 4;
		for(; i < input.length(); i++){
			if(Character.isDigit(input.charAt(i)) == false){
				break;
			}
		}
		if(i == input.length() || i == 4) return false;
		char comma2 = input.charAt(i);
		if(comma2 != ',' || i == input.length()-1){
			return false;
		}
		for(++i; i < input.length(); i++){
			if(Character.isDigit(input.charAt(i)) == false){
				return false;
			}
		}
		return true;
	}
	
	private int getFirstNumber(String input){
		int i=3;
		while(Character.isDigit(input.charAt(++i)));
		return Integer.parseInt(input.substring(4,i));
	}
	
	private int getSecondNumber(String input){
		int i=3;
		while(Character.isDigit(input.charAt(++i)));
		return Integer.parseInt(input.substring(i+1,input.length()));
	}
	
	private String add(int num1, int num2){		
		return Integer.toString(num1+num2);
	}
	
	private String subtract(int num1, int num2){
		return Integer.toString(num1-num2);
	}
	
	private String multiply(int num1, int num2){
		return Integer.toString(num1*num2);
	}
	
	private String divide(int num1, int num2){
		if(num2 == 0) return "Cannot divide by zero!";
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format((double) num1/num2);
	}
}
