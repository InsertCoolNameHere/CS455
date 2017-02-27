package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import cs455.overlay.commons.HostInfo;
import cs455.overlay.commons.Neighbor;
import cs455.overlay.info.NodeGroup;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServer;
import cs455.overlay.util.ConsoleReader;
import cs455.overlay.util.Edge;
import cs455.overlay.util.GenerateOverlay;
import cs455.overlay.util.LogFactory;
import cs455.overlay.wireformats.DeregisterRequest;
import cs455.overlay.wireformats.DeregisterResponse;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessagingNodeList;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.PullTrafficSummaryReq;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.TaskInitiation;
import cs455.overlay.wireformats.TrafficSummary;

public class Registry implements Node {

	private ServerSocket registryServerSocket;
	
	private NodeGroup nodeGroup;
	
	private EventFactory eventFactory;
	
	private static Logger logger;
	
	private int registryServerPort;
	
	private Map<String,HostInfo> knownHosts;
	
	private List<String> hostsWhoHaveFinished;
	
	private List<String> hostsWhoHaveSummarised;
	
	private Map<String,List<TrafficSummary>> hostToTrafficSummaryListMap;
	
	private Map<String, Socket> addrToSocketMap;
	
	private TCPServer registryServer;
	
	private Map<Integer, List<Edge>> nodeToEdgeMap;
	
	
	public Registry(int registryPort) {
		try {
			
			logger = LogFactory.getLogger(Registry.class.getName(), "overlay-registry.out");
			
			logger.info("Logger Initialized in Registry Node.");
			
			knownHosts = new HashMap<String,HostInfo>();
			/* Getting Singleton Instance from EventFactory */
			eventFactory = EventFactory.getInstance();
			
			setNodeGroup(new NodeGroup());
			registryServerPort = registryPort;
			
			/*======================CREATING NEW SERVER SOCKET FOR REGISTRY==========================*/
			
			setRegistryServerSocket(new ServerSocket(registryPort));
			logger.info("ServerSocket created at port"+ registryServerSocket.getLocalPort());
			
			addrToSocketMap = new HashMap<String, Socket>();
			hostsWhoHaveFinished = new ArrayList<String>();
			hostsWhoHaveSummarised = new ArrayList<String>();
			hostToTrafficSummaryListMap = new HashMap<String, List<TrafficSummary>>();
			
			logger.info("Registry Node Initialization Successful");
			nodeToEdgeMap = new HashMap<Integer, List<Edge>>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	/* Socket s is the registry side Socket object on which we are to respond */
	public void onEvent(Event e, Socket s) {
		// TODO Auto-generated method stub
		logger.info("Registry Node received SOMETHING");
		if(Register.class.toString().equals(e.getClass().toString())) {
			Register regReq = (Register)e;
			logger.info("Registry Node received a RegisterRequest from "+regReq.getIpAddress()+":"+regReq.getSocketPortNum());
			handleRegisterRequest(regReq,s);
			
		} else if(DeregisterRequest.class.toString().equals(e.getClass().toString())) {
			
			DeregisterRequest req = (DeregisterRequest)e;
			logger.info("Registry Node received a DeregisterRequest from "+req.getIp()+":"+req.getPort());
			handleDeregisterRequest(req, s);
			
		} else if(TaskComplete.class.toString().equals(e.getClass().toString())) {
			TaskComplete req = (TaskComplete)e;
			logger.info("REGISTRY RECEIVED A TASK COMPLETE MESSAGE. VALIDATING IT RIGHT NOW");
			handleTaskCompleteResponse(req, s);
			
		} else if(TrafficSummary.class.toString().equals(e.getClass().toString())) {
			TrafficSummary tr = (TrafficSummary)e;
			handleTaskCompleteSummaryRcvd(tr, s);
		}

	}
	
	
	/* HANDLING AS EACH SUMMARY GETS RECEIVED FROM EACH NODE */
	private synchronized void handleTaskCompleteSummaryRcvd(TrafficSummary req, Socket s) {
		// TODO Auto-generated method stub
		String key = req.getIp()+":"+req.getPortNum();
		
		/*logger.info("SUMMARY RECEIVER GOT: "+req.getIp()+":"+req.getPortNum()+"\t"+req.getNumMessagesSent()+"\t"+req.getNumMesagesReceived()+"\t"+req.getSumMessagesSent()
				+"\t"+req.getSumMessagesReceived()+"\t"+req.getNumMessagesRelayed());*/
		
		if(!isEntryInHostsWhoHaveSummarized(key) && searchKnownHosts(key) != null) {
			hostsWhoHaveSummarised.add(key);
			
			List<TrafficSummary> summaries;
			
			if(hostToTrafficSummaryListMap.get(key) == null) {
				summaries = new ArrayList<TrafficSummary>();
			} else {
				summaries = hostToTrafficSummaryListMap.get(key);
			}
			summaries.add(req);
			hostToTrafficSummaryListMap.put(key, summaries);
			
			
			logger.info("I KNOW THIS MESSAGING NODE");
		}
		
		/* REGISTRY HAS RECEIVED TASK COMPLETE FROM ALL MSG NODES*/
		if(hostsWhoHaveSummarised.size() == knownHosts.size()) {
			logger.info("I HAVE RECEIVED SUMMARY FROM EVERYONE");
			setHostsWhoHaveSummarized(new ArrayList<String>());
			
			printFullSummary();
		}
		
	}
	
	
	
	
	private synchronized void printFullSummary() {
		String summaryStr="\n\nSUMMARY:\n=========================\n\n";
		
		long numMessagesSentF = 0;
		long numMessagesReceivedF = 0;
		long sumMessagesSentF = 0;
		long sumMessagesReceivedF = 0;
		
		for(String k: hostToTrafficSummaryListMap.keySet()) {
			long numMessagesSent = 0;
			long numMessagesReceived = 0;
			long sumMessagesSent = 0;
			long sumMessagesReceived = 0;
			long numMessagesRelayed = 0;
			
			for(TrafficSummary ts: hostToTrafficSummaryListMap.get(k)) {
				numMessagesSent+=ts.getNumMessagesSent();
				numMessagesReceived+=ts.getNumMesagesReceived();
				sumMessagesSent+=ts.getSumMessagesSent();
				sumMessagesReceived+=ts.getSumMessagesReceived();
				numMessagesRelayed+=ts.getNumMessagesRelayed();
			}
			
			summaryStr+=k+"\t"+numMessagesSent+"\t"+numMessagesReceived+"\t"+sumMessagesSent
					+"\t"+sumMessagesReceived+"\t"+numMessagesRelayed+"\n";
			
			numMessagesSentF+=numMessagesSent;
			numMessagesReceivedF+=numMessagesReceived;
			sumMessagesSentF+=sumMessagesSent;
			sumMessagesReceivedF+=sumMessagesReceived;
		}
		summaryStr+="\n=================================================================================================================\n";
		summaryStr+="SUM      "+"\t"+numMessagesSentF+"\t"+numMessagesReceivedF+"\t"+sumMessagesSentF
				+"\t"+sumMessagesReceivedF+"\t"+"\n";
		
		System.out.println(summaryStr);
		
	}

	private synchronized void handleTaskCompleteResponse(TaskComplete req, Socket s) {
		// TODO Auto-generated method stub
		String key = req.getIp()+":"+req.getSockPortNum();
		if(!isEntryInHostsWhoHaveFinished(key) && searchKnownHosts(key) != null) {
			hostsWhoHaveFinished.add(key);
			logger.info("I KNOW THIS MESSAGING NODE");
		}
		
		/* REGISTRY HAS RECEIVED TASK COMPLETE FROM ALL MSG NODES*/
		if(hostsWhoHaveFinished.size() == knownHosts.size()) {
			logger.info("I HAVE RECEIVED ACK FROM EVERYONE");
			setHostsWhoHaveFinished(new ArrayList<String>());
			
			/*WAIT 15 secs*/
			try {
				Thread.sleep(1000*30);
			} catch (Exception e) {
				System.out.println(e);
			}
			
			/* SENDING REQUEST FOR SUMMARY TO ALL MSG NODES*/
			handleSummaryGrabRequest();
		}
		
	}
	
	private void handleSummaryGrabRequest() {
		
		PullTrafficSummaryReq req = new PullTrafficSummaryReq();
		req.setType(Protocol.PULL_TRAFFIC_SUMMARY.typeCode());
		
		byte[] msg = req.getBytes();
		Collection<HostInfo> hosts = knownHosts.values();
		
		for(HostInfo h: hosts) {
			Socket sock = h.getSock();
			
			TCPSender sender = new TCPSender(sock,"overlay-registry.out");
			
			sender.sendBytes(msg);
		}
		logger.info("RESGITER SENT OUT SUMMARY GRAB REQUEST");
		
	}

	public synchronized boolean isEntryInHostsWhoHaveFinished(String key) {
		if(hostsWhoHaveFinished.contains(key)) {
			return true;
		}
		return false;
	}
	
	public synchronized boolean isEntryInHostsWhoHaveSummarized(String key) {
		if(hostsWhoHaveFinished.contains(key)) {
			return true;
		}
		return false;
	}

	public synchronized void handleRegisterRequest(Register r, Socket s) {
		String key = r.getIpAddress()+":"+ r.getSocketPortNum();
		RegisterResponse rsp;
		
		if(searchKnownHosts(key)!= null){
			String info="NODE BY THIS IP:PORT ALREADY EXISTS";
			rsp = new RegisterResponse(Protocol.REGISTRATION_RESPONSE.typeCode(), "FAILURE", info);
			
		} else {
			HostInfo h = new HostInfo();
			h.setIp(r.getIpAddress());
			h.setSocketPortNo(r.getSocketPortNum());
			h.setServerSocketPortNo(r.getServerPortNum());
			if(addrToSocketMap.get(key) == null) {
				logger.severe("REQUEST PORT INFO DOES NOT MATCH WITH PORT IN ADDRTOSOCKET MAP");
			}
			h.setSock(addrToSocketMap.get(key));
			//knownHosts.put(key,h);
			addKnownHost(key,h);
			String info="WELCOME TO THE CLUSTER ! THERE ARE CURRENTLY "+getNumKnownHosts()+" NODE(S) IN THE REGISTRY, INCLUDING YOU.";
			rsp = new RegisterResponse(Protocol.REGISTRATION_RESPONSE.typeCode(), "SUCCESS", info);
			
		}
		byte[] msg = rsp.getBytes();
		TCPSender sender = new TCPSender(s,"overlay-registry.out");
		
		sender.sendBytes(msg);
	}
	
	private synchronized HostInfo searchKnownHosts(String key) {
		// TODO Auto-generated method stub
		return knownHosts.get(key);
	}

	private synchronized void addKnownHost(String key, HostInfo h) {
		knownHosts.put(key,h);
	}

	/* SENDS OUT A DEREGISTER RESPONSE TO THE MESSAGING NODE */
	
	public void handleDeregisterRequest(DeregisterRequest r, Socket s) {
		/* THE IP AND PORT RECEIVED IS THE IP AND PORT OF THE MESSAGING NODE SIDE SOCKET */
		String key = r.getIp()+":"+ r.getPort();
		
		boolean validity = checkValidityDreq(r.getIp(), r.getPort(), key, s);
		DeregisterResponse rsp;
		if(!validity){
			
			String info="INVALID DEREGISTRATION REQUEST";
			rsp = new DeregisterResponse(Protocol.DEREGISTER_RESPONSE.typeCode(), info);
			
		} else {
			String info="SUCCESS. PLEASE REMOVE YOURSELF.";
			rsp = new DeregisterResponse(Protocol.DEREGISTER_RESPONSE.typeCode(), info);
			
		}
		byte[] msg = rsp.getBytes();
		logger.info("DEREGISTRATION_RESPONSE SENT OUT FROM REGISTER...");
		TCPSender sender = new TCPSender(s,"overlay-registry.out");
		
		sender.sendBytes(msg);
		
		if(validity) {
			removeKnownHostEntry(key);
			/*try {
				s.close();
				
				logger.info("SOCKET TO CLIENT MESSAGING NODE CLOSED");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.severe("SOCKET TO CLIENT MESSAGING NODE NOT CLOSED");
				e.printStackTrace();
			}*/
		}
	}
	
	
	/* CHECKS VALIDITY OF DEREGISTER REQUEST */
	
	public boolean checkValidityDreq(String ip, int port, String key, Socket s) {
		
		String remoteIp = s.getInetAddress().toString().split("/")[1];
		if(!ip.equals(remoteIp)) {
			return false;
		}
		
		if(!findKnownHostEntry(key)) {
			return false;
		}
		return true;
	}
	
	public synchronized boolean findKnownHostEntry(String key) {
		
		if(knownHosts.get(key) != null) {
			return true;
		}
		
		return false;
	}
	
	public synchronized void removeKnownHostEntry(String key) {
		knownHosts.remove(key);
	}
	
	
	public void startup() {
		/* This will be the main registry server with
		 * ServerSocket object that waits listening for incoming connections */
		
		/*=======================STARTING UP REGISTRY SERVER THREAD=================================*/
		registryServer = new TCPServer(getRegistryServerSocket(), getNodeGroup(), this, addrToSocketMap);
		
		/* This is the Registry Server Thread that listens to incoming connections */
		Thread registryServerThread = new Thread(registryServer,"RegistryReceiverThread");
		
		registryServerThread.start();
		
		/*=======================STARTING UP CONSOLE READER THREAD===================================*/
		ConsoleReader consoleReader = new ConsoleReader(this);
		Thread consoleReaderThread = new Thread(consoleReader, "ConsoleReaderThread");
		
		consoleReaderThread.start();
		
		try {
			
			registryServerThread.join();
			consoleReaderThread.join();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String arg[]) {
		
		int registryPort = Integer.parseInt(arg[0]);
		
		Registry r = new Registry(registryPort);
		
		r.startup();
		
		/* REST OF MAIN METHOD */
		
	}

	public ServerSocket getRegistryServerSocket() {
		return registryServerSocket;
	}

	public void setRegistryServerSocket(ServerSocket registryServerSocket) {
		this.registryServerSocket = registryServerSocket;
	}

	public NodeGroup getNodeGroup() {
		return nodeGroup;
	}

	public void setNodeGroup(NodeGroup nodeGroup) {
		this.nodeGroup = nodeGroup;
	}

	@Override
	public void onCommand(String s) {
		// TODO Auto-generated method stub
		if("list-messaging nodes".equals(s)){
			displayHosts();
		} else if(s.contains("setup-overlay")) {
			handleOverlayCreation(addrToSocketMap, s);
		} else if(s.contains("send-overlay-link-weights")) {
			handleLinkSendingToEveryone();
		} else if(s.contains("start")){
			handleTaskInitiation(s);
		} else if(s.equals("list-weights")) {
			printLinkWeightsMessage(nodeToEdgeMap.values());
		}
	}
	
	
	private void handleTaskInitiation(String s) {
		int numRounds = Integer.valueOf(s.split(" ")[1]);
		TaskInitiation task = new TaskInitiation();
		task.setType(Protocol.TASK_INITIATE.typeCode());
		task.setRounds(numRounds);
		
		byte[] msg = task.getBytes();
		Collection<HostInfo> hosts = knownHosts.values();
		Collection<List<Edge>> edges = nodeToEdgeMap.values();
		
		for(HostInfo h: hosts) {
			Socket sock = h.getSock();
			
			TCPSender sender = new TCPSender(sock,"overlay-registry.out");
			
			sender.sendBytes(msg);
		}
		logger.info("REGISTER SENT OUT TASK INITIATION");
		
	}

	/* INFORMING ALL NODES OF LINKS */
	private void handleLinkSendingToEveryone() {
		// TODO Auto-generated method stub
		Collection<HostInfo> hosts = knownHosts.values();
		Collection<List<Edge>> edges = nodeToEdgeMap.values();
		
		LinkWeights req = generateLinkWeightsMessage(edges);
		
		for(HostInfo h: hosts) {
			Socket s = h.getSock();
			
			TCPSender sender = new TCPSender(s,"overlay-registry.out");
			
			sender.sendBytes(req.getBytes());
			logger.info("RESGITER SENT OUT LINK WEIGHTS TO EVERYONE");
		}
	}

	private LinkWeights generateLinkWeightsMessage(Collection<List<Edge>> edges) {
		LinkWeights req = new LinkWeights();
		req.setType(Protocol.Link_Weights.typeCode());
		int totalLinks = 0;
		String info="";
		for(List<Edge> es: edges) {
			totalLinks+=es.size();
			for(Edge e: es) {
				HostInfo src = e.getSrc();
				HostInfo dest = e.getDest();
				int weight = e.getWeight();
				
				String line = src.getIp()+":"+src.getServerSocketPortNo()+" "+dest.getIp()+":"+dest.getServerSocketPortNo()+
						" "+weight+"\n";
				
				info+=line;
			}
			
		}
		req.setLinkNum(totalLinks);
		req.setLinksInfo(info);
		logger.info("LINK STRING=======");
		logger.info(info);
		return req;
	}
	
	
	private void printLinkWeightsMessage(Collection<List<Edge>> edges) {
		
		int totalLinks = 0;
		String info="";
		for(List<Edge> es: edges) {
			totalLinks+=es.size();
			for(Edge e: es) {
				HostInfo src = e.getSrc();
				HostInfo dest = e.getDest();
				int weight = e.getWeight();
				
				InetAddress dummySrc = null;
				InetAddress dummyDest = null;
				try {
					dummySrc = InetAddress.getByName(src.getIp());
					dummyDest = InetAddress.getByName(dest.getIp());
				} catch (UnknownHostException e1) {
					logger.severe("PROBLEMS CONVERTING TO HOSTNAME");
					e1.printStackTrace();
				}
				
				String line = dummySrc.getHostName()+":"+src.getServerSocketPortNo()+" "+dummyDest.getHostName()+":"+dest.getServerSocketPortNo()+
						" "+weight+"\n";
				
				info+=line;
			}
			
		}
		System.out.println("WEIGHTS\n============\n");
		System.out.println(info);
	}

	private void handleOverlayCreation(Map<String, Socket> addrSockMap, String cmd) {
		
		int numHosts = getNumKnownHosts();
		
		if(cmd.contains(" ")){
			String numConn = cmd.split(" ")[1];
			if(numConn.matches("^-?\\d+$")) {
				int num = Integer.valueOf(numConn.trim());
				
				if(num != 4) {
					System.out.println("CONNECTION LENGTH OF ONLY 4 SUPPORTED");
					return;
				}
				
				Map<String, List<Neighbor>> relation = GenerateOverlay.generateOverlay(numHosts, 4);
				logger.info("OVERLAY GENERATED:\n================\n");
				logger.info(GenerateOverlay.displayOverlay(relation));
				
				List<HostInfo> hosts = new ArrayList<HostInfo>(getKnownHosts().values());
				
				for(String node: relation.keySet()) {
					int nodeInd = Integer.valueOf(node);
					nodeToEdgeMap.put(nodeInd, new ArrayList<Edge>());
					HostInfo srcNode = hosts.get(nodeInd-1);
					for(Neighbor n: relation.get(node)) {
						int weight = n.getWeight();
						int neighborInd = n.getNode();
						HostInfo neighbor = hosts.get(neighborInd-1);
						Edge e = new Edge(srcNode, neighbor, weight);
						nodeToEdgeMap.get(nodeInd).add(e);
					}
					
				}
				
				for(Integer n: nodeToEdgeMap.keySet()){
					
					logger.info(n+"===>"+nodeToEdgeMap.get(n).size()+" entries");
					List<Edge> edges = nodeToEdgeMap.get(n);
					createandSendMessagingNodesListRequest(edges);
				}
				
				
			} else {
				System.out.println("INVALID COMMAND ENTERED");
				return;
			}
		}
		
	}
	
	

	public void createandSendMessagingNodesListRequest(List<Edge> edges) {
		
		Socket senderSocket = edges.get(0).getSrc().getSock();
		
		MessagingNodeList req = new MessagingNodeList();
		req.setType(Protocol.MESSAGING_NODES_LIST.typeCode());
		req.setTotalNum(edges.size());
		
		String info="";
		for(Edge e: edges) {
			int weight = e.getWeight();
			HostInfo dest = e.getDest();
			info+=dest.getIp()+":"+dest.getSocketPortNo()+":"+dest.getServerSocketPortNo()+":"+weight+"\n";
			
		}
		req.setInfos(info);
		
		TCPSender sender = new TCPSender(senderSocket,"overlay-registry.out");
		
		sender.sendBytes(req.getBytes());
		logger.info("RESGITER SENT OUT MESSAGING NODES LIST");
		
	}
	
	
	private void displayHosts() {
		
		System.out.println("\n\nHOSTS IN THE OVERLAY\n========================\n");
		for(HostInfo h: getKnownHosts().values()) {
			InetAddress iaddr;
			try {
				iaddr = InetAddress.getByName(h.getIp());
				//System.out.println(iaddr.getHostName());
				System.out.println(iaddr.getHostName()+":"/*+h.getSocketPortNo()+":"*/+h.getServerSocketPortNo());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				logger.info("ERROR OCCURED CONVERTING IP ADDRESS");
				e.printStackTrace();
			}
			
		}
		System.out.println("\n========================\n");
	}
	
	public synchronized int getNumKnownHosts() {
		return knownHosts.size();
	}
	
	public synchronized Map<String, HostInfo> getKnownHosts() {
		return knownHosts;
	}

	public synchronized void setKnownHosts(Map<String, HostInfo> knownHosts) {
		this.knownHosts = knownHosts;
	}

	public synchronized Map<String, Socket> getAddrToSocketMap() {
		return addrToSocketMap;
	}
	
	public synchronized void addToAddrToSocketMap(String key, Socket sock) {
		addrToSocketMap.put(key, sock);
	}

	public synchronized void setAddrToSocketMap(Map<String, Socket> addrToSocketMap) {
		this.addrToSocketMap = addrToSocketMap;
	}

	public synchronized void setHostsWhoHaveFinished(List<String> hostsWhoHaveFinished) {
		this.hostsWhoHaveFinished = hostsWhoHaveFinished;
	}
	
	public synchronized void setHostsWhoHaveSummarized(List<String> hostsWhoHaveFinished) {
		this.hostsWhoHaveSummarised = hostsWhoHaveFinished;
	}

}
