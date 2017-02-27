package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Register implements Event {
	
	private int msgType;
	private String ipAddress;
	private int serverPortNum;
	private int socketPortNum;

	public Register() {
		
	}
	/* Generate Marshalled byte from a request object
	 * */
	
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(msgType);
			byte[] ipBytes = ipAddress.getBytes();
			int elementLength = ipBytes.length;
			dout.writeInt(elementLength);
			dout.write(ipBytes);
			dout.writeInt(serverPortNum);
			dout.writeInt(socketPortNum);
			
			dout.flush();
			marshalledBytes = baOutputStream.toByteArray();
			baOutputStream.close();
			dout.close();
			return marshalledBytes;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * Umarshalls a byte array into a request object
	 * */
	
	public Register(byte[] msgBytes) {
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msgBytes);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			msgType = din.readInt();
			int ipLength = din.readInt();
			
			byte[] tmpByteArr = new byte[ipLength];
			
			din.readFully(tmpByteArr);
			
			ipAddress = new String(tmpByteArr);
			serverPortNum = din.readInt();
			socketPortNum = din.readInt();
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getServerPortNum() {
		return serverPortNum;
	}

	public void setServerPortNum(int portNum) {
		this.serverPortNum = portNum;
	}

	public int getSocketPortNum() {
		return socketPortNum;
	}

	public void setSocketPortNum(int socketPortNum) {
		this.socketPortNum = socketPortNum;
	}

}
