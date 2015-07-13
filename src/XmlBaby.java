import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XmlBaby {
	
	//function will take in the hashmap of server data and marshalls it to an xml file every time a new connection is made
	public static void addToXml(HashMap<String, String> serverinfo) throws ParserConfigurationException, IOException, TransformerException {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document document = db.newDocument();

		
		Element root = document.createElement("ClientServerData");
		document.appendChild(root);
		
		for (String name : serverinfo.keySet()) {
			Element serverData = document.createElement("Server-Data");
			Element serverName = document.createElement("Server-Name");
			Element serverIp = document.createElement("Server-IP");
		
			serverName.appendChild(document.createTextNode(name));
			serverIp.appendChild(document.createTextNode(serverinfo.get(name)));
		
			serverData.appendChild(serverName);
			serverData.appendChild(serverIp);
		
		
			root.appendChild(serverData);
		}	
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource domSource = new DOMSource(document);
		
		//if the file doesnt exist we create it
		File yomane = new File("c:\\users\\ft8\\ClientServerData.xml");
		if (!yomane.exists())
			yomane.createNewFile();
		
		StreamResult streamResult = new StreamResult(new File("c:\\users\\ft8\\ClientServerData.xml"));
		transformer.transform(domSource, streamResult);

		
	}

	//unmarhalls the data of ips and names to a hashmap and returns a hash map. we will set the hashmap of allServerData to the output of this function at startup
	public static HashMap<String, String> chillXml() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		
		HashMap<String, String> oldServerData = new HashMap<String, String>();
		
		File yomane = new File("c:\\users\\ft8\\ClientServerData.xml");
		
		Document document = db.parse(yomane);
		
		if (yomane.exists()) {
			NodeList elements = document.getElementsByTagName("Server-Data");
			for (int i = 0; i < elements.getLength(); i++) {
				Node yo = elements.item(i);
				NodeList childs = yo.getChildNodes();
				oldServerData.put(childs.item(0).getTextContent(), childs.item(1).getTextContent());
			}
		}
		return oldServerData;
		
	}
	public static DefaultListModel getSavedDownloads() {
		DefaultListModel filenames = new DefaultListModel();
		File saveDownloads = new File("C:\\users\\ft8\\clientdownloads");
		for (String s: saveDownloads.list()) {
			filenames.addElement(s);
		}
		return filenames;
	}
}
