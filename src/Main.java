import edu.stanford.nlp.ling.Word;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;
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
		long start = System.currentTimeMillis();

		TLNManager.datasetReader("E:\\Eclipse\\TP2_2015\\src\\testset");
		BigramsManager.init();
		System.out.println("Programme s'est termine apres: " + (System.currentTimeMillis() - start) + "ms");
	}

	public static int hashValue(Object key, int capacity) {
		if(key instanceof String)  {						//Si c'est un String, on fait un hash a decalage cyclique
			int h = 0;
			for(int i = 0 ; i < ((String) key).length(); i++) {
				h = (h << 5) | (h >>> 27);
				h += (int)(((String) key).charAt(i));
			}
			return Math.abs(h % capacity);							//On compresse le hash et on le return
		}
		else if(key instanceof TLNManager.BigramEntry) {
			int h = 0;
			for(int i = 0 ; i < ((((TLNManager.BigramEntry) key).getBigram1()).length()); i++) {
				h = (h << 5) | (h >>> 27);
				h += (int)(((String) ((TLNManager.BigramEntry) key).getBigram1()).charAt(i));
			}
			return Math.abs(h % capacity);							//On compresse le hash et on le return
		}
		else {											//Si ce n'est pas un String, on utilise la fct hashCode par defaut de Java
			return key.hashCode() % capacity;
		}
	}

	public static void searchFor(String string) {
		String test = "This article is about the astronomical object. For other uses, see Planet  (disambiguation). " +
				"A planet is a large, rounded astronomical body that is  neither a star nor its remnant. The best available theory of planet";
		int wordPosition = test.toLowerCase().indexOf(string, 0);
		while (wordPosition >= 0) {
			System.out.println("trouvable a la position : " + wordPosition);
			wordPosition = test.toLowerCase().indexOf(string, wordPosition+ string.length());
		}
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
		WordMap<String,String> wordMap = new WordMap<>(3);
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




















