package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterResponse implements Event{
	private int type;
	private String status;
	private String infoMsg;
	
	public RegisterResponse(int type, String status, String infoMsg) {
		this.type = type;
		this.status = status;
		this.infoMsg = infoMsg;
		
	}
	
	/*
	 * Unmarshalls a byte array into a request object
	 * */
	
	public RegisterResponse(byte[] msgBytes) {
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msgBytes);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			int statusLength = din.readInt();
			
			byte[] statusB = new byte[statusLength];
			
			din.readFully(statusB);
			
			status = new String(statusB);
			
			int infoLength = din.readInt();
			byte[] infoB = new byte[infoLength];
			
			din.readFully(infoB);
			
			infoMsg = new String(infoB);
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getInfoMsg() {
		return infoMsg;
	}
	public void setInfoMsg(String infoMsg) {
		this.infoMsg = infoMsg;
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
			dout.writeInt(type);
			byte[] statusBytes = status.getBytes();
			int elementLength = statusBytes.length;
			dout.writeInt(elementLength);
			dout.write(statusBytes);
			
			byte[] infoBytes = infoMsg.getBytes();
			elementLength = infoBytes.length;
			dout.writeInt(elementLength);
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

}
