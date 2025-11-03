package hw2idd;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class TXTIndexer {

    public static void main(String[] args) {
        String docsPath = "file_txt";   // directory dove sono i file .txt
        String indexPath = "index";     // directory dell'indice creato
        
        long startTime = System.currentTimeMillis();

        try {
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, config);

            File folder = new File(docsPath);
            if (!folder.exists() || !folder.isDirectory()) {
                System.out.println("La directory specificata non esiste: " + docsPath);
                writer.close();
                return;
            }

            File[] files = folder.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
            if (files == null || files.length == 0) {
                System.out.println("Nessun file in formato .txt trovato in " + docsPath);
                writer.close();
                return;
            }

            System.out.println("Indicizzazione avviata...\n");

            for (File file : files) {
                System.out.println("Indicizzo il file: " + file.getName());
                StringBuilder content = new StringBuilder();
                String title = null;

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    title = br.readLine();
                    if (title == null || title.trim().isEmpty()) {
                        title = file.getName();
                    }

                    String line;
                    while ((line = br.readLine()) != null) {
                        content.append(line).append("\n");
                    }

                    Document doc = new Document();
                    doc.add(new TextField("title", title.trim(), Field.Store.YES));
                    doc.add(new StringField("title_exact", title.trim(), Field.Store.YES));
                    doc.add(new TextField("content", content.toString(), Field.Store.YES));
                    doc.add(new StringField("filename", file.getName(), Field.Store.YES));
                    writer.addDocument(doc);

                } catch (Exception e) {
                    System.out.println("Errore durante la lettura di " + file.getName() + ": " + e.getMessage());
                }
            }

            writer.close();
            System.out.println("\nFase di indicizzazione completata!");
            System.out.println("Indice salvato in: " + indexPath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fine misurazione tempo
        long endTime = System.currentTimeMillis();
        printIndexingTime(startTime, endTime);
    }

    
    // Calcola e stampa il tempo totale di indicizzazione
private static void printIndexingTime(long start, long end) {
    double durationMs = end - start;
    System.out.printf("\nTempo totale di indicizzazione: %.2f ms\n", durationMs);
}

}
