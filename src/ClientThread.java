import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


public class ClientThread implements Runnable {

	private String ip;
	public static boolean requestSentForFileList = false;
	public static boolean requestSentForDownload = false;
	public static String fileForDownload;
	
	
	public ClientThread(String ip) {
		this.ip = ip;
	}
	
	
	@Override
	public void run() {
		try {
			Socket socket = new Socket(ip, 10170);
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			String name = dis.readUTF();
			
			//when we get the name of the server were connected to, we store it in our serverdata
			//the function will not store this data if the name already exists in the data
			ClientServerGui.storeNewConnection(name, ip);
			ClientServerGui.updateCurrentlyConnected(name, ip);
			while (true) {
				
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (requestSentForFileList) {
					dos.writeUTF("getfilelist");
					dos.flush();
					String filelist = dis.readUTF();
					System.out.println(filelist + "from server");
					ClientServerGui.updateAvailableFiles(filelist);
					requestSentForFileList = false;
				}
				else if (requestSentForDownload && fileForDownload != null) {
					dos.writeUTF("download,"+fileForDownload);
					char c = dis.readChar();
					if (c == 'y') {
						
						int size = dis.readInt();
						System.out.println(size);
						byte[] filecontent = new byte[size];
						dis.readFully(filecontent, 0, filecontent.length);
					
						//after weve read in the byte data we create our file
						File toDownload = new File("C:\\users\\ft8\\clientdownloads\\" + fileForDownload);
						if (!toDownload.exists())
							toDownload.createNewFile();
					
						//finally we write the byte array to the file
						FileOutputStream fos = new FileOutputStream(toDownload);
						fos.write(filecontent);
//					
						fos.close();
						
						
						System.out.println("downloaded");
						ClientServerGui.moveFromAvailableToDownloads(fileForDownload);
						
						requestSentForDownload = false;
						fileForDownload = null;
						
					}
					else if (c == 'n')
						ClientServerGui.showFileDoesntExistWarning(fileForDownload);
						requestSentForDownload = false;
						fileForDownload = null;
					
				}
			}
			
		} catch (IOException | ParserConfigurationException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	
	

	
}
