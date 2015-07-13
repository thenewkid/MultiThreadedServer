import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;


public class ServerMain implements Runnable {

	//arrayList for all the directory paths that are created
	private static ArrayList<String> fileDirPaths = new ArrayList<String>();
	
	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(10170);
			while (true) {
				//waits for a connection
				Socket socket = server.accept();
				
				//when a user is connected the server starts a client thread that handles this particular client
				Thread serverThread = new Thread(new ServerThread(socket));
				serverThread.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static void deleteServerFile(String filename) throws IOException {
		//use the function getDir(filename) to get the dir associated with our filename
		if (!getDir(filename).equals("not found")) {
			File fileToDelete = new File(getDir(filename)+"\\"+filename);
			fileToDelete.delete();
			ClientServerGui.updateSharedFiles();
		}

	}
	//helper function for creating a directory and files within the directory
	public static void createFiles(String dirName, String filename) throws IOException {
		
		//we will use a default path of "C:\\users\\ft8\\ to place our server dirs
		String dirPath = "C:\\users\\ft8\\"+dirName;
		File newOrOldDir = new File(dirPath);
		
		//check if file does not exist
		if (!newOrOldDir.exists()) {
		
			//make the dir
			newOrOldDir.mkdir();
			
			//whenever a new dir is created we add the filename containing the double slashes to the arrayList
			fileDirPaths.add(dirPath);
			
		}
		
		//once we have our dir made, we want to add a file in it
		File fileObj = new File("C:\\users\\ft8\\"+dirName+"\\"+filename);
		if (!fileObj.exists())
			fileObj.createNewFile();
		//System.out.println(newOrOldDir.getName() + " " + newOrOldDir.getAbsolutePath());
	}
	
	//takes in an arraylist of dirs in case the user wants to specify folders for the server to collect files from
	public static void addDirs(ArrayList<String> dirs) {
		fileDirPaths.addAll(dirs);
	}
	
	//function for retreiving the list of files in server files, return a comma seperated value of filenames
	public static String getServerFiles() {
		StringBuffer stringTheory = new StringBuffer();
		int index = 0;
		for (String dirPath: fileDirPaths){
			for (String s : new File(dirPath).list()) {
				if (index > 0)
					stringTheory.append(",");
				stringTheory.append(s);
				index++;
			}
		}
		
		return stringTheory.toString();
	}
	//this function takes in a filename that exists and returns the directory its in
	public static String getDir(String filename) {
		for (String dir : fileDirPaths) 
			if (Arrays.asList(new File(dir).list()).contains(filename)) {
				return dir;
			}
		return "not found";
	}

}
