package cs455.overlay.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cs455.overlay.commons.Neighbor;

public class GenerateOverlay {
	
	public static Map<String, List<Neighbor>> generateOverlay(int nodes, int neighbor) {
		Random rand = new Random();
		Map<String, List<Neighbor>> relation = new HashMap<String, List<Neighbor>>();
		for(int i=1;i<=nodes;i++) {
			int nextNeighbor = getNext(i, nodes);
			int nxt2Neighbor = get2Next(i,nodes);
			
			List<Neighbor> nbs = new ArrayList<Neighbor>();
			Neighbor n = new Neighbor();
			n.setNode(nextNeighbor);
			n.setWeight(rand.nextInt(10)+1);
			nbs.add(n);
			
			   
			Neighbor n1 = new Neighbor();
			n1.setNode(nxt2Neighbor);
			n1.setWeight(rand.nextInt(10)+1);
			nbs.add(n1);
		
			relation.put(String.valueOf(i), nbs);
			
		}
		
		return relation;
	}
	
	private static int getNext(int i, int nodes) {
		if(i == nodes) {
			return 1;
		} else {
			return i+1;
		}
	}
	
	private static int get2Next(int i, int nodes) {
		if(i >= nodes-1) {
			return i+2-nodes;
		} else {
			return i+2;
		}
	}
	
	public static String displayOverlay(Map<String, List<Neighbor>> relation) {
		String dis="";
		for(String s: relation.keySet()) {
			List<Neighbor> ns = relation.get(s);
			
			dis+=s+"===>";
			for(Neighbor n: ns) {
				dis+=n.getNode()+"("+n.getWeight()+")\t";
			}
			dis+="\n";
			
		}
		return dis;
	}
	
	public static void main(String arg[]) {
		Map<String, List<Neighbor>> relation = GenerateOverlay.generateOverlay(10, 4);
		
		for(String s: relation.keySet()) {
			List<Neighbor> ns = relation.get(s);
			
			System.out.print(s+"===>");
			for(Neighbor n: ns) {
				System.out.print(n.getNode()+"("+n.getWeight()+")\t");
			}
			System.out.println();
		}
		
	}

}
