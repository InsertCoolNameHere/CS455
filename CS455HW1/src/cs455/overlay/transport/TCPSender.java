package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import cs455.overlay.node.Registry;
import cs455.overlay.util.LogFactory;

public class TCPSender {

	private Socket sendingSocket;
	private DataOutputStream dout;
	//private Logger logger;
	
	public TCPSender(Socket s, String loggerName) {
		//logger = LogFactory.getLogger(Registry.class.getName(), loggerName);
		this.sendingSocket = s;
		try {
			this.sendingSocket.setSendBufferSize(1000000);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			this.dout = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//logger.severe(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	public TCPSender(String loggerName) {
		//logger = LogFactory.getLogger(Registry.class.getName(), loggerName);
	}
	
	public void sendBytes(Socket s, byte[] msg) {
		try {
			DataOutputStream dd = new DataOutputStream(s.getOutputStream());
			int msgLen = msg.length;
			
			synchronized (this) {
				/*DataOutputStream dd = new DataOutputStream(s.getOutputStream());
				int msgLen = msg.length;*/
				dd.writeInt(msgLen);
				dd.write(msg, 0, msgLen);
				dd.flush();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//logger.severe(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	
	public void sendBytes(byte[] msg) {
		try {
			int msgLen = msg.length;
			dout.writeInt(msgLen);
			dout.write(msg,0,msgLen);
			dout.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//logger.severe(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}
