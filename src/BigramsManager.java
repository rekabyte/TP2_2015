import java.util.ArrayList;

public class BigramsManager {

    private static ArrayList<TLNManager.BigramEntry> bigrams;
    private static ArrayList<TLNManager.BigramEntry> processedBigrams = new ArrayList<>();
    private static WordMap<String, FileMap<String, ArrayList<Integer>>> wordMap;
    private static WordMap<String, Integer> wordsCount;
    private static WordMap<TLNManager.BigramEntry, Integer> bigramsCount;

    public static void init() {
        bigrams = TLNManager.getBigrams();
        wordMap = TLNManager.getWordMap();
        wordsCount = TLNManager.getWordsCount();
        bigramsCount = new WordMap<>(10);
        //countBigrams();
        //likelyBigram("the");
    }

    private static void likelyBigram(String w1) {
        WordMap<String, Float> probabilties = new WordMap<>(4);
        ArrayList<String> secondsWord = new ArrayList<>();

        for(TLNManager.BigramEntry bigram : bigrams) {                      //Trouve tout les seconds mots possibles apers un firstWord
            if(bigram.getBigram1().equalsIgnoreCase(w1)) {
                if(!secondsWord.contains(bigram.getBigram2()))
                    secondsWord.add(bigram.getBigram2());
            }
        }
        System.out.println(secondsWord);
        //{1} = On cherche P(W2|W1) POUR CHAQUE W2 ET P(W2|W1) = C(W1|W2) / C(W1)
        //{2} = DONC ON VA LOOPER TOUT LES W2
        //{3} = POUR CHAQUE ITERATION ON VA TROUVER C(W1|W2) QUI SE TROUVE DANS BIGRAMSCOUNT
        //{4} = DANS LA MEME ITERATION ON VA TROUVER C(W1) AUSSI
        //{5} = DANS LA MEME ITERATION ON CALCULE SA PROBA PUIS ON L'AJOUTE DANS probabilities.
        //{6} = ON SORT DE L'ITERATION,PUIS ON DETERMINE LE MOT AVEC LA PROBA LA PLUS HAUTE PUIS ON LE RETOURNE

        for(String w2 : secondsWord) {                                  //{2}
            int cW1W2 = -1;
            int cW1   = -1;
            for(TLNManager.BigramEntry entry : bigramsCount.keySet()) { //{3}
                if(entry.equals(new TLNManager.BigramEntry(w1,w2))) {
                    cW1W2 = bigramsCount.get(entry);
                    break;
                }
            }
            for(String entry : wordsCount.keySet()) {                   //{4}
                if(entry.equalsIgnoreCase(w1)) {
                    cW1 = wordsCount.get(w1);
                    //System.out.println(cW1);
                    break;
                }
            }

            float currentProbabilty = ((float) cW1W2 / (float) cW1);                        //{5}
            probabilties.put(w2, currentProbabilty);
        }

        //{6}





    }

    private static void countBigrams() {
        ArrayList<TLNManager.BigramEntry> bigramsCopy = new ArrayList<>(bigrams);
        for (TLNManager.BigramEntry entry : bigrams) {      //Parcourt chaque bigram dans bigrams
            if(alreadyProcessed(entry)) {                   //Verifie si le bigram a deja ete analysee, si oui on ne l'analyse pas
                continue;
            }
            processedBigrams.add(entry);                    //Si non, on l'analyse puis on l'ajoute a la liste des deja analysé
            int compteur = 0;                               //Compte la presence d'un bigram dans la liste
            for (TLNManager.BigramEntry entry2 : bigrams) { //Parcourt chaque bigram dans bigram
                if (entry.equals(entry2))
                    compteur++;
            }
            bigramsCount.put(entry, compteur);
        }
        bigrams = bigramsCopy;
    }

    private static boolean alreadyProcessed(TLNManager.BigramEntry entry) {
        for(TLNManager.BigramEntry bigram : processedBigrams)
            if(bigram.equals(entry)) {
                return true;
            }
        return false;
    }

}
