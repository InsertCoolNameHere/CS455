package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskInitiation implements Event {

	private int type;
	private int rounds;
	
	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(type);
			
			dout.writeInt(rounds);
			
			dout.flush();
			marshalledBytes = baOutputStream.toByteArray();
			baOutputStream.close();
			dout.close();
			
			return marshalledBytes;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public TaskInitiation() {}
	
	public TaskInitiation(byte[] msg) {
		
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msg);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			rounds = din.readInt();
			
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

	public int getRounds() {
		return rounds;
	}

	public void setRounds(int rounds) {
		this.rounds = rounds;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
	public static void main(String arg[]) {
		int i=0;
		for (i=0;i<10;i++){
			if(i==6){
				break;
				
			}
		}
		System.out.println(i);
	}

}
