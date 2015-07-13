import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;


public class ServerThread implements Runnable {

	private Socket connection;
	public ServerThread(Socket connection) {
		this.connection = connection;
	}
	@Override
	public void run() {
		SocketAddress sa = connection.getRemoteSocketAddress();
		
		try {
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
			DataInputStream dis = new DataInputStream(connection.getInputStream());
			
			//write the servers name/developer name
			dos.writeUTF("Dylans Server");
			
			//update the jlabel that contains the number of clients connected
			ClientServerGui.updateNumClient(sa.toString().substring(1, sa.toString().indexOf(":")));
			
			
			while (true) {
				
				//server is waiting for input from the user
				String nextInput = dis.readUTF();
				System.out.println(nextInput);
				//if the user sends the string getfile list
				//we get the files on the server and send them as a comma seperated string. This makes it easy to send and retrieve on the other end.
				//a byte array or char array is unnecessary and would require more code, thus requiring more computational power from the jvm
				if (nextInput.equals("getfilelist")) {
					System.out.println(ServerMain.getServerFiles());
					dos.writeUTF(ServerMain.getServerFiles());
					
				}
				
				//if the input from the user has a comma, and the first part of the string is download, then we know to 
				//parse the filename
				else if (nextInput.indexOf(',')!= -1 && nextInput.split(",")[0].equals("download")) {
					
					//parse the filename
					String filename = nextInput.split(",")[1];
					
					//if our file is currently in our shared files
					if (ServerMain.getServerFiles().indexOf(filename) != -1) {
						
						//the file exists so send a y
						dos.writeChar('y');
						
						//get the dir associated with the filename requested by client
						String dir = ServerMain.getDir(filename);
						
						//create a file object by concatenating the dir and filename making the correct file object
						File file = new File(dir+"\\"+filename);
						
						//initialize a byte[] with the size of the file
						byte[] filecontent = new byte[(int)file.length()];
						
						//create an input stream passing in a file object
						FileInputStream instr = new FileInputStream(file);
						
						//read the content of the file into the byte array
						instr.read(filecontent);
						
						//write the size of the file as an int so the client can init their byte[] with the size of the file
						dos.writeInt((int)filecontent.length);
						
						//flush the int
						dos.flush();
						
						//write the byte[]  to the output stream
						dos.write(filecontent);
						
						//flush that down
						dos.flush();
						
					}
					else {
						//write the char n, 
						dos.writeChar('n');
					}
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	


