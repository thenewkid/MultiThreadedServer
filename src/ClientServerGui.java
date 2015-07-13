import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;





import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;


public class ClientServerGui {

	private static JScrollPane scrollhard;
	private static JFrame jf;
	private static JLabel currentlyConnected = new JLabel("Not Connected");
	
	private static JLabel numClients = new JLabel("Number of Clients: 0");
	
	//bad variable names, currentAddresses are the ips that the client has connected to
	private static DefaultListModel currentAddresses = new DefaultListModel();
	
	private static JList availableIps = new JList(currentAddresses);
	
	private static DefaultListModel filesAvailable = new DefaultListModel();
	private static JList availableFiles = new JList(filesAvailable);
	
	private static DefaultListModel filesDownloaded = new DefaultListModel();
	private static JList downloadedFiles = new JList(filesDownloaded);
	
	//these are bad variable names. I Know!!! the currentIps are the clients that have connected to my server
	private static DefaultListModel currentIps = new DefaultListModel();
	private static JList currentClients = new JList(currentIps);
	
	private static HashMap<String, String> allServerData = new HashMap<String, String>();
	private static HashMap<String, String> allClientData = new HashMap<String, String>();
	
	
	private static JOptionPane warning = new JOptionPane();
	
	private static ArrayList<String> clientIps = new ArrayList<String>();
	private static JTextField ipToConnect = new JTextField();
	
	//the dynamic list for the sharedFileJList
	private static DefaultListModel sharedFilesContent = new DefaultListModel();
	
	//the actual jlist that will show the contents of sharedFilesContent when its updated
	private static JList sharedFiles = new JList(sharedFilesContent);
	
	private static Thread currentClientThread;
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException, SAXException {
		setupJFrame();
		
		addJFrameMaterial();
		
		endJFrame();
		
		//starts server
		Thread serverMain = new Thread(new ServerMain());
		serverMain.start();
		
		// you can either create files and directories, or specify directories and files already created
		createFilesBro();
		ServerMain.addDirs(createDirList("C:\\users\\ft8\\ServerFiles", "C:\\users\\ft8\\ServerFiles1"));
		
		updateSharedFiles();
		allServerData = XmlBaby.chillXml();
		updateServersConnected();
		
		DefaultListModel savedDownloads = XmlBaby.getSavedDownloads();
		updateDownloads(savedDownloads);
		
	}
	
	private static void updateDownloads(DefaultListModel savedDownloads) {
		filesDownloaded.clear();
		for (int i = 0; i < savedDownloads.getSize(); i++) {
			if (!filesDownloaded.contains(savedDownloads.elementAt(i)))
				filesDownloaded.addElement(savedDownloads.elementAt(i));
		}
		
	}

	//create an array list of directories to add to our server  files
	public static ArrayList<String> createDirList(String... args) {
		ArrayList<String> dirsdirs = new ArrayList<String>();
		for (String s : args)
			dirsdirs.add(s);
		return dirsdirs;
			
	}
	public static void updateClientIp(String ip) {
		if (!clientIps.contains(ip))
			clientIps.add(ip);
	}
	private static void createFilesBro() throws IOException {
		ServerMain.createFiles("ServerFiles", "DylanTheCodeMonkey.txt");
		ServerMain.createFiles("ServerFiles", "DylanTheCodeCheetah.txt");
		ServerMain.createFiles("ServerFiles", "DylanTheCodeCat.txt");
		ServerMain.createFiles("ServerFiles1", "DylanTheCodePanther.txt");
		ServerMain.createFiles("ServerFiles1", "DylanTheCodeDog.txt");
		ServerMain.createFiles("ServerFiles1", "DylanTheCodeSnake.txt");
	}
	
