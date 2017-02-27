package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ConnectionRequest implements Event{
	
	private int type;
	private String message;
	private String info;
	
	public ConnectionRequest() {}
	
	public ConnectionRequest(byte[] msg) {
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msg);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			
			int messageLength = din.readInt();
			byte[] tmpByteArr = new byte[messageLength];
			din.readFully(tmpByteArr);
			message = new String(tmpByteArr);
			
			
			int infoLength = din.readInt();
			byte[] tmpByteArr1 = new byte[infoLength];
			din.readFully(tmpByteArr1);
			info = new String(tmpByteArr1);
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public synchronized String getMessage() {
		return message;
	}
	public synchronized void setMessage(String message) {
		this.message = message;
	}
	public synchronized String getInfo() {
		return info;
	}
	public synchronized void setInfo(String info) {
		this.info = info;
	}
	
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(type);
			
			byte[] msgBytes = message.getBytes();
			int elementLength = msgBytes.length;
			dout.writeInt(elementLength);
			dout.write(msgBytes);
			
			byte[] infoBytes = info.getBytes();
			int elementLength1 = infoBytes.length;
			dout.writeInt(elementLength1);
			dout.write(infoBytes);
			
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
		return type;
	}
	public synchronized void setType(int type) {
		this.type = type;
	}

}
