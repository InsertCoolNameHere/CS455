package cs455.overlay.util;

import cs455.overlay.commons.HostInfo;

public class Edge {
	
	private HostInfo src;
	private HostInfo dest;
	private int weight;
	
	public Edge(HostInfo h1, HostInfo h2, int w) {
		src = h1;
		dest = h2;
		weight = w;
	}
	
	
	public HostInfo getSrc() {
		return src;
	}
	public void setSrc(HostInfo src) {
		this.src = src;
	}
	public HostInfo getDest() {
		return dest;
	}
	public void setDest(HostInfo dest) {
		this.dest = dest;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	

}
