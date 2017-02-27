package cs455.overlay.wireformats;

import java.io.DataInputStream;

public interface Event {
	
	/**
	 * Marshalling of request into bytes
	 * @return
	 */
	public byte[] getBytes();
	
	/**
	 * Returns the type of request
	 */
	public int getType();
	
	
}
