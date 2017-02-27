package cs455.overlay.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DecodeLinksInfo {
	
	private String srcNode;
	private int srcNodeInd;
	private String infoString;
	//private Map<Integer, List<Integer>> adjacencyList;
	private Map<Integer, List<NeighborDist>> adjacencyList;
	private List<String> nodes;
	private List<List<Integer>> history;
	private int totalNodes;
	private int totalLinks;
	List<Integer> traversal;
	Random rand;

	public DecodeLinksInfo(String infoString, String src, int totalLinks) {
		/*this.totalLinks = totalLinks;
		history = new ArrayList<List<Integer>>();
		nodes = new ArrayList<String>();
		totalNodes = countNodes(infoString);
		srcNodeInd = nodes.indexOf(src);
		System.out.println(srcNodeInd);
		adjacencyList = new HashMap<Integer, List<NeighborDist>>();
		this.infoString = infoString;
		this.srcNode = src;
		populateAdjacencyList();
		traversal = calculateShortestPath();*/
		startup(infoString, src, totalLinks);
	}
	
	public DecodeLinksInfo(){}
	
	
	public void startup(String infoString, String src, int totalLinks) {
		rand = new Random();
		this.totalLinks = totalLinks;
		history = new ArrayList<List<Integer>>();
		nodes = new ArrayList<String>();
		totalNodes = countNodes(infoString);
		srcNodeInd = nodes.indexOf(src);
		//System.out.println(srcNodeInd);
		adjacencyList = new HashMap<Integer, List<NeighborDist>>();
		this.infoString = infoString;
		this.srcNode = src;
		populateAdjacencyList();
		traversal = calculateShortestPath();
	}
	
	private int countNodes(String infoStr) {
		String[] lines = infoStr.split("\n");
		for(String line: lines) {
			if(line.trim().length() > 0) {
				String tokens[] = line.split(" ");
				String src = tokens[0];
				String dest = tokens[1];
				
				insertNode(src);
				insertNode(dest);
			}
		}
		
		return nodes.size();
	}

	private void populateAdjacencyList() {
		
		String[] lines = infoString.split("\n");
		for(String line: lines) {
			if(line.trim().length() > 0) {
				String tokens[] = line.split(" ");
				String src = tokens[0];
				String dest = tokens[1];
				
				int srcInd = insertNode(src);
				int destInd = insertNode(dest);
				
				int weight = Integer.valueOf(tokens[2]);
				
				addLinkToMap(srcInd, destInd, weight);
				addLinkToMap(destInd, srcInd, weight);
				
			}
		}
	}
	
	private void addLinkToMap(int src, int dest, int weight) {
		if(adjacencyList.get(src) == null) {
			adjacencyList.put(src, new ArrayList<NeighborDist>());
		}
		
		List<NeighborDist> ns = adjacencyList.get(src);
		NeighborDist n = new NeighborDist();
		n.setNodeId(dest);
		n.setDist(weight);
		
		ns.add(n);
		adjacencyList.put(src, ns);
	}
	
	private List<Integer> calculateShortestPath() {
		
		/* The distance row as it gets updated */
		List<Integer> dist = new ArrayList<Integer>();
		
		/* List of nodes still to visit */
		List<Integer> queue = new ArrayList<Integer>();
		
		for(int i=0; i< totalNodes; i++) {
			queue.add(i);
			if(i == srcNodeInd) {
				dist.add(0);
			} else {
				dist.add(Integer.MAX_VALUE);
			}
		}
		
		/* S: Visited Nodes */
		List<Integer> S = new ArrayList<Integer>();
		
		
		history.add(new ArrayList<Integer>(dist));
		
		while(queue.size() > 0) {
			
			int u = getIndexWithMinimumDist(queue, dist);
			
			//System.out.println("SELECTED: "+ u);
			S.add(u);
			
			for(NeighborDist n : adjacencyList.get(u)) {
				int v = n.getNodeId();
				if(dist.get(v) > dist.get(u)+ n.getDist()) {
					int newdist = dist.get(u)+ n.getDist();
					dist.set(v, newdist);
				}
			}
			
			
			history.add(new ArrayList<Integer>(dist));
			//System.out.println(dist);
			
			//update Neighbors
			
		}
		
		return S;
	}
	
	
	
	/**
	 * This method returns the smallest value in the distance vector
	 * @param queue List of nodes still to visit
	 * @param dist The distance row as it gets updated
	 * @return
	 */
	private int getIndexWithMinimumDist(List<Integer> queue, List<Integer> dist) {
		
		int len = Integer.MAX_VALUE;
		int index = -1;
		
		for(int i=0; i < totalNodes; i++) {
			if(queue.contains(i)) {
				if(dist.get(i) < len) {
					index = i;
					len = dist.get(i);
				}
			}
		}
		
		if(index >= 0) {
			int rmInd = queue.indexOf(index);
			if(rmInd>=0) {
				queue.remove(rmInd);
				return index;
			}
			
		}
		return -1;
	}

	private String printAdjacencyList() {
		String str="";
		for(int i=0; i< totalNodes; i++) {
			str+=i+">";
			for(NeighborDist n: adjacencyList.get(i)) {
				str+="("+n.getNodeId()+","+n.getDist()+") ";
			}
			str+="\n";
		}
		return str;
	}
	
	private int insertNode(String n) {
		if(!nodes.contains(n)) {
			nodes.add(n);
		}
		return nodes.indexOf(n);
		
	}

	private String printNodes() {
		// TODO Auto-generated method stub
		int i=0;
		String str="";
		for(String n: nodes){
			str+=i+">"+n+"\n";
			i++;
		}
		return str;
	}
	
	public List<String> getShortestPath(String destStr) {
		
		int dest = nodes.indexOf(destStr);
		int lastVal = history.get(history.size()-1).get(dest);
		int src = srcNodeInd;
		List<Integer> path = new ArrayList<Integer>();
		path.add(dest);
		
		for(int i=history.size()-2; i>=0;i--){
			List<Integer> l= history.get(i);
			
			if(l.get(dest) > lastVal) {
				
				path.add(traversal.get(i));
				
				dest = traversal.get(i);
				lastVal = l.get(dest);
			}
		}
		Collections.reverse(path);
		List<String> pathStr = new ArrayList<String>();
		
		for(Integer i: path) {
			
			pathStr.add(nodes.get(i));
		}
		return pathStr;
	}
	
	
	private List<Integer> getShortestPath(int dest) {
		
		int lastVal = history.get(history.size()-1).get(dest);
		int src = srcNodeInd;
		List<Integer> path = new ArrayList<Integer>();
		path.add(dest);
		
		for(int i=history.size()-2; i>=0;i--){
			List<Integer> l= history.get(i);
			
			if(l.get(dest) > lastVal) {
				
				path.add(traversal.get(i));
				dest = traversal.get(i);
				lastVal = l.get(dest);
				//System.out.println(traversal.get(i));
			}
		}
		
		Collections.reverse(path);
		return path;
	}
	
	
	public String printAllShortestPaths() {
		
		String info="";
		for(int i=0; i< totalNodes; i++) {
			if(i != srcNodeInd) {
				List<Integer> shortestPath = getShortestPath(i);
				
				String pathstr = decodeShortestPath(shortestPath);
				System.out.println(pathstr);
			}
		}
		
		
		return "";
	}
	
	private String decodeShortestPath(List<Integer> shortestPath){
		
		String pathStr="";
		
		int s = shortestPath.get(0);
		pathStr+=getNodeName(nodes.get(s));
		
		for(int i=1;i< shortestPath.size();i++) {
			int d = shortestPath.get(i);
			int weight = getLinkWeight(s,d);
			
			pathStr+="--"+weight+"--"+getNodeName(nodes.get(d));
			
			s=d;
			
		}
		
		return pathStr;
	}
	
	
	private String getNodeName(String str) {
		
		
		String[] tokens = str.split(":");
		
		String ip = tokens[0];
		
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(ip);
			String host = addr.getHostName();
			return host+":"+tokens[1];
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			
			System.out.println("ERROR IN DIJKSTRA");
			e.printStackTrace();
		}
		
		
		return null;
	}

	private int getLinkWeight(int s, int d) {
		
		List<NeighborDist> list = adjacencyList.get(s);
		for(NeighborDist n: list) {
			if(n.getNodeId() == d){
				return n.getDist();
			}
			
		}
		return 0;
	}

	public String getRandNode() {
		// TODO Auto-generated method stub
		int indx = srcNodeInd;
		while(indx==srcNodeInd){
			indx = rand.nextInt(totalNodes);
		}
		
		return nodes.get(indx);
	}
	
	public static void main(String[] args) {
		//String me="129.82.44.133:38629";
		String me="129.82.44.152:42251";
		//String me="1";
		//String info="1 2 2\n2 3 14\n2 4 5\n2 5 4\n1 4 5\n4 5 58\n3 5 34";
		
		String info = "129.82.44.152:42251 129.82.44.172:35171 9\n"
				+ "129.82.44.152:42251 129.82.44.133:38629 5\n"
				+ "129.82.44.172:35171 129.82.44.133:38629 5\n"
				+ "129.82.44.172:35171 129.82.44.141:46671 4\n"
				+ "129.82.44.133:38629 129.82.44.141:46671 1\n"
				+ "129.82.44.133:38629 129.82.44.126:44009 3\n"
				+ "129.82.44.141:46671 129.82.44.126:44009 3\n"
				+ "129.82.44.141:46671 129.82.44.152:42251 8\n"
				+ "129.82.44.126:44009 129.82.44.152:42251 2\n"
				+ "129.82.44.126:44009 129.82.44.172:35171 2";
				
		
		DecodeLinksInfo d = new DecodeLinksInfo(info, me, 10);
		
		System.out.println(d.printNodes());
		
		//System.out.println(d.printAdjacencyList());
		//List<Integer> shortestPath = d.calculateShortestPath();
		//System.out.println("\n\n"+shortestPath+"\n\n");
		/*for(List<Integer> l: d.history) {
			System.out.println(l);
		}*/
		
		int dest = 3;
		
		d.printAllShortestPaths();
		System.out.println(d.getShortestPath("129.82.44.172:35171"));
		
		//System.out.println(d.getRandNode());
		//List<Integer> shortestPath2 = d.getShortestPath("4");
		//System.out.println(shortestPath2);
		// TODO Auto-generated method stub

	}

	public synchronized List<String> getNodes() {
		return nodes;
	}

	public synchronized void setNodes(List<String> nodes) {
		this.nodes = nodes;
	}

	

	

}
