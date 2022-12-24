import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


public class Main {

	public static String query;
	public static boolean debug;
	private static WordMap<String, String> searchQueries;
	private static WordMap<String, String> probQueries;
	private static ArrayList<String[]> queriesToBeExecuted;
	private static String dataSetPath;

	public static void main(String[] args) throws Exception {
		dataSetPath = new String("");
		long start = System.currentTimeMillis();													//DEBUG
		if(args.length > 0) dataSetPath = args[0];													//DEBUG
		if(args.length > 1) query = args[1];														//DEBUG
		else System.out.println("Usage: java Main <dataset> <query.txt> [optional: -debug]");		//DEBUG
		if(args.length > 2) debug = args[2].equalsIgnoreCase("-debug");					//DEBUG

		readQuery(query);
		TLNManager.datasetReader(dataSetPath);
		BigramsManager.init();

		//If debug is true:
		if(debug) System.out.println("Programme s'est termine apres: " + (System.currentTimeMillis() - start) + "ms");
	}

	public static WordMap<String,String> getSearchQueries() { return searchQueries;}
	public static WordMap<String,String> getProbQueries() { return probQueries;}
	public static ArrayList<String[]> getQueriesToBeExecuted() {
		return queriesToBeExecuted;
	}
	public static void setQueriesToBeExecuted(ArrayList<String[]> queriesToBeExecuted) {
		Main.queriesToBeExecuted = queriesToBeExecuted;
	}

	private static void readQuery(String filePath) {
		searchQueries = new WordMap<>(4);
		probQueries = new WordMap<>(4);
		queriesToBeExecuted = new ArrayList<>();
		try{
			File query = new File(filePath);
			Scanner scanner = new Scanner(query);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.toLowerCase().contains("search")) {
					String value = line.split(" ")[line.split(" ").length - 1];
					searchQueries.put(value.toLowerCase(), "search");

					String[] currentQuery = {"search", value};
					queriesToBeExecuted.add(currentQuery);

					if(debug) System.out.println("We're looking in which file we find:\t" + value);
					continue;
				}
				else if(line.toLowerCase().contains("the most probable bigram of")) {
					String value = line.split(" ")[line.split(" ").length - 1];
					probQueries.put(value.toLowerCase(), "probable");

					String[] currentQuery = {"probable", value};
					queriesToBeExecuted.add(currentQuery);

					if(debug) System.out.println("We're looking for most likely word after:\t" + value);
					continue;
				}
				System.out.println("Rien trouve dans le fichier query");
				scanner.nextLine();
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Fichier "+query+" introuvable.");
		}
	}

}