package dns;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Classe utilitaire pour envoyer des messages UDP a un destinataire.
 * Le destinataire est choisit lors de la creation de l'instance.

 * L'envoi verifie que notre adresse est valide avant d'effectuer le traitement.
 * 
 * @author lighta
 * @contributor Maxime Nadeau (AK83160) - Refactor du code
 */
public class UDPSender  {
	
	// Le Socket utilisé pour l'envoi des messages
	private DatagramSocket SendSocket;

	// Le port spécifié pour la destination
	private int portDestination = 53;
	public int getPortDestination() { return portDestination; }

	// L'adresse IP pour la destination des paquets
	private InetAddress addrDestination;
	public InetAddress getAddrDestination() { return addrDestination; }
	
	/**
	 * Contructor
	 * NB : Si le socket d'envoi n'est pas specifié on essai d'en creer un
	 * 
	 * @param ipDestination Adresse ip ou envoyer le paquet
	 * @param portDestination Port de destination du paquet
	 * @param sendsocket Socket à utiliser pour l'envoi
	 */
	public UDPSender(String ipDestination, int portDestination, DatagramSocket sendsocket){
		try {
			if(sendsocket == null) SendSocket = new DatagramSocket();
			else SendSocket = sendsocket;
			System.out.println("Construction d'un socket d'envoi sur port=" + SendSocket.getLocalPort());
	
			this.portDestination = portDestination;
			
			// Cree l'adresse de destination
			this.addrDestination = InetAddress.getByName(ipDestination);
		} catch (UnknownHostException | SocketException e) {
			System.err.println("Impossible de créer l'utilitaire d'envoi UDP.");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Constructor
	 * NB : Si le socket d'envoi n'est pas specifié on essai dans creer un
	 * 
	 * @param ipDestination Adresse de destination
	 * @param portDestination Port de destination
	 * @param sendsocket Socket à utiliser pour l'envoi
	 */
	public UDPSender(InetAddress ipDestination, int portDestination, DatagramSocket sendsocket) {
		try {
			if(sendsocket == null) SendSocket = new DatagramSocket();
			else SendSocket = sendsocket;
			System.out.println("Construction d'un socket d'envoi sur port="+SendSocket.getLocalPort());

			this.portDestination = portDestination;
			this.addrDestination = ipDestination;
		} catch (SocketException e) {
			System.err.println("Impossible de créer l'utilitaire d'envoi UDP.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Effectue l'envoie du message a la destination specifie.
	 * NB : Ne ferme pas le socket apres l'envoie
	 * 
	 * @param packet : data a envoyer (UDP)
	 * @throws IOException
	 */
	public void SendPacketNow(DatagramPacket packet) 
		throws IOException {
		//Envoi du packet a un serveur dns pour interrogation
		if(SendSocket == null)
			throw new IOException("Invalid Socket for send (null)");
		if(packet == null)
			throw new IOException("Invalid Packet for send (null)");
		
		try {
			//set la destination du packet
			packet.setAddress(addrDestination);
			packet.setPort(portDestination);
			//Envoi le packet
			System.out.println("Sending packet to adr="+addrDestination.getHostAddress()+" port="+portDestination+ "srcport="+SendSocket.getLocalPort());
			SendSocket.send(packet);
		} catch (IOException e) {
			System.err.println("Probleme a l'execution :");
			e.printStackTrace();
		}
	}
}
