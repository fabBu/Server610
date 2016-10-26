import java.io.*;
import java.net.*;

/* TCP Server */

class server extends Thread {
	protected Socket clientSocket;

	public static void main (String argv[]) throws Exception
	{
		// Create socket server on port 2016
		int port = 2016;
		ServerSocket socket = new ServerSocket(port);
		
		try {
			while(true)
			{
				System.out.println ("Waiting for connection.....");
				new server(socket.accept());          
			}
		} finally {
			socket.close();
		}
	}
	
	public server ( Socket clientSocket) {
		this.clientSocket = clientSocket;
		start();
	}
	
	public void run(){
		try {
			// Get input and ouput buffer
			BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream()));
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			
			// Show information connection from the client
			System.out.println("Nouvelle connection");
	        System.out.println("IP: " + clientSocket.getInetAddress().toString());
	        System.out.println("Port: " + clientSocket.getPort());
	        
	        // Show sentence from the client and send modified sentence to client
	        String inputLine;
	        while((inputLine = in.readLine())!=null ){
				System.out.println ("Texte recu: " +inputLine);
				out.println( inputLine.toUpperCase());
			}

	        // CLosing all connexions
	        out.close();
	        in.close();
	        clientSocket.close();
	        
		} catch (IOException E) {
			System.err.println("Problem with Communication Server");
	        System.exit(1);
	    }
	}
}
