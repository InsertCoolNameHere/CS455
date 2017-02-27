package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LinkWeights implements Event{

	private int type;
	private int linkNum;
	private String linksInfo;
	
	@Override
	public byte[] getBytes() {
		byte[] marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		try {
			dout.writeInt(type);
			dout.writeInt(linkNum);
			byte[] infoBytes = linksInfo.getBytes();
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
	
	
	public LinkWeights() {}
	
	public LinkWeights(byte[] msg) {
		try {
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msg);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			type = din.readInt();
			linkNum = din.readInt();
			
			int infoLength = din.readInt();
			
			byte[] tmpByteArr = new byte[infoLength];
			
			din.readFully(tmpByteArr);
			
			linksInfo = new String(tmpByteArr);
			
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
		return 0;
	}

	public int getLinkNum() {
		return linkNum;
	}

	public void setLinkNum(int linkNum) {
		this.linkNum = linkNum;
	}

	public String getLinksInfo() {
		return linksInfo;
	}

	public void setLinksInfo(String linksInfo) {
		this.linksInfo = linksInfo;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	
	public static void main(String arg[]) {
		String paths="129.82.44.133:42663//129.82.44.126:42293";
		String myself = "129.82.44.133:42663";
		String[] tokens = paths.split("//");
		int i;
		
		for(i=0; i< tokens.length; i++) {
			System.out.println("??"+myself+"??");
			System.out.println("??"+tokens[i]+"??");
			if(myself.equals(tokens[i])) {
				System.out.println("HERE");
				break;
			}
		}
		
		System.out.println("MATCHED VALUE OF i: "+i);
		if(i==tokens.length-1){
			System.out.println("FINAL");
		} else if(i < tokens.length-1){
			System.out.println(tokens[i+1]);
		} else {
			System.out.println("ERROR");
		}
	}

}
