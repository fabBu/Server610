
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

 /**
 * Application principale qui lance les autres processus
 * 
 * @author Maxime Bouchard
 */
public class ServeurDNS {
	
	public static final String ADRESSE_DNS_DEFAUT = "10.162.8.51";
	
	public static void main(String[] args) {
		
		System.out.println("--------------------------------------");
		System.out.println("Ecole de Technologie Superieures (ETS)");
		System.out.println("GTI610 - Reseau de telecommunication");
		System.out.println("      Serveur DNS simplifie");
		System.out.println("--------------------------------------");
		
		if (args.length == 0) {
			System.out.println("Usage: "
					+"[addresse DNS] <Fichier DNS> <TrueFalse/Redirection seulement>");
			System.out.println("Pour lister la table: "
					+"showtable <Fichier DNS>");
			System.out.println("Pour lancer par defaut, tapper : default");
			System.exit(1);
		}
		
		UDPReceiver receiverUDP = new UDPReceiver();
		File f = null;	
		receiverUDP.setPort(53);
		
		/* cas ou l'argument = default
		 Le serveur DNS de redirection par defaut est celui de l'ecole "10.162.8.51" 
		         ====> attention, si vous travaillez ailleurs, pensez a le mettre a jour
		 Le cache dns est le fichier: "DNSFILE.TXT"
		 et la redirection est par defaut a "false" 
		*/
		if(args[0].equals("default")){
			if (args.length <= 1) {
				
				try {
					receiverUDP.setServeurDNS(InetAddress.getByName(ADRESSE_DNS_DEFAUT));
				} catch (UnknownHostException e) {
					System.err.println("Impossible de résoudre l'adresse " + ADRESSE_DNS_DEFAUT);
					e.printStackTrace();
					System.exit(-1);
				}
				
				f = new File("DNSFILE.TXT");
				if(f.exists()){
					receiverUDP.setFichierDNS("DNSFILE.TXT");
				}
				else{
					try {
						f.createNewFile();
						receiverUDP.setFichierDNS("DNSFILE.TXT");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				receiverUDP.setRedirectionSeulement(false);
				
				// et on lance le thread
				receiverUDP.start();
			}
			else{
				System.out.print("L'�x�cution par d�faut n'a pas d'autres arguments");
			}
		}
		else{
			if(args[0].equals("showtable")){ // cas o� l'argument = showtable cacheDNS
				if (args.length == 2) {
					f = new File(args[1]);
					if(f.exists()){
						receiverUDP.setFichierDNS(args[1]);
					}
					else{
						try {
							f.createNewFile();
							receiverUDP.setFichierDNS(args[1]);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					CacheDNS cacheDNS = new CacheDNS(args[1]);
					cacheDNS.listCorrespondingTable();
					cacheDNS.close();
				}
				else if (args.length < 2){
					System.out.println("vous n'avez pas indique le nom du fichier");
				} else {
					System.out.println("vous avez indiquez trop d'arguments");
				}
			}
			else{
				if (args.length == 3) { // cas ou les arguments sont: [IPserveurDNS] [cacheDNS] [redirectionOuNon]
					
					try {
						receiverUDP.setServeurDNS(InetAddress.getByName(args[0]));
					} catch (UnknownHostException e) {
						System.err.println("Impossible de résoudre l'adresse " + args[0]);
						e.printStackTrace();
						System.exit(-1);
					}
					
					f = new File(args[1]);
					if(f.exists()){
						receiverUDP.setFichierDNS(args[1]);
					}	
					else{
						try {
							f.createNewFile();
							receiverUDP.setFichierDNS(args[1]);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if(args[2].equals("false")){
						receiverUDP.setRedirectionSeulement(false);
					}
					else{
						receiverUDP.setRedirectionSeulement(true);
					}
					// et on lance le thread
					receiverUDP.start();
				}
				else
					System.out.println("Un argument est manquant!");
			}
		}
	}	
}

