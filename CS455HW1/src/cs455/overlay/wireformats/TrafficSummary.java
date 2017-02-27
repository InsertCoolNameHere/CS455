package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficSummary implements Event{

	private int type;
	private String ip;
	private int portNum;
	private long numMessagesSent;
	private long sumMessagesSent;
	private long numMesagesReceived;
	private long sumMessagesReceived;
	private long numMessagesRelayed;
	
	
	public TrafficSummary() {}
	
	public TrafficSummary(byte[] msg) {
		
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msg);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			
			int ipLength = din.readInt();
			byte[] tmpByteArr = new byte[ipLength];
			din.readFully(tmpByteArr);
			ip = new String(tmpByteArr);
			
			portNum = din.readInt();
			
			numMessagesSent = din.readLong();
			sumMessagesSent = din.readLong();
			numMesagesReceived = din.readLong();
			sumMessagesReceived = din.readLong();
			numMessagesRelayed = din.readLong();
			
			
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
			
			byte[] ipBytes = ip.getBytes();
			int elementLength = ipBytes.length;
			dout.writeInt(elementLength);
			dout.write(ipBytes);
			
			dout.writeInt(portNum);
			
			dout.writeLong(numMessagesSent);
			dout.writeLong(sumMessagesSent);
			dout.writeLong(numMesagesReceived);
			dout.writeLong(sumMessagesReceived);
			dout.writeLong(numMessagesRelayed);
			
			
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

	public  int getType() {
		return type;
	}

	public  void setType(int type) {
		this.type = type;
	}

	public  String getIp() {
		return ip;
	}

	public  void setIp(String ip) {
		this.ip = ip;
	}

	public  int getPortNum() {
		return portNum;
	}

	public  void setPortNum(int portNum) {
		this.portNum = portNum;
	}

	public  long getNumMessagesSent() {
		return numMessagesSent;
	}

	public  void setNumMessagesSent(long numMessagesSent) {
		this.numMessagesSent = numMessagesSent;
	}

	public  long getSumMessagesSent() {
		return sumMessagesSent;
	}

	public  void setSumMessagesSent(long sumMessagesSent) {
		this.sumMessagesSent = sumMessagesSent;
	}

	public  long getNumMesagesReceived() {
		return numMesagesReceived;
	}

	public  void setNumMesagesReceived(long numMesagesReceived) {
		this.numMesagesReceived = numMesagesReceived;
	}

	public  long getSumMessagesReceived() {
		return sumMessagesReceived;
	}

	public  void setSumMessagesReceived(long sumMessagesReceived) {
		this.sumMessagesReceived = sumMessagesReceived;
	}

	public  long getNumMessagesRelayed() {
		return numMessagesRelayed;
	}

	public  void setNumMessagesRelayed(long numMessagesRelayed) {
		this.numMessagesRelayed = numMessagesRelayed;
	}
}
