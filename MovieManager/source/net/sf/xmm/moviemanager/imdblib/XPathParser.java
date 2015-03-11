package net.sf.xmm.moviemanager.imdblib;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.xmm.moviemanager.models.imdb.ModelIMDbEntry;
import net.sf.xmm.moviemanager.util.StringUtil;

import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathParser {

	
	static Document parseXPath(StringBuffer data, String uri) throws SAXException, IOException, XPathExpressionException {
		SimpleUserAgentContext context = new SimpleUserAgentContext();
		context.setScriptingEnabled(false);
		context.setExternalCSSEnabled(false);
		
		DocumentBuilderImpl dbi = new DocumentBuilderImpl(context);
		//Document document = dbi.parse(new StringReader(data.toString()));
		
		//Document document = dbi.parse(new InputSourceImpl(in, url, "ISO-8859-1")) ;
		Document document = dbi.parse(new InputSourceImpl(new StringReader(data.toString()), "http://akas.imdb.com/title/tt" + uri)) ;
		
		return document;
	}
	
	static ArrayList<Element> evaluateDocument(Document document, String expression) throws SAXException, IOException, XPathExpressionException {
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		ArrayList<Element> res = getElements(xpath, document, expression);
		return res;
	}
	
	static ArrayList<Element> parseXPath(StringBuffer data, String uri, String expression) throws SAXException, IOException, XPathExpressionException {
		
		Document document = parseXPath(data, uri);
				
			//eval = "html//div[@class='info-offset']/table[@class='cast']";
			//eval = "html//table[@class='cast']";

		return evaluateDocument(document, expression);
	}
	
    static void parseDataUsingXPath(ModelIMDbEntry dataModel, StringBuffer data, String uri) {

    	try {
    		Document document = parseXPath(data, "http://akas.imdb.com/title/tt" + uri);
    		String eval = "html//div[@class='info']|html//div[@class='info stars']";	
    		
    		ArrayList<Element> res = evaluateDocument(document, eval);
    		
    		for (Element e : res) {
    			parseXPathResult(dataModel, e);
    		}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
  

    static ArrayList<Element> getElements(XPath xpath, Document document, String path) throws XPathExpressionException {

    	long time = System.currentTimeMillis();
    	NodeList nodeList = (NodeList) xpath.evaluate(path, document, XPathConstants.NODESET);
    	time = System.currentTimeMillis() - time;
    	//System.out.println("time:" + time);

    	ArrayList<Element> result = new ArrayList<Element>();

    	int length = nodeList.getLength();
    	for(int i = 0; i < length; i++) {
    		Element element = (Element) nodeList.item(i);
    		result.add(element);
    	}
    	return result;
    }


    
    static String parseXPathResult(ModelIMDbEntry dataModel, Element e) {
    	/*
    	 Matches strings like:
    	 "
        
        Director:
        
        
        
            
            
            James Cameron
        
         
    	"  
    	 */
    	
 	   Pattern p = Pattern.compile("\\s*(.+?):\\s*(.+)");
 	   Matcher m = p.matcher(e.getTextContent());
 	    	   
 	   if (m.find()) {

 		   int gCount = m.groupCount();
 		  
 		   if (gCount == 2) {

 			   String name = m.group(1);
 			   String info = m.group(2).trim();

 			   if (name.equals("Also Known As")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(2).getChildNodes();
 				   
 				   String aka = "";
 				   
 				   for (int i = 0; i < children.getLength(); i++) {
 					   Node n = children.item(i);
 					   String tmp = n.getTextContent();
 					   
 					   if (!tmp.trim().equals("") && !tmp.trim().equals("more")) {
 						   aka += tmp.trim() + "\n";
 					   }
 				   }
 				   aka = StringUtil.removeDoubleSpace(aka);
 				  //System.out.println("Also Known As:" + aka);
 				   dataModel.setAka(aka);
 			   }
 			   else if (name.equals("Sound Mix")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();
 				   
 				   String sound = getNodesContent(children);
 				   sound = sound.replaceAll("\\(", " (");
 				   
 				  //System.out.println("sound:" + sound);
 				   dataModel.setWebSoundMix(sound);
 			   }
 			   else if (name.equals("Plot")) {
 				  
 				   int index = info.indexOf("full summary");
 				   if (index != -1) {
 					   info = info.substring(0, index);
 				   }
 				  dataModel.setPlot(info);
 			   }
 			   else if (name.equals("Certification")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();
 				   String certification = getNodesContent(children);
 				   certification = certification.replaceAll("\\(", " (");
 				   certification = StringUtil.removeDoubleSpace(certification);
 				   //System.out.println("Certification:" + certification);
 				   dataModel.setCertification(certification);
 			   }
 			   else if (name.equals("Language")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();
 				   
 				   String language = getNodesContent(children);
 				  //System.out.println("Language:" + language);
 				   dataModel.setLanguage(language);
 			   }
 			   else if (name.startsWith("Country")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();

 				   String country = getNodesContent(children);
 				  //System.out.println("Country:" + country);
 				   dataModel.setCountry(country);
 			   }
 			   else if (name.startsWith("User Rating")) {
 				   String rating;

 				   NodeList children = e.getChildNodes();
 				   children = children.item(7).getChildNodes();
 				   children = children.item(1).getChildNodes();
 				   //printNodeTree(children.item(1));
 				   
 				   rating = getNodesContent(children); 

 				  if (rating.indexOf("/") != -1) {
 					  // On the format 8.5/10
 					  info = rating.substring(0, rating.indexOf("/"));
 					  dataModel.setRating(info);
 				  }
 			   }
 			   else if (name.startsWith("Writer") || name.startsWith("Creator") || name.startsWith("Director")) {

 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();

 				   String resultValue = "";

 				   for (int i = 0; i < children.getLength(); i++) {
 					   Node n = children.item(i);
 					   String tmp = n.getTextContent().trim();

 					   if (!tmp.equals("") && !tmp.equals("more") &&
 							   !tmp.startsWith("(written by)") && 
 							   !tmp.startsWith("(writer)") && 
 							   !tmp.startsWith("(co-creator)") &&
 							   !tmp.startsWith("(screenplay)")) {

 						   if (resultValue.length() > 0)
 							   resultValue += ", ";

 						   resultValue += removeNewLines(tmp);
 					   }
 				   }
 				   resultValue = resultValue.replaceAll("\\|", ", ");

 				   if (name.startsWith("Director"))
 					   dataModel.setDirectedBy(resultValue);
 				   else
 					   dataModel.setWrittenBy(resultValue);

 			   }
 			   else if (name.startsWith("Creator")) {
 				   //System.out.println("Director:" + info);
 				   dataModel.setWrittenBy(info);
 			   }
 			   else if (name.startsWith("Director")) {
 				   //System.out.println("Director:" + info);
 				   dataModel.setDirectedBy(info);
 			   }
			   else if (name.startsWith("Runtime")) {
 				   info = StringUtil.removeDoubleSpace(info);
 				   String runtime =  info.replaceAll("\\s\\|", ",");
 				   //System.out.println("Runtime:" + runtime);
 				   dataModel.setWebRuntime(runtime);
 			   }
 			   else if (name.startsWith("Genre")) {
 				   String genre =  info.replaceAll("\\s\\|", ",");

 				   genre = StringUtil.removeAtEnd(genre, "more");
 				   //System.out.println("Genre:" + genre);
 				   genre = genre.trim();
 				   dataModel.setGenre(genre);
 			   }
 			   else if (name.startsWith("MPAA")) {
 				  //System.out.println("MPAA:" + info);
 				   dataModel.setMpaa(info);
 			   }
 			   else if (name.startsWith("Awards")) {
 				   NodeList children = e.getChildNodes();
 				   children = children.item(3).getChildNodes();

 				   String awards = getNodesContent(children);
 				   awards = awards.replaceAll("&", " & ");
 				   
 				   awards = StringUtil.removeAtEnd(awards, "more");
 				   //System.out.println("Awards:" + info);
 				   dataModel.setAwards(awards);
 			   }
 			   else if (name.startsWith("Color")) {
 				  //System.out.println("Color:" + info);
 				   dataModel.setColour(info);
 			   }
 			   else {
 				  //System.out.println("name:" + name);
 				  //System.out.println("info:" + info);
 			   }
 		   }
 	   }
 	   // Cast
 	   else {
 		   NodeList children = e.getChildNodes();
 		   
 		   children = children.item(3).getChildNodes();
 		   children = children.item(1).getChildNodes();
 		   
 		   //printNodeTree(children.item(1));
 		   
 		   String cast = "";
 		   
 		   for (int i = 0; i < children.getLength(); i++) {
 			   Node n = children.item(i);
 			   String tmp = n.getTextContent();
 			   			   
 			   if (!tmp.trim().equals("")) {
 				
 				   String [] split = tmp.split("\\.\\.\\.");
 				   
 				   if (split.length != 2)
 					   continue;
 				   
 				   if (cast.length() > 0)
 					   cast += ", ";
 				   
 				   cast += split[0].trim() + " (" + split[1].trim() + ")";
 			   }
 		   }
 		   cast = cast.replaceAll("\\s\\|", ",");
 		   
 		  //System.out.println("Cast:" + cast);
 		   dataModel.setCast(cast);
 	   }
 	   return null;
    }
        
    static String getNodesContent(NodeList nodes) {

    	String result = "";

    	for (int i = 0; i < nodes.getLength(); i++) {
    		Node n = nodes.item(i);
    		String tmp = n.getTextContent();

    		if (!tmp.trim().equals("")) {
    			result += removeNewLines(tmp.trim());
    		}
    	}
    	result = result.replaceAll("\\|", ", ");

    	return result;
    } 
    
    static String removeNewLines(String input) {
 	   return input.replaceAll("\\n", "");
    }
     

    static void printNodeTree(Node node) {
    	    	
    	if(node instanceof Element) {
    		Element element = (Element) node ;

    		// Visit tag.

    		doElement(element) ;

    		// Visit all the children, i.e., tags contained in this tag.

    		NodeList nl = element.getChildNodes() ;
    		if(nl == null) return ;
    		int num = nl.getLength() ;
    		for(int i=0; i<num; i++)
    			printNodeTree(nl.item(i)) ;

    		// Process the end of this tag.
    		doTagEnd(element) ;
    	}
    }

   static int indent = 0;

    static String getIndent() {
    	StringBuffer buf = new StringBuffer();

    	for (int i = 0; i < indent; i++)
    		buf.append(" ");

    	return buf.toString();
    }

    static LinkedList<String> elemList = new LinkedList<String>();
    
    public static void printList(LinkedList<String> elemList) {
    	
    	System.out.print("<");
    	for (String e : elemList) {
    		System.out.print(e + ".");
    	}
    	System.out.println(">");
    }
    
    public static void doElement(Element element) {
    	indent++;
    	elemList.add(element.getTagName());
    	
    	//printList(elemList);
    	
    	
    	
    	System.out.println(getIndent() + "<" + element.getTagName() + ">") ;
    	System.out.println( element.getTextContent());

    }
    
    
    public static void doTagEnd(Element element) {
    	indent--;
    	System.out.println(getIndent() + "</" + element.getTagName() + ">") ;      
    
    	elemList.removeLast();
    }

    
    public static void printPath(Node n) {

    	String str = n.toString();
    	
    	do {
    		n = n.getParentNode();
    		str += "." + n.toString();

    	} while (n.getParentNode() != null);
    	
    	System.out.println(str);
    }
}
