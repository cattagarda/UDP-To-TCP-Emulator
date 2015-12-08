//Reference: Computer Networking: A Top Down Approach, by Kurose and Ross

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

public class UDPtoTCPServer {
	int syn;
	int ack;
	int fin;
	int synNum;
	int ackNum;
	int windowSize = 3;
	String status = "";
    static String dataToSend = "Huwawawa!";
	
	static DatagramSocket serverSocket;
	static DatagramPacket serverPacket;
    static byte[] sendData = new byte[1024];
    static byte[] receiveData = new byte[1024];
    int[] dropProbability = {0,25,50,75,100};
	
	public UDPtoTCPServer(){
		try {
			serverSocket = new DatagramSocket(10224);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serverPacket = new DatagramPacket(sendData, sendData.length);
		
		synNum = 0;
		ackNum = 0;
		syn = 0;
		ack = 0;
		fin = 0;
	}

	public void waitConnection(){
		System.out.println("Wait connection...");
		System.out.println("3-way handshake...");
		
		try {
			serverSocket.receive(serverPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		receiveData = serverPacket.getData();
		String receivedData = new String(receiveData);
		System.out.println("Received data from client: "+receivedData);
		
		String[] data = receivedData.split("\\|");
		System.out.println(Integer.parseInt(data[0]));
		ackNum = Integer.parseInt(data[0]) + 1;
		
		ack = 1;
		
		Random rand = new Random();
		synNum = rand.nextInt(1000);
		syn = 1;
		
		status = "";
		status = synNum+"|"+ackNum+"|"+syn+"|"+ack+"|"+fin;
		sendData = status.getBytes();
		serverPacket.setData(sendData);
		
		try {
			System.out.println("Sending data to client.. "+status);
			serverSocket.send(serverPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			serverSocket.receive(serverPacket);
			System.out.println(new String (serverPacket.getData()));
			System.out.println("Connection established!");
			
			sendData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(){
		byte[] getData = new byte[1024];
		System.out.println("Waiting for connection...");
		
		while(true){
			try {
				serverSocket.receive(serverPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			getData = serverPacket.getData();
			System.out.println(new String(getData));
		}
	}
	
	public void sendData(){
		Random random = new Random();
		int dropConnection = dropProbability[random.nextInt(5)];
		
		sendData = dataToSend.getBytes();
		int length = sendData.length;
		int quo = length / windowSize;
		int rem = length % windowSize;
		
		if(rem != 0) quo++;
		
		int i = 0;
		while(i<quo){
			for(int j=0; j<windowSize; j++){
				try{
					byte[] perByte = new byte[1];
					perByte[0] = sendData[(i*windowSize)+j];
					
					String toSend = ((i*windowSize)+j)+"|"+new String(perByte);
					perByte = toSend.getBytes();
					
					System.out.println("Sending >>> "+ new String(perByte));
        		 	serverPacket.setData(perByte);
        		 	
        		 	if(random.nextInt(100) <= dropConnection){
        		 		System.out.println("The packet was dropped "+dropConnection+"%!");
        		 	} else {
        		 		serverSocket.send(serverPacket);
        		 	}
        		 	
        		 	Thread.sleep(2000);
        		 	
				}catch(Exception e){ 
					System.out.println("Finished getting the files!");
					break;
				}
			}
			
			for(int k=0; k<windowSize; k++){
				try{
					serverSocket.setSoTimeout(4000);
					serverSocket.receive(serverPacket);
					System.out.println("Received ack: "+new String(serverPacket.getData()));
					
				} catch (IOException e){
					System.out.println("Timeout lapsed. Resending window...");
					i--;
					break;
				}
			}
			
			i++;
		}
	}
	
	public static void main(String args[]) throws Exception{
	       UDPtoTCPServer server = new UDPtoTCPServer();
	       
	       server.waitConnection();
	    }
}
