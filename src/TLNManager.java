import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TLNManager {

    private static ArrayList<File> dataSet;
    private static WordMap<String, FileMap<String, ArrayList<Integer>>> wordMap;
    private static WordMap<String, Integer> wordsCount;
    private static ArrayList<BigramEntry> bigrams;
    private static WordMap<BigramEntry, Integer> bigramCounts;
    private static WordMap<String, Integer> wordsPerFile;
    public static WordMap<String, String> queries;
    private static WordMap<String, String> searchQueries;
    private static WordMap<String, String> probQueries;

    private static boolean debug;

    public static class BigramEntry {
        String bigram1;
        String bigram2;
        int length;

        public BigramEntry(String bigram1, String bigram2) {
            this.bigram1 = bigram1;
            this.bigram2 = bigram2;
            length = bigram1.length() + bigram2.length();
        }

        @Override
        public boolean equals(Object other) {
            BigramEntry anotherBigramEntry = (BigramEntry) other;

            return bigram1.equalsIgnoreCase(anotherBigramEntry.getBigram1()) &&
                    bigram2.equalsIgnoreCase(anotherBigramEntry.getBigram2());
        }

        public String getBigram1() {
            return bigram1;
        }

        public String getBigram2() {
            return bigram2;
        }

        public String toString() { return bigram1 + " - " + bigram2;}
    }

    //Ouvre le dataset et le decompresse, les stockes temporairement sur l'ordinateur, les analyses(TLN), puis les supprimes.
    public static void datasetReader(String filePath) throws IOException {
        File destDir = new File("src/temp");
        dataSet = new ArrayList<>();
        debug = Main.debug;

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            if (isValidFile(zipEntry.getName())) {
                //System.out.println(zipEntry.getName());
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    //fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    //write file content
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);

                    dataSet.add(newFile);
                    fos.close();
                }
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();

        processDataset(dataSet);
        deleteDirectory(destDir);
    }

    //Traite les textes qui se trouvent dans la datalist, les tokenize et les repartit sur la wordMap et gere egalement les fileMaps
    private static void processDataset(List<File> datasetList) throws FileNotFoundException {
        bigrams = new ArrayList<>();
        wordMap = new WordMap<>(20);
        wordsCount = new WordMap<>(5);
        bigramCounts = new WordMap<>(10);
        wordsPerFile = new WordMap<>(10);

        queries = new WordMap<>(4);
        searchQueries = Main.getSearchQueries();
        probQueries = Main.getProbQueries();
        queries.putAll(searchQueries);
        queries.putAll(probQueries);

        // set up pipeline properties
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,pos,lemma");
        props.setProperty("coref.algorithm", "neural");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //This piece of code annotate the query search
        //If the query says "search was", it becomes : "search be"
        WordMap<String,String> newMap = new WordMap<>(2);
        for(String query : searchQueries.keySet()) {
            CoreDocument newDoc = new CoreDocument(query);
            pipeline.annotate(newDoc);
            newMap.put(newDoc.tokens().get(0).lemma(), searchQueries.get(query));
        }
        searchQueries = newMap;
        //System.out.println("Ensemble des search queries apres annotations:\t"+searchQueries);

        newMap = new WordMap<>(2);
        for(String query : probQueries.keySet()) {
            CoreDocument newDoc = new CoreDocument(query);
            pipeline.annotate(newDoc);
            newMap.put(newDoc.tokens().get(0).lemma(), probQueries.get(query));
        }
        searchQueries = newMap;
        //if(debug) System.out.println("Ensemble des prob queries apres annotations:\t"+searchQueries);





        for(String query : queries.keySet())                 //Pour chaque mot (il peut etre soit search ou prob)
        {
            int numFichier = 0;                 //DEBUG
            //For every file in datasetlist
            for (File file : datasetList) {
                //Reading the current File:
                Scanner scanner = new Scanner(file);
                StringBuilder fileContent = new StringBuilder();
                while (scanner.hasNext()) {
                    fileContent.append(scanner.nextLine()).append(" ");
                }
                scanner.close();
                CoreDocument document = new CoreDocument(fileContent.toString().toLowerCase(Locale.ROOT));
                // annotate the document
                pipeline.annotate(document);

                int compteur = 1;                                                                   //Compte l'indexe des mots(tok)
                String previousWord = "";

                //Pour chaque tok contenu dans le fichier
                for (CoreLabel tok : document.tokens()) {
                    //Si le tok actuel est un caractere d'accentuation, on skip cette iteration et on passe au tok suivant.
                    if (tok.word().matches("'|,|:|.|\"|<|>|=|;|/|[|]|\\{|}"))
                        continue;


                    //System.out.println(prob);
                    if (!wordMap.containsKey(tok.lemma())) {                                         //Si la wordMap ne contient pas le mot
                        wordMap.put(tok.lemma(), new FileMap<>(4));                             //On cree le mot dans la wordMap et on cree un fileMap comme valeur
                        wordMap.get(tok.lemma()).put(file.getName(), new ArrayList<>());
                    }

                    if (!wordMap.get(tok.lemma()).containsKey(file.getName()))                       //Si la wordMap contient le mot mais dans un autre fichier:
                        wordMap.get(tok.lemma()).put(file.getName(), new ArrayList<>());            //Alors on get le mot et on rajoute un nouveau fileMap avec le nom du fichier actuel

                    wordMap.get(tok.lemma()).get(file.getName()).add(compteur);                     //On rajoute la position du mot dans la fileMap du mot

                    //=======S'execute uniquement si le mot cherche equivaut a query + on cherche sa probabilité========:
                    //Compte les bigrams uniquement si on en a besoin (present dans probQueries)
                    if(previousWord.equalsIgnoreCase(query)
                            && queries.get(query).equalsIgnoreCase("probable")) {
                        if (compteur != 1) {                                                             //Gere les bigrams:
                            BigramEntry bigram = new BigramEntry(previousWord, tok.lemma());
                            bigrams.add(bigram);
                            if (!bigramCounts.containsKey(bigram))                                       //Gere les counts de bigram
                                bigramCounts.put(bigram, 1);
                            else
                                bigramCounts.put(bigram, bigramCounts.get(bigram) + 1);
                        }
                   }

                    if (!wordsCount.containsKey(tok.lemma())) wordsCount.put(tok.lemma(), 1);     //Gere les wordsCount:
                    else wordsCount.put(tok.lemma(), wordsCount.get(tok.lemma()) + 1);

                    previousWord = tok.lemma();
                    compteur++;
                }
                wordsPerFile.put(file.getName(), compteur - 1);

                numFichier++;//DEBUG
                if (debug)
                    System.out.println(file.getName() + " annotated \t" + numFichier + "/" + datasetList.size());//DEBUG
            }
        }
    }

    //Methode qui limiter la creation de fichiers au dossier actuel
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    //Permet de supprimer le dossier specifié comme argument
    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    //Retourne true si le fichier est un fichier texte et ne contient pas le prefixe "._" ni ".DS_store"
    //(qui est un double des fichiers originaux generee par MacOS automatiquement pour chaque fichier cree
    //sur leur plateforme.)
    //Exemple: Un utilisateur Mac cree un dossier test, pour lui c'est un dossier normal
    //mais pour un Windows user c'est un dossié compressé contenant deux dossier a l'interieur : test (qui contient les fichiers originaux)
    //et un autre dossier caché qui s'appelle __MACOSX et qui contient la copie de tout les fichiers presents dans le premier dossier test
    private static boolean isValidFile(String fileName) {
        return fileName.endsWith(".txt") && !fileName.contains("._") && !fileName.contains(".DS_store");
    }

    //============  GETTERS ================
    public static ArrayList<File> getData() {
        return dataSet;
    }

    public static WordMap<String, FileMap<String, ArrayList<Integer>>> getWordMap() {
        return wordMap;
    }

    public static ArrayList<BigramEntry> getBigrams() {
        return bigrams;
    }

    public static WordMap<String, Integer> getWordsCount() {
        return wordsCount;
    }

    public static WordMap<BigramEntry, Integer> getBigramCounts() {
        return bigramCounts;
    }

    public static WordMap<String, Integer> getWordsPerFile() {
        return wordsPerFile;
    }

    public static WordMap<String, String> getSearchQueries() {
        return searchQueries;
    }

    public static WordMap<String, String> getProbQueries() {
        return probQueries;
    }
}