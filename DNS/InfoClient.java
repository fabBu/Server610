import java.net.InetAddress;

/*
 * Informations d'un client de notre DNS.
 * 
 * @author Maxime Nadeau (AK83160) - Refactor du code.
 * @originalAuthor Maxime Bouchard (aj98150)
 */
public class InfoClient {
	private int port;
	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }
	
	public InetAddress adresse;
	public InetAddress getAdresse() { return adresse; }
	public void setAdresse(InetAddress adresse) { this.adresse = adresse; }
}
