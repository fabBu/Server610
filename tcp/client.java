package tcp;
import java.io.*;
import java.net.*;


/* Client TCP */

public class client {

	public static void main(String[] args) throws Exception {
		String sentence =null;

		int port = 2016;
		String server = "127.0.0.1";

		PrintWriter toServerWriter = null;
		BufferedReader fromServerReader = null;
		Socket clientSocket =null;
		System.out.println("Initialisation");



		try{
			if(args.length == 1)
				server = args[0];
			else
				System.out.println("L'adresse par défaut sera utilisé(127.0.0.1)");
		}
		catch(NumberFormatException e ){
			System.err.println("Port invalid");
			System.exit(1);
		}
		// Get sentence from client
		BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));

		

		try{
			// Create connection to the server and send the sentence
			clientSocket = new Socket(server, port);
			fromServerReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			toServerWriter = new PrintWriter(clientSocket.getOutputStream(), true);

			System.out.println("Connexion etablie");
		}catch (IOException e ){
			System.out.println(e);
			System.exit(1);
		}

		System.out.print("phrase: ");

		sentence = inFromUser.readLine();
		while (sentence != null){
			toServerWriter.println(sentence);

			System.out.println("Phrase en majuscule ->  "+fromServerReader.readLine() );
			System.out.print("phrase:" );
			sentence = inFromUser.readLine();
		}

		fromServerReader.close();
		inFromUser.close();
		toServerWriter.close();
		clientSocket.close();
		System.exit(1);
	}

}
