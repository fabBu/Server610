package dns;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Cette classe permet la reception d'un paquet UDP sur le port de reception
 * UDP/DNS. Elle analyse le paquet et extrait le hostname
 * 
 * Il s'agit d'un Thread qui ecoute en permanance pour ne pas affecter le
 * deroulement du programme
 * 
 * 
 * @author Maxime Nadeau (AK83160) - Refactor du code et correction de bugs.
 * @originalAuthor Maxime Bouchard (aj98150)
 */

public class UDPReceiver extends Thread {
	/**
	 * Les champs d'un Packet UDP 
	 * --------------------------
	 * En-tete (12 octects) 
	 * Question : l'adresse demande 
	 * Reponse : l'adresse IP
	 * Autorite : info sur le serveur d'autorite 
	 * Additionnel : information supplementaire
	 */

	/**
	 * Definition de l'En-tete d'un Packet UDP
	 * --------------------------------------- 
	 * 
	 *   0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
	 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 * |                      ID                       |
	 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 * |QR    Opcode  |AA|TC|RD|RA|   Z    |   RCODE   |
	 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 * |                     QDCOUNT                   |
	 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 * |                     ANCOUNT                   |
	 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 * |                     NSCOUNT                   |
	 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 * |                     ARCOUNT                   |
	 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
	 * 
	 * 
	 * L'identifiant est un entier permettant d'identifier la requete. 
	 * parametres contient les champs suivant : 
	 * 		QR (1 bit) : indique si le message est une question (0) ou une reponse (1). 
	 * 		OPCODE (4 bits) : type de la requete (0000 pour une requete simple). 
	 * 		AA (1 bit) : le serveur qui a fourni la reponse a-t-il autorite sur le domaine? 
	 * 		TC (1 bit) : indique si le message est tronque.
	 *		RD (1 bit) : demande d'une requete recursive. 
	 * 		RA (1 bit) : indique que le serveur peut faire une demande recursive. 
	 *		UNUSED, AD, CD (1 bit chacun) : non utilises. 
	 * 		RCODE (4 bits) : code de retour.
	 *                       0 : OK, 1 : erreur sur le format de la requete,
	 *                       2: probleme du serveur, 3 : nom de domaine non trouve (valide seulement si AA), 
	 *                       4 : requete non supportee, 5 : le serveur refuse de repondre (raisons de securite ou autres).
	 * QDCount : nombre de questions. 
	 * ANCount, NSCount, ARCount : nombre d'entrees dans les champs Reponse, Autorite,  Additionnel.
	 */
	
	// La taille maximale d'un paquet IPv4 - À noter que l'application ne supporte pas les jumbogram IPv6
	//		https://en.wikipedia.org/wiki/User_Datagram_Protocol#Packet_structure
	protected final static int TAILLE_IPV4_MAX_THEORIQUE = 65535;
	
	// Le port  de redirection (par defaut)
	protected final static int PORT_REDIRECTION_DEFAUT = 53;
	
	public UDPReceiver() { }
	
	public UDPReceiver(InetAddress serveurDNS, int Port) {
		this.serveurDNS = serveurDNS;
		this.port = Port;
	}

	// Détermine si notre serveur est en cours d'exécution
	private boolean stop = false;

	// Adresse IP du serveur de redirection des requêtes DNS
	protected InetAddress serveurDNS;
	public InetAddress getServeurDNS() { return serveurDNS; }
	public void setServeurDNS(InetAddress serveurDNS) { this.serveurDNS = serveurDNS; }

	// Fichier contenant notre cache d'adresses DNS
	private String fichierDNS;
	public void setFichierDNS(String fichierDNS) { this.fichierDNS = fichierDNS; }

	// Détermine si le serveur est en mode redirection seulement
	private boolean redirectionSeulement = false;
	public void setRedirectionSeulement(boolean redirectionSeulement) { this.redirectionSeulement = redirectionSeulement; }

