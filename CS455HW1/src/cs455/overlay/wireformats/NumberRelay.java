package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NumberRelay implements Event {

	private int type;
	private int num;
	private String path;
	
	public NumberRelay() {
	}
	
	public NumberRelay(byte[] msg) {
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msg);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			num = din.readInt();
			
			int pathLength = din.readInt();
			byte[] tmpByteArr = new byte[pathLength];
			din.readFully(tmpByteArr);
			path = new String(tmpByteArr);
			
			baInputStream.close();
			din.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(type);
			dout.writeInt(num);
			byte[] infoBytes = path.getBytes();
			int elementLength = infoBytes.length;
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

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return type;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
