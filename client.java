import java.io.*;
import java.net.*;

/* Client TCP */

public class client {

	public static void main(String[] args) throws Exception {
		String sentence;
		String modifiedSentence;
		System.out.println("Client");
		
		// Get sentence from client
		BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
		sentence = inFromUser.readLine();
		
		// Create connection to the server and send the sentence
		Socket clientSocket = new Socket("10.196.115.175", 2016);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.writeBytes(sentence + '\n');
		
		// Get modified sentence from the server
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		modifiedSentence = inFromServer.readLine();
		System.out.println("SERVER: " + modifiedSentence);
		clientSocket.close();
	}

}
