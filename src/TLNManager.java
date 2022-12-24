import edu.stanford.nlp.ling.CoreLabel;
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

    public static boolean debug = true;

    public static class BigramEntry extends Object{
        String bigram1 = "";
        String bigram2 = "";
        int length = -1;

        public BigramEntry(String bigram1, String bigram2) {
            this.bigram1 = bigram1;
            this.bigram2 = bigram2;
            length = bigram1.length() + bigram2.length();
        }

        @Override
        public boolean equals(Object other) {
            BigramEntry anotherBigramEntry = (BigramEntry) other;
            if(bigram1.equalsIgnoreCase(anotherBigramEntry.getBigram1()) &&
            bigram2.equalsIgnoreCase(anotherBigramEntry.getBigram2())) {
                //System.out.println(anotherBigramEntry + " est pareil que " + this.toString());
                return true;
            }
            //System.out.println(anotherBigramEntry + " est different de " + this.toString());
            return false;
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


                    //TODO inserer le code qui lit le fichier
                    dataSet.add(newFile);
                    fos.close();
                }
            }
            zipEntry = zis.getNextEntry();
        }
        System.out.println("Lecture/ecriture des fichiers termines");
        zis.closeEntry();
        zis.close();

        processDataset(dataSet);
        deleteDirectory(destDir);
    }

    //Traite les textes qui se trouvent dans la datalist, les tokenize et les repartit sur la wordMap et gere egalement les fileMaps
    private static void processDataset(List<File> datasetList) throws FileNotFoundException {
        wordMap = new WordMap<>(20);
        wordsCount = new WordMap<>(5);
        bigrams = new ArrayList<>();
        bigramCounts = new WordMap<BigramEntry, Integer>(10);

        // set up pipeline properties
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,pos,lemma");
        props.setProperty("coref.algorithm", "neural");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        int numFichier = 0;                 //DEBUG
        //For every file in datasetlist
        for (File file : datasetList) {
            Scanner scanner = new Scanner(file);
            String fileContent = "";
            while (scanner.hasNext()) {
                fileContent += scanner.nextLine();
                fileContent += " ";
            }
            scanner.close();


            CoreDocument document = new CoreDocument(fileContent.toLowerCase(Locale.ROOT));
            // annotate the document
            //long start2 = System.currentTimeMillis();
            pipeline.annotate(document);

            int compteur = 1;                                                                   //Compte l'indexe des mots(tok)
            String previousWord = "";

            //Pour chaque tok contenu dans le fichier

            //System.out.println("On commence l'analyse des tok.");
            for (CoreLabel tok : document.tokens()) {
                if (tok.word().matches("'|,|:|.|\"|<|>|=|;|/|[|]|\\{|\\}"))
                    continue;                                                                   //Si le tok actuel est un caractere d'accentuation, on
                //skip cette iteration et on passe au tok suivant.

                //long start = System.nanoTime();

                if (!wordMap.containsKey(tok.lemma())) {                                         //Si la wordMap ne contient pas le mot
                    wordMap.put(tok.lemma(), new FileMap<>(4));                             //On cree le mot dans la wordMap et on cree un fileMap comme valeur
                    wordMap.get(tok.lemma()).put(file.getName(), new ArrayList<>());
                }

                if (!wordMap.get(tok.lemma()).containsKey(file.getName()))                       //Si la wordMap contient le mot mais dans un autre fichier:
                    wordMap.get(tok.lemma()).put(file.getName(), new ArrayList<>());            //Alors on get le mot et on rajoute un nouveau fileMap avec le nom du fichier actuel

                wordMap.get(tok.lemma()).get(file.getName()).add(compteur);                     //On rajoute la position du mot dans la fileMap du mot

                if(compteur != 1) {                                                             //Gere les bigrams:
                    BigramEntry bigram = new BigramEntry(previousWord, tok.lemma());
                    bigrams.add(bigram);
                    if(!bigramCounts.containsKey(bigram))                                       //Gere les counts de bigram
                        bigramCounts.put(bigram, 1);
                    else
                        bigramCounts.put(bigram, bigramCounts.get(bigram) + 1);
                }



                if(!wordsCount.containsKey(tok.lemma()))    wordsCount.put(tok.lemma(), 1);     //Gere les wordsCount:
                else wordsCount.put(tok.lemma(), wordsCount.get(tok.lemma()) + 1);

                previousWord = tok.lemma();

                compteur++;

            }
            //System.out.println("Analyse d'un fichier s'est termine apres: " + (System.currentTimeMillis() - start2) + "ms");
            numFichier++;
            if(debug) System.out.println(file.getName() + " annotated \t" + numFichier +"/"+ datasetList.size());
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

}


























