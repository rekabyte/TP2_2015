import java.io.*;
import java.util.Locale;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Properties;


public class Main {
	/**
	 * Driver class method.
	 * @param args Not used.
	 * @throws Exception Throws file exception.
	 */

	public static String query;
	public static boolean debug;
	private static WordMap<String, String> searchQueries;
	private static WordMap<String, String> probQueries;

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();													//DEBUG
		if(args.length > 0) query = args[0];														//DEBUG
		else System.out.println("Usage: java Main <dataset> <query.txt> [optional: -debug]");		//DEBUG
		if(args.length > 1) debug= args[1].equalsIgnoreCase("-debug") ? true : false;	//DEBUG

		readQuery(query);

		TLNManager.datasetReader("E:\\Eclipse\\TP2_2015\\src\\dataset2");
		BigramsManager.init();
		System.out.println("Programme s'est termine apres: " + (System.currentTimeMillis() - start) + "ms");

	}

	public static WordMap<String,String> getSearchQueries() { return searchQueries;}
	public static WordMap<String,String> getProbQueries() { return probQueries;}

	private static void readQuery(String filePath) {
		searchQueries = new WordMap<>(4);
		probQueries = new WordMap<>(4);
		try{
			File query = new File(filePath);
			Scanner scanner = new Scanner(query);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.toLowerCase().contains("search")) {
					String value = line.split(" ")[line.split(" ").length - 1];
					searchQueries.put(value.toLowerCase(), "search");
					//System.out.println("We're looking for: " + value);
					continue;
				}
				else if(line.toLowerCase().contains("the most probable bigram of")) {
					String value = line.split(" ")[line.split(" ").length - 1];
					probQueries.put(value.toLowerCase(), "probable");
					//System.out.println("Most likely word after " + value);
					continue;
				}
				System.out.println("Rien trouve dans le fichier query");
				scanner.nextLine();
			}
			scanner.close();
			if(debug) {
				System.out.println("Ensemble des search queries: " + searchQueries);
				System.out.println("Ensemble des proba queries: " + probQueries);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Fichier "+query+" introuvable.");
		}
	}

}




