	//adds the event to deal with the user clicking the delete button
	private static void addDeleteEvent(JButton btn) {
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!sharedFiles.isSelectionEmpty()) {
					ArrayList<String> filenames = (ArrayList<String>)(sharedFiles.getSelectedValuesList());
					try {
						for (String s : filenames)
							ServerMain.deleteServerFile(s);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}});
	}
	
	//adds the connect button functionality
	private static void addConnectEvent(JButton btn) {
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//first check if previous server name is selected, if so connect to it
				if (!availableIps.isSelectionEmpty()) {
					
					//get the current value of server name in the list
					String selectedValue = (String) availableIps.getSelectedValue();
					
					//we know this element is not null if they selected it from the list
					selectedValue = allServerData.get(selectedValue);
					
					//now start a new client thread passing in the selected value which will be an ip address
					Thread clientThread = new Thread(new ClientThread(selectedValue));
					clientThread.start();
				}
					
				//if we get here then we know the user has not selected anything from out previous server names
				else if (availableIps.isSelectionEmpty())  {
					
					//if the text in the ip address text box is not empty
					if (!ipToConnect.getText().isEmpty())  {
						
						//get the current text
						String currentIp = ipToConnect.getText();
						
						//now we have to make sure its a valid ip address, Im going to solve this by making sure
						//that there are 3 periods in the string
						if (getCount(currentIp, '.') == 3) {
							
							Thread clientThread = new Thread(new ClientThread(currentIp));
							clientThread.start();
						}
						
					}
//						
				}
			}
			
		});
	}
	//adds the functionality for the get files button
	private static void addGetFilesEvent(JButton btn) {
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				//this lets the client thread know to send a request for files
				ClientThread.requestSentForFileList = true;
			}
			
		});
	}
	//adds the download button functionality
	private static void addDownloadEvent(JButton btn) {
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!availableFiles.isSelectionEmpty()) {
					String selectedFile = (String) availableFiles.getSelectedValue();
					ClientThread.fileForDownload = selectedFile;
					ClientThread.requestSentForDownload = true;
					
				}
				
				
			}
			
		});
		
	}
	private static void addDeleteDownloadEvent(JButton btn) {
		File clientDownloads = new File("c:\\users\\ft8\\clientdownloads");
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				for (Object s : downloadedFiles.getSelectedValuesList()) {
					s = (String) s;
					File toRemoveFromDownloads = new File("C:\\users\\ft8\\clientdownloads\\"+s);
					toRemoveFromDownloads.delete();
					DefaultListModel savedDownloads = XmlBaby.getSavedDownloads();
					updateDownloads(savedDownloads);
				}
				
			}
			
		});
	}
	//adds all the elements
	private static void addJFrameMaterial() {
		//create the three buttons and add their action events
		JButton connect = new JButton("Connect");
		
		//add functionality to our connect button
		addConnectEvent(connect);
		
		JButton getFiles = new JButton("Get Files");
		
		//add functionailty to our get Files button
		addGetFilesEvent(getFiles);
		
		JButton downloadFile = new JButton("Download File");
		addDownloadEvent(downloadFile);
		
		JButton deleteButton = new JButton("delete server file");
		addDeleteEvent(deleteButton);
		
		deleteButton.setBounds(700, 430, 150, 20);
		jf.add(deleteButton);
		
		//add jlabel for keeping track of number of users
		numClients.setBounds(500, 600, 200, 20);
		jf.add(numClients);
		
		JLabel sharedStuff = new JLabel("Shared Files");
		sharedStuff.setBounds(700, 100, 100, 20);
		jf.add(sharedStuff);
		
		sharedFiles.setBounds(700, 120, 150, 300);
		jf.add(sharedFiles);
		
		

		//set their bounds
		connect.setBounds(0, 0, 100, 25);
		getFiles.setBounds(0, 30, 100, 25);
		downloadFile.setBounds(0, 60, 100, 25);
		
		scrollhard = new JScrollPane(downloadedFiles);
		//set bounds of our 3 JList
		availableIps.setBounds(0, 120, 150, 300);
		availableFiles.setBounds(175, 120, 150, 300);
		scrollhard.setBounds(350, 120, 150, 300);
		
		//add jlists and jlabels for them
		JLabel ips = new JLabel("Available Ips");
		ips.setBounds(0, 100, 100, 20);
		jf.add(ips);
		jf.add(availableIps);
		
		JLabel availFiles = new JLabel("Available Files");
		availFiles.setBounds(175, 100, 100, 20);
		jf.add(availFiles);
		jf.add(availableFiles);
		
		JButton deleteDownload = new JButton("Delete Download");
		deleteDownload.setBounds(350, 420, 150, 20);
		jf.add(deleteDownload);
		addDeleteDownloadEvent(deleteDownload);
		
		JLabel downloads = new JLabel("Downloaded Files");
		downloads.setBounds(350, 100, 100, 20);
		
		
		jf.add(downloads);
		jf.add(scrollhard);
		
		jf.add(warning);
		
		//set the bounds of our jtextfield
		ipToConnect.setBounds(120, 0, 150, 25);
		jf.add(ipToConnect);
		
		//add our who connected to label
		currentlyConnected.setBounds(300, 0, 400, 20);
		jf.add(currentlyConnected);
		
		//add the current Clients and label
		JLabel currentAdds = new JLabel("Clients Connected");
		currentAdds.setBounds(525, 100, 115, 20);
		jf.add(currentAdds);
		currentClients.setBounds(525, 120, 150, 300);
		jf.add(currentClients);
		
		//add buttons
		jf.add(connect);
		jf.add(getFiles);
		jf.add(downloadFile);
		
	}
	
	public static void showFileDoesntExistWarning(String filename) {
		warning.showMessageDialog(jf, "Warning: File ---"+filename+"---"+ " Does Not Exist");
	}
	public static void setupJFrame() {
		jf = new JFrame();
		jf.setSize(700, 700);
		jf.setLayout(null);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	public static void endJFrame() {
		jf.setVisible(true);
	}
	
	//function that returns true if the filesAvailable list is empty, else false
	public static boolean filesAvailableEmpty() {
		return filesAvailable.isEmpty();
	}
	//function that returns true if the filesAvailable list is empty, else false
	public static boolean filesDownloadedEmpty() {
		return filesDownloaded.isEmpty();
	}
	//function that returns true if the filesAvailable list is empty, else false
	public static boolean currentAddressesEmpty() {
		return currentAddresses.isEmpty();
	}
	
	public static void storeNewConnection(String name, String ip) throws ParserConfigurationException, IOException, TransformerException {
		if (!allServerData.containsKey(name)) {
			allServerData.put(name, ip);
			
			//this function loops through our server data hashmap and adds all the names to currentIps
			updateServersConnected();
			XmlBaby.addToXml(allServerData);
		}
	}
	private static void updateServersConnected() {
		for (String name : allServerData.keySet())
			if (!currentAddresses.contains(name))
				currentAddresses.addElement(name);
	}
	
	private static void updateClientsConnected() {
		for (String s : clientIps)
			if (!currentIps.contains(s))
				currentIps.addElement(s);
	}
	
	//adds the 
	public static void updateAvailableFiles(String commaSeperatedFileNames) {
		String[] filenames = commaSeperatedFileNames.split(",");
		filesAvailable.clear();
		for (String fname : filenames) {
			if (!filesAvailable.contains(fname) && !filesDownloaded.contains(fname))
				filesAvailable.addElement(fname);
		}
	}
	//helper function. takes in a string and a char to count the number of times the char is present, returns int
	private static int getCount(String str, char c) {
		int count=0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == c)
				count++;
		}
		return count;
	}
	
	public static void updateNumClient(String ip) {
		updateClientIp(ip);
		updateClientsConnected();
		numClients.setText("Number of Clients: " + clientIps.size());
		
	}
	
	//tell you who you are connected to
	public static void updateCurrentlyConnected(String name, String ip) {
		currentlyConnected.setText("Connected to " + name + " @ " + ip);
	}
	
	//moves the file donwloaded to the downloads list
	public static void moveFromAvailableToDownloads(String fileForDownload) {
		filesAvailable.removeElement(fileForDownload);
		if (!filesDownloaded.contains(fileForDownload))
			filesDownloaded.addElement(fileForDownload);
		
	}
	
	//gets the filenames, clears the old list and re populates with the updated server files
	public static void updateSharedFiles() {
		String[] filenames = ServerMain.getServerFiles().split(",");
		sharedFilesContent.clear();
		for (String s : filenames) {
			System.out.println(s);
			sharedFilesContent.addElement(s);
		}
	}
	
	//return the list of server names and the corressponding ips
	public static HashMap<String, String> getCurrentServerData() {
		return allServerData;
	}


}
