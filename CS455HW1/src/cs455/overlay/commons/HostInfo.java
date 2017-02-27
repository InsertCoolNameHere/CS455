package cs455.overlay.commons;

import java.net.Socket;

public class HostInfo {
	
	private int socketPortNo;
	private int serverSocketPortNo;
	private String ip;
	private Socket sock;
	
	public int getSocketPortNo() {
		return socketPortNo;
	}
	public void setSocketPortNo(int socketPortNo) {
		this.socketPortNo = socketPortNo;
	}
	public int getServerSocketPortNo() {
		return serverSocketPortNo;
	}
	public void setServerSocketPortNo(int serverSocketPortNo) {
		this.serverSocketPortNo = serverSocketPortNo;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Socket getSock() {
		return sock;
	}
	public void setSock(Socket sock) {
		this.sock = sock;
	}

}
