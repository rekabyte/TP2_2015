import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BigramsManager {

    private static ArrayList<TLNManager.BigramEntry> bigrams;
    private static WordMap<String, FileMap<String, ArrayList<Integer>>> wordMap;
    private static WordMap<String, Integer> wordsCount;
    private static WordMap<TLNManager.BigramEntry, Integer> bigramsCount;
    private static WordMap<String, String> searchQueries;
    private static WordMap<String, String> probQueries;

    public static void init() {
        bigrams = TLNManager.getBigrams();
        wordMap = TLNManager.getWordMap();
        wordsCount = TLNManager.getWordsCount();
        bigramsCount = TLNManager.getBigramCounts();

        searchQueries = TLNManager.getSearchQueries();
        probQueries = TLNManager.getProbQueries();

        for(String s : probQueries.keySet()) System.out.println(secondLikely(s));

        //System.out.println(TLNManager.queries);
        //for(String s : TLNManager.getWordsPerFile().keySet()) System.out.println(s + "\t" +  TLNManager.getWordsPerFile().get(s));
        //System.out.println("Tout les bigrams:" + bigrams);;
        //System.out.println("Comptage de bigrams:" + bigramsCount);;
    }

    //================= DEVELOPER'S UTILITIES =====================
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

        return "Le mot le plus probable apres: \t" + w1 + " est " + findSmallestString(probabilties.getKeysFromValue(highestProb));
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

}
