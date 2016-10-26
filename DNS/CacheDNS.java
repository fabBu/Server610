import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * Cette classe est utilise pour gérer la cache DNS.
 * 
 * Elle peut enregistrer une reponse dans le fichier texte et effectuer des recherches par hostname.
 * Si le hostname existe, l'adresse IP est retroune, sinon, l'absence de cette adresse est signale
 * 
 * @author Maxime Nadeau (AK83160) - Ajout de validation, refactor du code et correction de bugs.
 * @originalAuthor Maxime Bouchard (aj98150)
 * @contributor lighta, Simon -  Nettoyer pour eviter les erreurs de manipulation
 */
public class CacheDNS implements Closeable {

	private static final String LINE_SEPARATOR = "\r\n";

	// Cette expression régulière est une version modifiée de celle obtenue
	// sur le site suivant : http://www.regular-expressions.info/examples.html
	private static final Pattern REGEX_ADRESSE_IPV4 = Pattern.compile("^(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?){1})$");
	
	// Chemin du fichier à utiliser
	private String filename;
	
	// Objet servant à parcourir le fichier de cache
	private Scanner scanneurFichierSource;

	/**
	 * Construteur
	 * @param filename : Nom du fichier pour sauvegarder les adressesIP et hostname
	 * 
	 */
	public CacheDNS(String filename){
		
		File fichier = new File(filename);
		
		try {
			if ( (!fichier.exists() && fichier.createNewFile())
					|| (fichier.canWrite() && fichier.canRead())) {
				this.filename = filename;
				this.scanneurFichierSource = new Scanner(new FileReader(filename));
				
				this.scanneurFichierSource.useDelimiter(LINE_SEPARATOR);
				
			} else {
				System.err.println("L'application doit avoir des droits d'accès en lecture et en écriture sur le fichier spécifié.");
				System.exit(-1);
			}
		} catch (IOException e) {
			System.err.println("L'application doit avoir des droits d'accès en lecture et en écriture sur le fichier spécifié.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * @param hostname : NS (ex google.com)
	 * @param adresseIP : Ip resolvant le NS
	 */
	public void startRecord(String hostname,String adresseIP){
		
		Matcher validateurAdresse = REGEX_ADRESSE_IPV4.matcher(adresseIP);
		
		if(!validateurAdresse.matches()) {
			System.out.println("Invalid adresseIP to write ("+adresseIP+")");
			return; 	
		}
		try {
			FileWriter writerFichierSource = new FileWriter(filename,true);		
			writerFichierSource.write(hostname + " " + adresseIP);
			writerFichierSource.write(LINE_SEPARATOR);
			writerFichierSource.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Search un hostname et retourne une ip
	 * @param hostname = adresse dns a chercher
	 */
	public List<String> startResearch(String hostname){
		List<String> adresslist = new ArrayList<>();

		// Remise du Scanner au commencement du fichier
		scanneurFichierSource.reset();
		
		// Test pour savoir si le fichier est vide
		// S'il n'y a pas de ligne après le début du fichier, le fichier est vide
		if(!scanneurFichierSource.hasNextLine()){
			System.out.println("Le fichier DNS est vide");
			scanneurFichierSource.close();
			return adresslist;
		}
		
		// Lecture ligne par ligne
		do {
			String uneligne = scanneurFichierSource.nextLine();
			String[] hostnameFromFile = uneligne.split(" ");
			
			if(hostnameFromFile[0].equals(hostname)){
				adresslist.add(hostnameFromFile[1]);
			}
		} while(scanneurFichierSource.hasNextLine());
		
		return adresslist;
	}
	
	/**
	 * Affiche l'ensemble du contenu du DNSFILE
	 */
	public void listCorrespondingTable(){
		
		// Remise du Scanner au commencement du fichier
		scanneurFichierSource.reset();
		
		if(!scanneurFichierSource.hasNextLine()){
			System.out.println("La table est vide!");
			return;
		}
		
		while(scanneurFichierSource.hasNextLine()){
			System.out.println(scanneurFichierSource.nextLine());
		}
	}

	@Override
	public void close() {
		if (scanneurFichierSource != null) {
			scanneurFichierSource.close();
		}
	}
}