	// Le port sur lequel notre serveur écoute
	protected int port;
	public void setPort(int port) { this.port = port; }
	
	// L'adresse IP sur laquelle notre serveur écoute
	private String adresseIP = null;
	public String getAdresseIP() { return adresseIP; }
	private void setAdresseIP(String adresseIP) { this.adresseIP = adresseIP; }
	
	// HashMap permettant d'associer un identifiant de requête et sa provenance
	private HashMap<Integer, InfoClient> clients = new HashMap<>();
	
	@Override
	public void run() {
		try {
			// *Creation d'un socket UDP
			DatagramSocket serveur = new DatagramSocket(this.port);
			
			// *Boucle de reception
			while (!this.stop) {
				
				// *Création de notre paquet UDP vide
				byte[] buff = new byte[TAILLE_IPV4_MAX_THEORIQUE];
				DatagramPacket paquetRecu = new DatagramPacket(buff,buff.length);
				System.out.println("Serveur DNS  "+serveur.getLocalAddress()+"  en attente sur le port: "+ serveur.getLocalPort());

				// *Lecture d'un message dans notre file de réception du socket
				serveur.receive(paquetRecu);
				byte[] donneesDNS = new byte[paquetRecu.getLength()];
				System.arraycopy(paquetRecu.getData(), 0, donneesDNS, 0, paquetRecu.getLength());
				
				System.out.println("Paquet recu du " + paquetRecu.getAddress() + " du port: " + paquetRecu.getPort());
				System.out.println("Contenu du paquet : " + Arrays.toString(donneesDNS));
				
				// *Creation d'un DataInputStream pour manipuler les bytes du paquet
				DataInputStream fluxDonneesDNS = new DataInputStream(new ByteArrayInputStream(donneesDNS));
				
				// TODO: Compléter le traitement d'un paquet requête (prevenant de notre client)
				// ****** Si c'est un paquet requete *****

					// *Lecture du Query Domain name, a partir du 13 byte
				
				    // *Sauvegarde des informations client et de l'identifiant de la requete
				    //  dans notre liste des requêtes en cours (variable HashMap "clients")

					// *Si le mode est redirection seulement
						// *Rediriger le paquet vers le serveur DNS
					// *Sinon
						// *Rechercher l'adresse IP associe au Query Domain name
						//  dans le fichier de correspondance de ce serveur					

						// *Si la correspondance n'est pas trouvee
							// *Rediriger le paquet vers le serveur DNS
						// *Sinon	
							// *Creer le paquet de reponse a l'aide du UDPAnswerPaquetCreator
							// *Placer ce paquet dans le socket
							// *Envoyer le paquet
				            // *Retirer la requête de la liste des requêtes en cours

				// TODO: Compléter le traitement d'un paquet de réponse (provenant de notre serveur de redirection)
				// ****** Si c'est un paquet reponse *****
						// *Lecture du Query Domain name, a partir du 13 byte
						
						// *Passe par dessus Type et Class de notre requête
						
						// *Passe par dessus les premiers champs du ressource record
						//  pour arriver au ressource data qui contient l'adresse IP associe
						//  au hostname (dans le fond saut de 16 bytes)
						
						// *Capture de ou des adresse(s) IP (ANCOUNT est le nombre
						//  de rï¿½ponses retournï¿½es)			
					
						// *Ajouter la ou les correspondance(s) dans le fichier DNS
						//  si elles ne y sont pas deja
						
						// *Faire parvenir le paquet reponse au demandeur original,
						//  ayant emis une requete avec cet identifiant				
						// *Placer ce paquet dans le socket
						// *Envoyer le paquet
	                    // *Retirer la requête de la liste des requêtes en cours
			}
			
			// *Fermeture de notre socket pour nettoyer les ressources
			serveur.close();
		} catch (IOException e) {
			System.err.println("Problème à l'exécution :");
			e.printStackTrace(System.err);
		}
	}
}
