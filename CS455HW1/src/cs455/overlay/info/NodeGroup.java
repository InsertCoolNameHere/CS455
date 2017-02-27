package cs455.overlay.info;

import java.util.ArrayList;
import java.util.List;

public class NodeGroup {
	
	private List<NodeInfo> nodes;

	
	public void addNode(NodeInfo n) {
		if(nodes == null) {
			nodes = new ArrayList<NodeInfo>();
		}
		nodes.add(n);
	}
	
	public List<NodeInfo> getNodes() {
		return nodes;
	}

	public void setNodes(List<NodeInfo> nodes) {
		this.nodes = nodes;
	}
	

}
