import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TLNManager {

    public static ArrayList<File> dataSet;
    public static WordMap<String, FileMap<String, ArrayList<Integer>>> wordMap;
    public static ArrayList<String[]> bigrams = new ArrayList<String[]>();

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

        zis.closeEntry();
        zis.close();

        processDataset(dataSet);
        deleteDirectory(destDir);
    }

    //Traite les textes qui se trouvent dans la datalist
    private static void processDataset(List<File> datasetList) throws FileNotFoundException {
        wordMap = new WordMap<>(20);

        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,pos,lemma");
        // set a property for an annotator, in this case the coref annotator is being
        // set to use the neural algorithm
        props.setProperty("coref.algorithm", "neural");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object

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
            pipeline.annotate(document);

            int compteur = 1;                                                                   //Compte l'indexe des mots(tok)
            String previousWord = "";

            //Pour chaque tok contenu dans le fichier
            for (CoreLabel tok : document.tokens()) {
                if (tok.word().matches("'|,|:|.|\"|<|>|=|;|/|[|]|\\{|\\}"))
                    continue;      //Si le tok actuel est un caractere d'accentuation, on
                //skip cette iteration et on passe au tok suivant.

                if (!wordMap.containsKey(tok.lemma())) {                                         //Si la wordMap ne contient pas le mot
                    wordMap.put(tok.lemma(), new FileMap<>(4));                             //On cree le mot dans la wordMap et on cree un fileMap comme valeur
                    wordMap.get(tok.lemma()).put(file.getName(), new ArrayList<>());
                }

                if (!wordMap.get(tok.lemma()).containsKey(file.getName()))                       //Si la wordMap contient le mot mais dans un autre fichier:
                    wordMap.get(tok.lemma()).put(file.getName(), new ArrayList<>());            //Alors on get le mot et on rajoute un nouveau fileMap avec le nom du fichier actuel

                wordMap.get(tok.lemma()).get(file.getName()).add(compteur);                     //On rajoute la position du mot dans la fileMap du mot

                if(compteur != 1) {
                    String[] bigram = {previousWord, tok.lemma()};
                    bigrams.add(bigram);
                }

                previousWord = tok.lemma();

                compteur++;
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

}


























