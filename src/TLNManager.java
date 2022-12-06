import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TLNManager {

    public static ArrayList<File> dataSet;

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

    private static void processDataset(List<File> datasetList) throws FileNotFoundException {
        //for (File file : datasetList) System.out.println(file.getName());

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



        for(File file : datasetList) {                  //For every file in datasetlist
            Scanner scanner = new Scanner(file);
            String content = new String();
            while (scanner.hasNext()) {
                content += scanner.nextLine();
                content += " ";
            }
            scanner.close();
            CoreDocument document = new CoreDocument(content);
            // annotate the document
            pipeline.annotate(document);
            System.out.println(document.tokens());
            for (CoreLabel tok : document.tokens()) {
                System.out.println(tok.lemma());
            }
            //System.out.println(content);
        }


        CoreDocument document = new CoreDocument(new String(""));
        // annotate the document
        pipeline.annotate(document);
        System.out.println(document.tokens());
        for (CoreLabel tok : document.tokens()) {
            System.out.println(String.format("%s\t%s", tok.word(), tok.lemma()));
        }




    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    //TODO remove the warning
    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private static boolean isValidFile(String fileName) {
        return fileName.endsWith(".txt") && !fileName.contains("._") && !fileName.contains(".DS_store");

    }

}


























