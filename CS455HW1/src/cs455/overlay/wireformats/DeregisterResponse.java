package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeregisterResponse implements Event{
	 
	private int type;
	private String status;
	
	
	public DeregisterResponse() {}
	
	public DeregisterResponse(int type, String status) {
		this.type = type;
		this.status = status;
		
	}
	
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
	
	/*UNMARSHALLING*/
	
	public DeregisterResponse(byte[] msgBytes) {
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msgBytes);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			int statusLength = din.readInt();
			
			byte[] statusB = new byte[statusLength];
			
			din.readFully(statusB);
			
			status = new String(statusB);
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public int getType() {
		// TODO Auto-generated method stub
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
	
	

}
