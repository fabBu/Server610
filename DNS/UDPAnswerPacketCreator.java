package dns;
import java.util.List;

/**
 * Classe utilitaire pour creer un packet de reponse pour DNS en utilisant le protocol UDP.
 * Cette classe s'occupe de formater le data selon la specification du protocole.
 * 
 * @author lighta, Simon
 * @contributor Maxime Nadeau (AK83160) - Refactor du code
 */
public class UDPAnswerPacketCreator {
	
	/** Holder (thread safe) */
	private static class SingletonHolder
	{	
		/** Instance unique non préinitialisée */
		private final static UDPAnswerPacketCreator instance = new UDPAnswerPacketCreator();
	}
	 
	/** Point d'accès pour l'instance unique du singleton */
	public static UDPAnswerPacketCreator getInstance()
	{
		return SingletonHolder.instance;
	}
	
	/**
	 * 
	 * @param Qpacket Datagrame packet de la query DNS
	 * @param listadrr Adresse IP (v4) a transmettre comme reponse
	 * @return tableau de bytes donnant un packet de reponse DNS
	 */
	public byte[] createAnswerPacket(byte[] Qpacket,List<String> listadrr){
		int ancount = listadrr.size();
		if(ancount == 0){
			System.out.println("No adresse to search exiting");
			return null;
		}
		System.out.println("Preparing packet for len="+ancount);
		
		//System.out.println("Le packet QUERY recu");
		
		for(int i = 0;i < Qpacket.length;i++){
			if(i%16 == 0){
				//System.out.println("\r");
			}
			//System.out.print(Integer.toHexString(Qpacket[i] & 0xff).toString() + " ");
		}
		//System.out.println("\r");
		
		//copie les informations dans un tableau qui est utilise de buffer
		//durant la modification du packet
		byte[] tmp_packet = new byte[1024];
		System.arraycopy(Qpacket, 0, tmp_packet, 0, Qpacket.length);
		
		//copie de l'identifiant
		tmp_packet[0] = (byte)Qpacket[0];
		tmp_packet[1] = (byte)Qpacket[1];
		
		//modification des parametres
		//Active le champ reponse dans l'en-tete
		tmp_packet[2] = (byte) 0x81; //QR+opcode+AA+TC+RD
		tmp_packet[3] = (byte) 0x80; //RA+Z+RCODE
		tmp_packet[4] = (byte) 0x00; //Qcount & 0xFF00
		tmp_packet[5] = (byte) 0x01; //Qcount & 0x00FF
		
		tmp_packet[6] = ((byte) ((ancount&(0xFF00)) >>8) ); //Ancount & 0xFF00
		tmp_packet[7] = (byte) ((ancount&(0x00FF)) ); //Ancount & 0x00FF
		
		//Serveur authority --> 0 il n'y a pas de serveur d'autorite
		tmp_packet[8] = (byte) 0x00; //NScount & 0xFF00
		tmp_packet[9] = (byte) 0x00; //NScount & 0x00FF
		
		tmp_packet[10] = (byte) 0x00; //ARCOUNT & 0xFF00
		tmp_packet[11] = (byte) 0x00; //ARcount & 0x00FF

		//Lecture de l'hostname
		//ici comme on ne connait pas la grandeur que occupe le nom de domaine
		//nous devons rechercher l'index pour pouvoir placer l'adresse IP au bon endroit
		//dans le packet
		
		String hostName = "";
		int index = 12, len;
		
		//lire qname
		while ((len = (int)tmp_packet[index]) != 0) {
			//System.out.println("len=" + len);
			for (int i = 1; i <= len; i++) {
				hostName += (char)(tmp_packet[index+i]);
			}
			hostName = hostName + ".";
			index += len+1;
		}
		//System.out.println("hostname found="+hostName);
		//tmp_packet[index] = 0; //last index is 0 and mark end of qname

		//Identification de la class
		//type
		tmp_packet[index + 1] = (byte)0x00; //Qtype  & 0xFF00
		tmp_packet[index + 2] = (byte)0x01; //Qtype  & 0x00FF
		//class
		tmp_packet[index + 3] = (byte)0x00; //Qclass  & 0xFF00
		tmp_packet[index + 4] = (byte)0x01; //Qclass  & 0x00FF
		
		
		//Champ reponse
		int i, lenanswer=16;
		int j=index + 5;
		for(i=0; i<ancount; i++){
			//name offset !TODO whaaaat ?
			tmp_packet[j] = (byte) (0xC0); //name  & 0xFF00
			tmp_packet[j + 1] = (byte) (0x0C); //name  & 0x00FF
			
			tmp_packet[j + 2] = (byte) (0x00); //type  & 0xFF00
			tmp_packet[j + 3] = (byte) 0x01;	//type  & 0x00FF
			
			
			tmp_packet[j + 4] = (byte) 0x00; //class  & 0xFF00
			tmp_packet[j + 5] = (byte) 0x01; //class & 0x00FF
			
			//TTL
			tmp_packet[j + 6] = (byte) 0x00;
			tmp_packet[j + 7] = (byte) 0x01;
			tmp_packet[j + 8] = (byte) 0x1a;
			tmp_packet[j + 9] = (byte) (0x6c);
			
			
			//Grace a l'index de position, nous somme en mesure
			//de faire l'injection de l'adresse IP dans le packet
			//et ce au bon endroit
			tmp_packet[j + 10] = (byte) (0x00); //RDLENGHT & 0xFF00
			tmp_packet[j + 11] = (byte) 0x04;//taille RDLENGHT 0x00FF
			
			//Conversion de l'adresse IP de String en byte
			String adrr = listadrr.get(i);
			//System.out.println("Adr to transmit="+adrr);
			adrr = adrr.replace("."," ");
			String[] adr = adrr.split(" ");
			byte part1 = (byte)(Integer.parseInt(adr[0]) & 0xff);
			byte part2 = (byte)(Integer.parseInt(adr[1]) & 0xff);
			byte part3 = (byte)(Integer.parseInt(adr[2]) & 0xff);
			byte part4 = (byte)(Integer.parseInt(adr[3]) & 0xff);
			
			//IP RDATA
			tmp_packet[j + 12] = (byte) toUnsignedByte(part1);
			tmp_packet[j + 13] = (byte) toUnsignedByte(part2);
			tmp_packet[j + 14] = (byte) toUnsignedByte(part3);
			tmp_packet[j + 15] = (byte) toUnsignedByte(part4);
			j+=lenanswer;
		}
		
		byte[] paquetReponse = new byte[j];
		
		// Utilisation d'un "Padding" pour remplir le reste du buffer
		for(i = 0; i < j; i++){
			paquetReponse[i] = (byte) tmp_packet[i];
		}
		
		return paquetReponse;
	}
	
	private int toUnsignedByte(int data){
		int tmp=0;
		if( (data&(0x80))==(0x80) )
			tmp=(data&(0x7F))+128;
		else
			tmp=data;
		return tmp;
	}
}