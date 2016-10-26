import java.io.*;
import java.net.*;

/* TCP Server */

class server {

	public static void main (String argv[]) throws Exception
	{
		String clientSentence;
		String capitalizedSentence;
		String ipClient;
		Boolean condition = true;
		System.out.println("Server");
		
		// Create socket server on port 2016
		ServerSocket socket = new ServerSocket(2016);
		
		while(condition)
		{
			// Wait until there is a connection to the socket
			Socket connectionSocket = socket.accept();
			
			// Retrieve ip and sentence from the client
			BufferedReader inFromClient = new BufferedReader( new InputStreamReader( connectionSocket.getInputStream()));
			clientSentence = inFromClient.readLine();
			ipClient = connectionSocket.getRemoteSocketAddress().toString();

			// Show sentence from the client
            System.out.println("IP: " + ipClient + ", received: " + clientSentence);
            
            // Modify sentence and send it to client
            DataOutputStream outToClient = new DataOutputStream( connectionSocket.getOutputStream());
            capitalizedSentence = clientSentence.toUpperCase() + '\n';
            outToClient.writeBytes(capitalizedSentence);
            
            condition = false;
		}
		
		socket.close();
	}
	
}
