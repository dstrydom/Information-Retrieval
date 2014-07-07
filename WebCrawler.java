/*
CS 172, Spring 2014
Project, phase 1: Web Crawler
Partners:
	Danelze Strydom, dstry002@ucr.edu
	Denver Chen, dchen 020@ucr.edu
/*

/**************
Import Packages
**************/
import javax.swing.JOptionPane; //collect input via GUI
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.lang.System;
import java.util.HashSet;
import java.util.Set;
import java.io.File; //file manipulation
import java.io.PrintWriter; //file IO
import java.io.FileWriter; //file IO
import java.net.URLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.BufferedReader; //to access BufferedReader class
import java.io.InputStream; //to access InputStream class
import java.io.BufferedWriter;
import java.io.InputStreamReader; //to access InputStreamReader class
import java.io.IOException; //to access IOException class
import java.io.FileOutputStream; // creates a stream that can be used as an arg to a PrintWriter constructor
import java.util.Iterator; //to traverse HashSet
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap; //not thread-safe
import java.util.Map;
import java.util.Collections;

/****************
Web Crawler Class
****************/
public class WebCrawler {

	/***
	main
	***/
    public static void main(String[] args) {
        int numPgs = 50; //default value if it isn't entered from the command line
		if (args.length > 1) {
            try {
                numPgs = Integer.parseInt(args[1]); //sets #pages to crawl to that entered on command line
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[1] + " must be an integer."); //prints error if non-integer value was entered
                System.exit(1);
            }
        }
        int numHops = 3; //default value if it isn't entered from the commad line
        if (args.length > 2) {
            try {
                numHops = Integer.parseInt(args[2]); //sets #hops to that entered from command line
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[2] + " must be an integer."); //prints error if non-integer value entered
                System.exit(1);
            }
        }
		numPgs++;
		numHops++;
		String inputfile = "input.txt"; //file name of .edu seeds
		if (args.length > 0) {
			inputfile = args[0]; //1st argument of command line
		}
		String outputPath = "";
		if (args.length > 0) {
			outputPath = args[3];
			File theDir = new File(outputPath);

  
			if (!theDir.exists()) { // if the directory does not exist, create it
				System.out.println("creating directory: " + outputPath);
				boolean result = theDir.mkdir();  

				if(result) {    
					System.out.println("DIR created");  
				}
			}
		}
		
        String strURL = "";// = JOptionPane.showInputDialog("Enter website url:"); //Get start url from user and stored in string
        //int numPgs = Integer.parseInt(JOptionPane.showInputDialog("Enter MAX number of pages to get from each page:"));
        //int numHops = Integer.parseInt(JOptionPane.showInputDialog("Enter MAX number of hops to perform:"));
        Document doc; //object that will contain downloaded page contents (Jsoup)
        //HashSet<String> linkSet = new HashSet<String>(); //empty hash set to keep links int (doesn't allow duplicates)  
        //Set<String> linkSet = new HashSet<String>(); //IDE says <String> notation no longer needed, but keep above to test on command line
        ConcurrentHashMap<String, Boolean> map = new ConcurrentHashMap<String, Boolean>(); //Creates a map that is NOT thread safe
        ConcurrentHashMap<String, Boolean> map2 = new ConcurrentHashMap<String, Boolean>(); //Creates a map that is NOT thread safe
        Set<String> linkSet = Collections.newSetFromMap(map); //Creates a set from above map. Will allow iterator to traverse AND manipulate it
        Set<String> linkSet2 = Collections.newSetFromMap(map2); //Creates a set from above map. Will allow iterator to traverse AND manipulate it
        Iterator<String> it = linkSet.iterator(); //iterator for linkSet
        Iterator<String> it2 = linkSet2.iterator(); //iterator for linkSet
        Set<String> visitedSet = new HashSet<String>(); //copy of URLs set. Used to check if site has been visited yet (can be thread safe, because not iterated on)

        Elements links; //used in for loop to extract links from pg
       
        String line; //string used to read each line of html code and then write to file
        int count = 1; //counter used to determine # of pages downloaded. Each page is stored as "page#", where # = count
        int hop = 0; //hop counter
        String savePgName; //used to create page name, need to convert count to string and then add ".txt" to "page" to make "page#.txt"
        Path logFile = Paths.get(inputfile); //gets input seed file and populates linkSet with addresss
        try {
            BufferedReader reader = Files.newBufferedReader(logFile,StandardCharsets.UTF_8);
            String line2;
            while ((line2 = reader.readLine()) != null) {
                //System.out.println(line2);
                linkSet.add(line2);
            }
            it = linkSet.iterator();
            strURL = it.next();
        } catch (Exception e) {
            e.printStackTrace(); //exception prints stack
        }
        
        do{ //loop to iterate through set containing links. Do-while b/c 1st link is collected from user, then the rest are retrieved from the set
            try {//all tries are combined into one large try.
                //if(count > 1){ 
                    while(RobotExclusionUtil.robotsShouldFollow(strURL) == false){ //Checks if initial page is NOT allowed to be crawled
                        if (hop % 2 == 1) {
							if (it2.hasNext()) {
								strURL = it2.next();
							}    
							else {
								hop++;
								linkSet2.clear();
								it = linkSet.iterator();
								strURL = it.next();
							}
						}
                        else {
							if (it.hasNext()) {
								strURL = it.next();
							}    
							else {
								hop++;
								linkSet.clear();
								it2 = linkSet2.iterator();
								strURL = it2.next();
							}
						}
                    }//end while
                           
                    
                    String temp = strURL;
                    temp = temp.replaceFirst("www.","");
                    if (temp.substring(temp.length()-1).equals("/")) {
						temp = temp.substring(0,temp.length()-1);
                    }
                    while(visitedSet.contains(temp) == true){ //cycles through all visited links
						//don't use .remove(), because linkSet checks for duplicates
                        if (hop % 2 == 1) {
							if (it2.hasNext()) {
							strURL = it2.next();
							}    
							else {
								hop++;
								linkSet2.clear();
								it = linkSet.iterator();
								strURL = it.next();
							}
						}
                        else {
							if (it.hasNext()) {
								strURL = it.next();
							}    
							else {
								hop++;
								linkSet.clear();
								it2 = linkSet2.iterator();
								strURL = it2.next();
							}
						}
                            temp = strURL.replaceFirst("www.","");
                            if (temp.substring(temp.length()-1).equals("/")) {
                                temp = temp.substring(0,temp.length()-1);
                            }
                    }//end while
                    visitedSet.add(temp); //marks as visited but will proceed to crawl in code below
                
					URL url; //url object
					url = new URL(strURL);
					System.out.println(url);
					System.out.println(count);
        
					BufferedReader br; //stores InputStream collected data in the buffer, will be used to write to file when each pg is saved
                
					URLConnection connection = url.openConnection();
					//set timeouts
					connection.setConnectTimeout(5000); 
					connection.setReadTimeout(5000);
					br = new BufferedReader(new InputStreamReader(connection.getInputStream()));  //buffers input from "is"

                savePgName = outputPath + "page" + Integer.toString(count) + ".txt"; //creates each new page to download name: page1, page2, ...
                BufferedWriter outStream;
                //"downloads" page to file by same name
                outStream = new BufferedWriter(new FileWriter(savePgName));
                outStream.write("<!--" + strURL + "-->"); //add's page's address at top of txt file as an html comment, ex: <!--http://www.ucr.edu-->
                while (br.ready()) {
                   String asdf = br.readLine();
                   outStream.write(asdf);
                   outStream.write("\n");
                }
                
                br.close();
                outStream.close();
                
                doc = Jsoup.parse(new File(savePgName), "UTF-8", ""); //collect links and put in set

                links = doc.select("a[href]"); //collect all links from 1st pg, uses enhanced for loop


                for (Element link : links) { //"enhanced for loop". Parses pg for links and adds to set. Also normalizes them.
                    String tem = link.attr("href"); 
                    if (tem.indexOf("/",tem.indexOf(".")) > 0 && tem.indexOf(".") > 0) {
                        if (!tem.substring(tem.indexOf("."),tem.indexOf("/",tem.indexOf("."))).contains(".edu")) {
                            continue;
                        }
                    }
                    else if (!tem.contains(".edu")) { //ignores non-edu addresses
                        continue;
                    }
                    if (tem.substring(0,1).equals("#")) { //ignores anything that starts with #, line: #main
                        continue;
                    }
                    if (tem.length() > 6) {
                        if (tem.substring(0,7).equals("mailto:")) { //ignore email links
                            continue;
                        }
                    }
                    if (tem.length() > 17) {
                        if (tem.substring(0,18).equals("javascript:void(0)")) { //ignores anything javascript related
                            continue;
                        }
                    }
                    if (tem.length() > 3) {
                        if (tem.substring(0,4).equals("tel:")) { //ignore telephone numbers (could be opened in Skype, etc)
                            continue;
                        }
                    }
                    if (tem.length() > 1) {
                        if (tem.substring(0,2).equals("//")) { //add root to partial addresses
                            tem = "http:" + tem;
                        }
                    }
                    
                    if (tem.length() > 7) {
                        if (!tem.substring(0,7).equals("http://") && !tem.substring(0,8).equals("https://")) {
                            if (!tem.startsWith("/")) { 
                                tem = "/" + tem; //add "/" to start of string
                            }
                            tem = strURL.substring(0,(strURL.indexOf(".edu")+4)) + tem; //append after .edu
                        }
                    }
                    else {
                        if (!tem.startsWith("/")) {
                                tem = "/" + tem;
                            }
                        tem = strURL.substring(0,(strURL.indexOf(".edu")+4)) + tem; //append after .edu
                    }
                            
					tem = tem.replace("\n",""); //remove newlines because for some reason this url had a newline in it
                    if (hop % 2 == 1) {
                        linkSet.add(tem);
                        //System.out.println("1");
                    }
                    else {
                        linkSet2.add(tem);
                       //System.out.println("2");
                    }
                }
					if (hop % 2 == 1) {
						if (it2.hasNext()) {
							strURL = it2.next();
						}    
						else {
							hop++;
							linkSet2.clear();
							it = linkSet.iterator();
							strURL = it.next();
						}
					}
								else {
						if (it.hasNext()) {
							strURL = it.next();
						}    
						else {
							hop++;
							linkSet.clear();
							it2 = linkSet2.iterator();
							strURL = it2.next();
						}
					}
				//System.out.println(hop);        
                ++count; //increments page counter
			}//end try
           catch (MalformedURLException mue) {
               System.out.println("Malformed URL Exception thrown.");
               System.out.println("bad strURL = " + strURL);
               break; //so that it won't continuously print error message, and will show contents of Set
               //mue.printStackTrace();
           }
           catch (IOException e) {
               System.out.println("IO Exception thrown.");
               //e.printStackTrace();
               System.out.println("General I/O exception: " + e.getMessage());
               e.printStackTrace();
               //break; //so that it won't continuously print error message, and will show contents of Set
           }
       }while(count < numPgs && hop < numHops); //download pgs until # pages to download and # of hops to perform is satisfied
           
        //Print Set 
        //System.out.println("\n\nSet:");
        //for(String s:linkSet)
                //System.out.println(s);
            
    }//end main
}//end WebCrawler
