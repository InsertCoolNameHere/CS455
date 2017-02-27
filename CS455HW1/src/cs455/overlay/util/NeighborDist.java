package cs455.overlay.util;

public class NeighborDist {
	private int dist;
	private int nodeId;
	
	public synchronized int getDist() {
		return dist;
	}
	public synchronized void setDist(int dist) {
		this.dist = dist;
	}
	public synchronized int getNodeId() {
		return nodeId;
	}
	public synchronized void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	

}
