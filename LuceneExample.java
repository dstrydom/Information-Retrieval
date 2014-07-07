import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;//.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

class WebDocument {
	public String title;
	public String body;
	public String url;	
	public WebDocument(String t, String b, String u) {
		this.title = t;
		this.body = b;
		this.url = u;
	}
}


public class LuceneExample {
	public static String INDEX_DIR = "./index";
    public static String HTML_DIR = "./pages"; //String with path to directory containing HTML files (from phase1)
	public static void main(String[] args) throws CorruptIndexException, IOException {
            
           if(args.length > 0){ //if path given 
                if(args.length != 2){
                    System.out.println("Correct Usage: <html files dir> <index dir>");
                    System.exit(1);
                }
                HTML_DIR = args[0];
                INDEX_DIR = args[1];
                System.out.println("HTML_DIR = " + HTML_DIR);
                System.out.println("INDEX_DIR = " + INDEX_DIR);
            }
            //default is to create a directory called "testIndex" in the working directory
           
            
            File input = new File(HTML_DIR); //open directory with stored html files
            String[] pagesList = input.list(); //list of all the files in the folder
            File[] paths = input.listFiles(); //creates a FILE array with a list of all the files in the directory
            
            String fileName = ""; //each html file's name
            String htmlBody = ""; //each html file's body element
            String htmlURL = ""; //each html file's URL (in phase 1 we stored this as a comment at the top of the file)
            WebDocument page = null; //object that acts as triplet container for filename, htmlBody, and htmlURL
            
            String htmlTitle = ""; //each html file's URL
            for(int i=0; i<pagesList.length; ++i){ //steps through files in folder
                fileName = pagesList[i]; //gets each file's name from the list of files in the directory (which are web pages)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
                System.out.println("Indexing " + fileName);
                
                Document doc = Jsoup.parse(paths[i],"UTF-8", fileName); //creates a jsoup Document object from the file
                if(doc != null){
                    
                    if(doc.body() != null)
                        htmlBody = doc.body().text();  //parses the doc for the body (returns element, so call .text() to convert element to string)
                    
                    if(doc.title() != null)
                        htmlTitle = doc.title(); //parse the doc for the title (returns a string)
                
                   
                    htmlURL = doc.toString();//gets string of entire doc
                    if (htmlURL.length() < 5 ) continue;
                    if (!htmlURL.substring(0,4).equals("<!--")) continue;
                    htmlURL = htmlURL.substring(4, (htmlURL.indexOf(">")-2) ); //extracts url from it (we saved it as a <!--HTML COMMENT-->)
                    System.out.println("htmlURL parsed = " + htmlURL);
                    page = new WebDocument(htmlTitle, htmlBody, htmlURL); //creates triplet with correct information
                    index(page); //index pages based on their Title, Body, and URL
                }
                //if null, just skip
                System.out.println("\n");
            }//end for
	}
        
	
	public static void index (WebDocument page) {
		File index = new File(INDEX_DIR);	
		IndexWriter writer = null;
		try {	
			IndexWriterConfig indexConfig = new IndexWriterConfig(Version.LUCENE_34, new StandardAnalyzer(Version.LUCENE_35));
			writer = new IndexWriter(FSDirectory.open(index), indexConfig);
			System.out.println("Indexing to directory '" + index + "'...");	
			org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();	
			luceneDoc.add(new Field("text", page.body, Field.Store.YES, Field.Index.ANALYZED));
			luceneDoc.add(new Field("url", page.url, Field.Store.YES, Field.Index.NO));
			luceneDoc.add(new Field("title", page.title, Field.Store.YES, Field.Index.ANALYZED));
			writer.addDocument(luceneDoc);
                        
                        //System.out.println("toString:" + luceneDoc.toString()); //TODO: for debug: prints contents of Doc
                        
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (writer !=null)
				try {
					writer.close();
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
                
	}
	
}
