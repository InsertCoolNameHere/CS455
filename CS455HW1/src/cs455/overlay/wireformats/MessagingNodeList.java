package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessagingNodeList implements Event {
	
	private int type;
	private int totalNum;
	private String infos;
	
	public MessagingNodeList() {}
	
	public MessagingNodeList(byte[] msg) {
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msg);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			totalNum = din.readInt();
			int ipLength = din.readInt();
			
			byte[] tmpByteArr = new byte[ipLength];
			
			din.readFully(tmpByteArr);
			
			infos = new String(tmpByteArr);
			
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
			dout.writeInt(totalNum);
			
			byte[] ipBytes = infos.getBytes();
			int elementLength = ipBytes.length;
			dout.writeInt(elementLength);
			dout.write(ipBytes);
			
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

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public String getInfos() {
		return infos;
	}

	public void setInfos(String infos) {
		this.infos = infos;
	}

	public void setType(int type) {
		this.type = type;
	}

}
