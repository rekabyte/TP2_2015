import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BigramsManager {

    private static ArrayList<TLNManager.BigramEntry> bigrams;
    private static WordMap<String, FileMap<String, ArrayList<Integer>>> wordMap;
    private static WordMap<String, Integer> wordsCount;
    private static WordMap<TLNManager.BigramEntry, Integer> bigramsCount;

    public static void init() {
        bigrams = TLNManager.getBigrams();
        wordMap = TLNManager.getWordMap();
        wordsCount = TLNManager.getWordsCount();
        bigramsCount = TLNManager.getBigramCounts();

        processQuery();
    }

    //================= DEVELOPER'S UTILITIES =====================
    private static void processQuery() {
        for(String[] query : Main.getQueriesToBeExecuted()) {
            if(query[0].equalsIgnoreCase("search"))  {
                System.out.println(tfidf(query[1]));
                continue;
            }
            else if(query[0].equalsIgnoreCase("probable")) {
                System.out.println(query[1] + " " + secondLikely(query[1]) );
                continue;
            }
            System.out.println("Erreur ligne 42 BigramsManager.java");
        }
    }

    private static String secondLikely(String w1) {
        WordMap<String, Float> probabilties = new WordMap<>(4);
        ArrayList<String> secondsWord = new ArrayList<>();

        for (TLNManager.BigramEntry bigram : bigrams) {                      //Trouve tout les seconds mots possibles apers un firstWord
            if (bigram.getBigram1().equalsIgnoreCase(w1)) {
                if (!secondsWord.contains(bigram.getBigram2()))
                    secondsWord.add(bigram.getBigram2());
            }
        }
        //System.out.println("Les deuxiemes mots trouvables apres \"" + w1 + "\" sont " + secondsWord);
        //{1} = On cherche P(W2|W1) POUR CHAQUE W2 ET P(W2|W1) = C(W1|W2) / C(W1)
        //{2} = DONC ON VA LOOPER TOUT LES W2
        //{3} = POUR CHAQUE ITERATION ON VA TROUVER C(W1|W2) QUI SE TROUVE DANS BIGRAMSCOUNT
        //{4} = DANS LA MEME ITERATION ON VA TROUVER C(W1) AUSSI
        //{5} = DANS LA MEME ITERATION ON CALCULE SA PROBA PUIS ON L'AJOUTE DANS probabilities.
        //{6} = ON SORT DE L'ITERATION,PUIS ON DETERMINE LE MOT AVEC LA PROBA LA PLUS HAUTE PUIS ON LE RETOURNE

        for (String w2 : secondsWord) {                                                     //{2}
            int cW1W2 = bigramsCount.get(new TLNManager.BigramEntry(w1, w2));               //{3}
            int cW1 = wordsCount.get(w1);                                                   //{4}

            float currentProbabilty = ((float) cW1W2 / (float) cW1);                        //{5}
            probabilties.put(w2, currentProbabilty);
        }
        //System.out.println(probabilties);
        float highestProb = findMax(probabilties.values());                                 //{6}

        return findSmallestString(probabilties.getKeysFromValue(highestProb));
    }

    private static float findMax(Collection<Float> numbers) {
        ArrayList<Float> numbersList = (ArrayList<Float>) numbers;
        float max = -1;
        for (int i = 0; i < numbers.size(); i++) {
            max = Math.max(numbersList.get(i), max);
        }
        return max;
    }

    private static String findSmallestString(ArrayList<String> words) {
        Collections.sort(words);
        if(words.size() !=0) return words.get(0);
        return null;
    }

    //Compte combien de fois un mot est present dans un fichier specifique
    private static int countSpecificWord(String word, String fileName) {
        int count = -1;
        if(wordMap.containsKey(word)) {
            FileMap<String, ArrayList<Integer>> fileMap = wordMap.get(word);
            count = fileMap.get(fileName).size();
            //System.out.println("Le mot:\t" + word + " se trouve " + count + " fois dans le fichier: " + fileName);
        }
        return count;
    }

    //Compte combien de fois un mot est present dans chaque fichier:
    private static WordMap<String, Integer> countFilesByWord(String word) {
        WordMap<String, Integer> result = new WordMap<>(2);
        int count = -1;
        if(wordMap.containsKey(word)) {
            count = wordMap.get(word).size();
            for(String fileName : wordMap.get(word).keySet()) {
                result.put(fileName, countSpecificWord(word, fileName));
            }
        }
        return result;
    }

    private static String tfidf(String word) {
        /*
        POUR CHAQUE DOCUMENT OU IL Y A WORD:
        ON CHERCHE A CALCULER LE TFIDF
        NOUS CALCULERONS D'ABORD: TF(WORD) = COUNT(W) / TOTAL(W)
           COUNT(W) = NBRE DE FOIS QU'APPARAIT WORD DANS UN DOCUMENT==>countSpecificWord()
           TOTAL(W) = LONGUEUR DU DOCUMENT EN MOTS                  ==>wordsPerFile
        PUIS NOUS CALCULERONS IDF(W) = LN(TOTALd/COUNT(d,w))
            TOTALd      = NBRE TOTAL DE DOCUMENTS CONSIDERES        ==>nbreFichiers
            COUNT(d,w)  = NBRE DE DOCUMENTS CONTENANT WORD          ==>countFilesByWord
        PUIS TFIDF(WORD)    =   TF(W) * IDF(W)
         */

        WordMap<String, Float> tfidfValues = new WordMap<>(2);

        float tf   = 0.0f;
        float idf  = 0.0f;
        float tfidf= 0.0f;



        for(String fileName : countFilesByWord(word).keySet()) {        //Pour chaque fichier ou se trouve le mot Word:
            tf = ((float) countSpecificWord(word, fileName) / (float) TLNManager.getWordsPerFile().get(fileName));
            idf = (float) Math.log((float)TLNManager.getNbreFichiers() / (float)(countFilesByWord(word).size()));
            tfidf = tf * idf;
            tfidfValues.put(fileName, tfidf);
        }

        return findSmallestString(tfidfValues.getKeysFromValue(findMax(tfidfValues.values())));

    }

}
