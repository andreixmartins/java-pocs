import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String args[]) throws Exception {
        Directory memoryIndex = new EnhancedDirectory();
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writter = new IndexWriter(memoryIndex, indexWriterConfig);
        Document document = new Document();
        document.add(new TextField("title", "Conan the barb", Field.Store.YES));
        document.add(new TextField("body", "stygian dogs!", Field.Store.YES));
        writter.addDocument(document);
        writter.close();

        //indexInfo(memoryIndex);

        List<Document> documents =  searchIndex("title", "conan",memoryIndex,analyzer);
        System.out.println(documents);

        //indexInfo(memoryIndex);

        List<Document> documents2 =  searchIndex("title", "bulls",memoryIndex,analyzer);
        System.out.println(documents2);

        indexInfo(memoryIndex);
        ((EnhancedDirectory)memoryIndex).touch("segments_1");
        System.out.println("FIN.");
    }

    private static void indexInfo(Directory memoryIndex) throws Exception{
        System.out.println("*** Index info ");
        System.out.println("all files: ");
        Arrays.stream(memoryIndex.listAll()).forEach( f -> {
            try {
                System.out.println("File: " + f.toString() + " len: " + (memoryIndex.fileLength(f.toString()))+"");
                System.out.println("content: ");
                ((EnhancedDirectory)memoryIndex).hijack(f.toString());
            } catch (Exception e) {
                e.printStackTrace();
            };
        });
    }

    public static List<Document>  searchIndex(String inField, String queryString,Directory memoryIndex,StandardAnalyzer analyzer) {
        try {
            Query query = new QueryParser(inField, analyzer).parse(queryString);
            IndexReader indexReader = DirectoryReader.open(memoryIndex);
            IndexSearcher searcher = new IndexSearcher(indexReader);
            TopDocs topDocs = searcher.search(query, 10);
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(searcher.doc(scoreDoc.doc));
            }
            return documents;
        }catch (Exception e){
            System.out.println("could not search !!!!!!!!!!!!!!1");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
