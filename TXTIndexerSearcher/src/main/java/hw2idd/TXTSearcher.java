package hw2idd;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class TXTSearcher {

    private final IndexSearcher searcher;
    private final QueryParser titleParser;
    private final QueryParser contentParser;
    private final long totalDocs;

    public TXTSearcher(String indexPath) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
        totalDocs = reader.numDocs();

        // Analyzer personalizzato per il campo "title"
        Analyzer titleAnalyzer = CustomAnalyzer.builder()
                .withTokenizer(WhitespaceTokenizerFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                .build();

        // Analyzer standard per il campo "content"
        Analyzer contentAnalyzer = new StandardAnalyzer();

        titleParser = new QueryParser("title", titleAnalyzer);
        contentParser = new QueryParser("content", contentAnalyzer);
    }

    // Esecuzione della ricerca su uno o più campi
    public void search(String queryStr) throws Exception {
        long startTime = System.nanoTime();

        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        String[] terms = queryStr.split(",");

        boolean titleSearched = false;
        boolean contentSearched = false;

        for (String term : terms) {
            String[] parts = term.split(":", 2);
            if (parts.length != 2) {
                System.out.println("Formato della query non valido. Usa title:term o content:term o title:\"term\" o content:\"term\".");
                return;
            }

            String field = parts[0].trim().toLowerCase();
            String fieldQuery = parts[1].trim();

            Query query;
            switch (field) {
                case "title":
                    // Se la query del titolo ha spazi ma non virgolette → match esatto
                    if ((fieldQuery.contains(" ") || fieldQuery.contains(":")) && !fieldQuery.contains("\"")) {
                        query = new TermQuery(new Term("title_exact", fieldQuery));
                    } else {
                        query = titleParser.parse(fieldQuery);
                    }
                    booleanQueryBuilder.add(query, BooleanClause.Occur.MUST);
                    titleSearched = true;
                    break;

                case "content":
                    query = contentParser.parse(fieldQuery);
                    booleanQueryBuilder.add(query, BooleanClause.Occur.MUST);
                    contentSearched = true;
                    break;

                default:
                    System.out.println("Campo inserito non valido. I campi validi sono: title, content.");
                    return;
            }
        }

        BooleanQuery finalQuery = booleanQueryBuilder.build();
        TopDocs results = searcher.search(finalQuery, 10);

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        long totalDocumentsReturned = results.totalHits.value();
        System.out.println("Trovati " + totalDocumentsReturned + " documenti.\n");

        if (totalDocumentsReturned > 0) {
            int index = 1;
            for (ScoreDoc hit : results.scoreDocs) {
                Document doc = searcher.storedFields().document(hit.doc);

                String title = doc.get("title") != null ? doc.get("title") : "N/A";
                String fileName = doc.get("filename") != null ? doc.get("filename") : "N/A";

                System.out.println("[" + index + "] Titolo: " + title);
                System.out.println("File: " + fileName);
                System.out.println("Score: " + hit.score);

                if (contentSearched) {
                    System.out.println("Contenuto: " + snippet(doc.get("content")));
                }

                System.out.println("------------");
                index++;
            }
        } else {
            System.out.println("Nessun documento trovato per la query.");
        }

        System.out.println("\nStatistiche:");
        System.out.printf("Tempo di risposta: %.2f ms%n", duration / 1_000_000.0);
    }

    private String snippet(String content) {
        if (content == null) return "N/A";
        content = content.replaceAll("\\s+", " ");
        return content.length() > 250 ? content.substring(0, 250) + "..." : content;
    }

    public void close() throws IOException {
        searcher.getIndexReader().close();
    }

    public static void main(String[] args) {
        try {
            String indexPath = "index";
            TXTSearcher searcher = new TXTSearcher(indexPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Inserisci la tua query (es. 'title:term', 'content:\"frase\"'). Scrivi 'exit' per uscire.");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.equalsIgnoreCase("exit")) break;
                if (line.trim().isEmpty()) continue;
                searcher.search(line);
            }

            searcher.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
