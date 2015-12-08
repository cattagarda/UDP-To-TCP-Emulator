//Reference: Computer Networking: A Top Down Approach, by Kurose and Ross
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

public class UDPtoTCPClient {
	int syn;
	int ack;
	int fin;
	int synNum;
	int ackNum;
	int windowSize = 3;

	static DatagramSocket clientSocket;
	static DatagramPacket clientPacket;
    static InetAddress IPAddress;
    static byte[] sendData = new byte[1024];
    static byte[] receiveData = new byte[1024];
    String status = "";
    int[] dropProbability = {0,25,50,75,100};
    
    public UDPtoTCPClient(){
    	try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			IPAddress = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	clientPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 10224);
    }
    
    public void initHandshake(){
    	Random random = new Random();
    	
    	synNum = random.nextInt(1024);
    	ackNum = 0;
    	syn = 1;
    	ack = 0;
    	fin = 0;
    	
    	status = synNum+"|"+ackNum+"|"+syn+"|"+ack+"|"+fin;
    	sendData = status.getBytes();
    	clientPacket.setData(sendData);
    	System.out.println("Sending: "+status);
    	
    	try {
			clientSocket.send(clientPacket);
			clientSocket.receive(clientPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	receiveData = clientPacket.getData();
    	System.out.println("Received <<< "+new String(receiveData));
    	
    	status = new String(receiveData);
    	String[] currentStatus = status.split("\\|");
    	System.out.println(currentStatus[0]);
    	
    	ackNum = Integer.parseInt(currentStatus[0]) + 1;
    	syn = 0;
    	ack = 1;
    	fin = 0;
    	
    	status = synNum+"|"+ackNum+"|"+syn+"|"+ack+"|"+fin;
    	System.out.println(status);
    	clientPacket.setData(status.getBytes());
    	
    	try {
			clientSocket.send(clientPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("Connection established! - Client~");
    }
	
    public void sendData(){
    	int index = 0;
    	Random randomize = new Random();
    	int dropPacket = dropProbability[randomize.nextInt(5)];
    	byte[] msg = new byte[1024];
    	
    	while(true){
    		try{
    			clientSocket.setSoTimeout(10000);
    			clientSocket.receive(clientPacket);
    			
    			String received = new String(clientPacket.getData());
    			System.out.println("Recieved from server: "+received);
    			
    			String[] stats = received.split("\\|");
    			int indexNo = Integer.parseInt(stats[0]);
    			
    			msg[indexNo] = stats[1].getBytes()[0];
    			index = indexNo + 1;
    			
    			if((index % windowSize == 0) || (index == windowSize)){
    				int indexJ = index - windowSize;
    				
    				for(;indexJ < index; indexJ++){
    					String response = "0|"+indexJ+"|0|1|0|"+windowSize;
    					byte[] sendBuffer = response.getBytes();
    					System.out.println("Sending response.. "+response);
    					
    					Random random2 = new Random();
    					int probability = random2.nextInt(100);

    					if(probability <= dropPacket){
    						System.out.println("The packet was dropped "+dropPacket+"%!");
    					} else {
    						clientPacket.setData(sendBuffer);
    						clientSocket.send(clientPacket);
    					}
    					
    					try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}
    			}
    			
    		} catch (IOException e){
    			String message = new String(msg);
    			System.out.println("Received message = "+message);
    			
    			break;
    		}
    	}
    	
    }
    
    public void fourWayHandshake(){
    	System.out.println("\n\nInitializing four way handshake..");
    	byte[] buff = new byte[1024];
    	
    	try {
			clientSocket.receive(clientPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
    	
    	buff = clientPacket.getData();
    	
    	String getDataString = new String(buff);
    	System.out.println("Recieved: "+getDataString);
    	String[] getFlags = getDataString.split("\\|");
    	
    	ackNum = Integer.parseInt(getFlags[0]);
    	ack = 1;
    	fin = 1;
    	
    	status = synNum+"|"+ackNum+"|"+syn+"|"+ack+"|"+fin;
    	buff = status.getBytes();
    	clientPacket.setData(buff);
    	System.out.println("Sending: "+status);
    	
    	try {
			clientSocket.send(clientPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	try {
			clientSocket.receive(clientPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	buff = new byte[1024];
    	buff = clientPacket.getData();
    	
    	status = new String(buff);
    	System.out.println("Received: "+status);
    	System.out.println("\n\nClosing connection...");
    	
    	clientSocket.close();
    }
    
	public static void main(String args[]) throws Exception{
		UDPtoTCPClient client = new UDPtoTCPClient();
		
		client.initHandshake();
		client.sendData();
		client.fourWayHandshake();
	   }
}
