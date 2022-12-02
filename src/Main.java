public class Main {
	/**
	 * Driver class method.
	 * @param args Not used.
	 * @throws Exception Throws file exception.
	 */
	public static void main(String[] args) throws Exception {
		
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
		System.out.println(wordMap.keySet());
		System.out.println(wordMap.containsValue("receba"));
		
		WordMap<Integer,String> testMap = new WordMap<>(5);
		testMap.put(1, "bg");
		testMap.put(2, "bg");
		testMap.put(3, "bg");
		
		System.out.println(testMap.toString());
	}
	
}
