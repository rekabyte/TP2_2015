import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
//import edu.stanford.nlp.ling.*;
//import edu.stanford.nlp.pipeline.*;
import java.util.Properties;

public class Main {
	/**
	 * Driver class method.
	 * @param args Not used.
	 * @throws Exception Throws file exception.
	 */
	public static void main(String[] args) throws Exception {

		TLNManager.datasetReader("E:\\Eclipse\\TP2_2015\\src\\dataset");
	}

	public static void tlnTest() {
		String text = "Joe Smith wasn't born in California. " +
				"In 2017, he went to his car, sister's car Paris, France in the summer. " +
				"His flight left at 3:00pm on July 10th, 2017. " +
				"After eating some escargot for the first time, Joe said, \"That was delicious!\" " +

				"He sent a postcard to his sister Jane Smith. " +
				"After hearing about Joe's trip, Jane decided she might go to France one day.";

		// set up pipeline properties
		Properties props = new Properties();
		// set the list of annotators to run
		props.setProperty("annotators", "tokenize,pos,lemma");
		// set a property for an annotator, in this case the coref annotator is being
		// set to use the neural algorithm
		props.setProperty("coref.algorithm", "neural");
		// build pipeline
//		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		// create a document object
	//	CoreDocument document = new CoreDocument(text);
		// annnotate the document
	//	pipeline.annotate(document);
		//System.out.println(document.tokens());
	//	for (CoreLabel tok : document.tokens()) {
	///		System.out.println(String.format("%s\t%s", tok.word(), tok.lemma()));
	//	}


	}
	
	public static void fileTests() {
		
	}
	
	public static void printFileContent(String filePath) {
		
		FileInputStream fs = null;
		ZipInputStream zs = null;
		ZipEntry ze = null;
		
		try {
			System.out.println("Files in the zip are as follows: ");
			fs = new FileInputStream(filePath);
			zs = new ZipInputStream(new BufferedInputStream(fs));
			
			while((ze = zs.getNextEntry()) != null) {
				//System.out.println(ze.getName());
				String name = ze.getName();
				if(name.endsWith(".txt"))
						System.out.println(name);
			}
			zs.close();
			fs.close();
		} catch(FileNotFoundException fe) { fe.printStackTrace();
		} catch(IOException ie)			  { ie.printStackTrace();
		}
	}
	
	//Tests unitaires pour verifier que tout fonctionne bien
	public static void mapTest() {
		WordMap<String,String> wordMap = new WordMap<>(10);
		wordMap.put("un", "abc");
		wordMap.put("deux", "suii");
		wordMap.put("rtois", "receba");
		wordMap.put("quatro", "bomboleo");
		wordMap.put("khmsa", "bombolea");
		
		System.out.println(wordMap.toString());
		//System.out.println(wordMap.keySet());
		//System.out.println(wordMap.containsValue("receba"));
		
		WordMap<Integer,String> testMap = new WordMap<>(5);
		testMap.put(1, "bg");
		testMap.put(2, "bg");
		testMap.put(3, "bg");
		
		System.out.println(testMap.toString());
	}
	
}




















