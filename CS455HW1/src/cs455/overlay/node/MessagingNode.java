package cs455.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import cs455.overlay.commons.HostInfo;
import cs455.overlay.transport.TCPReceiver;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServer;
import cs455.overlay.util.LogFactory;
import cs455.overlay.util.ConsoleReader;
import cs455.overlay.util.DecodeLinksInfo;
import cs455.overlay.wireformats.ConnectionRequest;
import cs455.overlay.wireformats.DeregisterRequest;
import cs455.overlay.wireformats.DeregisterResponse;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessagingNodeList;
import cs455.overlay.wireformats.NumberRelay;
import cs455.overlay.wireformats.Protocol;
import cs455.overlay.wireformats.PullTrafficSummaryReq;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.TaskInitiation;
import cs455.overlay.wireformats.TrafficSummary;

public class MessagingNode implements Node {
	
	private String registryHostIP;
	private String registryPort;
	/* This is a single ServerSocket object responsible for listening to any
	 * Request coming in to this Messaging node */
	private ServerSocket server;
	
	/* THIS IS THE SOCKET USED TO COMMUNICATE WITH THE REGISTRY */
	private Socket socketToRegister; 
	/* THIS IS THE SOCKET PORT FOR THE ABOVE*/
	private int socketToRegisterPort;
	
	/* THIS IS NEEDED TO INFORM OTHER MESSAGING NODES ABOUT THE SERVERSOCKET OF THIS MESSAGING NODE*/
	private int serverPort;
	private EventFactory eventFactory;
	private static Logger logger;
	/* Hostname of the messaging node */
	public String hostName;
	/* Ip of the Messaging Node */
	public String localIP;
	ConsoleReader consoleReader ;
	TCPReceiver receiverFromServer;
	TCPServer messageNodeServer;
	private DecodeLinksInfo dijk;
	
	private long numMessagesSent;
	private long numMessagesReceived;
	private long sumMessagesSent;
	private long sumMessagesReceived;
	private long numMessagesRelayed;
	
	private synchronized void incrementNumMessagesSent(int i) {
		numMessagesSent+=i;
	}
	private synchronized void incrementNumMessagesReceived(int i) {
		numMessagesReceived+=i;
	}
	private synchronized void incrementSumMessagesSent(int i) {
		sumMessagesSent+=i;
	}
	private synchronized void incrementSumMessagesReceived(int i) {
		sumMessagesReceived+=i;
	}
	private synchronized void incrementnNumMessagesRelayed(int i) {
		numMessagesRelayed+=i;
	}
	
	/* A map of known hosts. Key is ip:listeningSocketPort */
	private Map<String, HostInfo> knownHostsMap;
	
	/* A map of receiver threads this node has got opened for each of neighbor*/
	private List<TCPReceiver> receiverThreadList;
	private final TCPSender senderFinal;
	

	private void initializeCounters() {
		numMessagesSent=0;
		numMessagesReceived=0;
		sumMessagesSent=0;
		sumMessagesReceived=0;
		numMessagesRelayed=0;
	}
	public MessagingNode(String registryHost,String registryPort) throws UnknownHostException {
		initializeCounters();
		hostName = InetAddress.getLocalHost().getHostName();
		localIP = InetAddress.getLocalHost().getHostAddress();
		logger = LogFactory.getLogger(MessagingNode.class.getName(), "overlay-messagingNode-"+hostName+".out");
		
		logger.info("Logger Initialized in Messaging Node on"+hostName);
		senderFinal = new TCPSender("overlay-messagingNode-"+hostName+".out");
		
		String hostStr = InetAddress.getByName(registryHost).toString();
		this.registryHostIP = hostStr.split("/")[1];
		this.registryPort = registryPort;
		knownHostsMap = new HashMap<String, HostInfo>();
		receiverThreadList = new ArrayList<TCPReceiver>();
		
		try {
			/* Dynamically Assign a port to this socket */
			server = new ServerSocket(0);
			serverPort = server.getLocalPort();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.severe("ServerSocket creation failed at "+ hostName);
			e.printStackTrace();
		}
		logger.severe("ServerSocket created at "+ hostName+":"+ server.getLocalPort());
		
	}
	
	

	@Override
	public void onEvent(Event e, Socket s) {
		// TODO Auto-generated method stub
		
		//logger.info("Messaging Node received SOMETHING");
		if(RegisterResponse.class.toString().equals(e.getClass().toString())) {
			RegisterResponse regRsp = (RegisterResponse)e;
			logger.info("Message Received from Registry. Status: "+regRsp.getStatus()+", INFO: "+regRsp.getInfoMsg());
			
		} else if(DeregisterRequest.class.toString().equals(e.getClass().toString())) {
			DeregisterRequest d = (DeregisterRequest)e;
			d.setIp(getLocalIP());
			d.setPort(getSocketToRegisterPort());
			byte[] msg = d.getBytes();
			sendDeregisterRequest(msg, socketToRegister);
			
		} else if(DeregisterResponse.class.toString().equals(e.getClass().toString())) {
			DeregisterResponse d = (DeregisterResponse)e;
			if(d.getStatus().contains("SUCCESS")) {
				
				logger.info("REGISTER RETURNED SUCCESS MESSAGE...SHUTTING DOWN THIS NODE");
				
				
				receiverFromServer.setGetOut();
				messageNodeServer.setGetOut();
				logger.info("SHUTTING DOWN CONSOLE READER");
				consoleReader.setGetOut(true);
				try {
					socketToRegister.close();
					logger.info("SOCKET TO REGISTER SHUT DOWN SUCCESSFULLY");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					logger.info("FAILURE TO SHUT DOWN SOCKET TO REGISTER");
					e1.printStackTrace();
				}
				
				
				try {
					server.close();
					logger.info("SERVER SOCKET SHUT DOWN SUCCESSFULLY");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					logger.info("FAILURE TO SHUT DOWN SERVER SOCKET");
					e1.printStackTrace();
				}
				
			} else {
				logger.info("REGISTER DID NOT RETURN SUCCESS MESSAGE");
			}
			
		} else if(MessagingNodeList.class.toString().equals(e.getClass().toString())) {
			
			MessagingNodeList req = (MessagingNodeList)e;
			logger.info("MESSAGING NODE LIST RECEIVED FROM REGISTRY.....");
			handleConnectingTheNeighbors(req);
			logger.info("NEIGHBORING MESSAGING NODES NOTIFIED.....");
			
		} else if(ConnectionRequest.class.toString().equals(e.getClass().toString())) {
			ConnectionRequest req = (ConnectionRequest)e;
			logger.info("MESSAGING NODE RECEIVED MESSAGE: "+req.getMessage());
			
			/* NEIGHBORING INFORMING THIS NODE OF CONNECTIONS */
			handleIncomingConnections(req,s);
		} else if(LinkWeights.class.toString().equals(e.getClass().toString())) {
			LinkWeights lw = (LinkWeights)e;
			logger.info("NUMBER OF LINKS: "+ lw.getLinkNum());
			logger.info(lw.getLinksInfo());
			
			handleDijkstraCalculation(lw);
			
			logger.info("DIJKSTRA CALCULATION FINISHED !!!");
			
		} else if(TaskInitiation.class.toString().equals(e.getClass().toString())) {
			TaskInitiation t = (TaskInitiation)e;
			logger.info("I NEED TO INITIATE "+t.getRounds()+" ROUNDS");
			
			handleNumberSending(t);
			
		} else if(NumberRelay.class.toString().equals(e.getClass().toString())) {
			NumberRelay nr = (NumberRelay)e;
			
			handleNumberRelaying(nr);
			
		} else if(PullTrafficSummaryReq.class.toString().equals(e.getClass().toString())) {
			PullTrafficSummaryReq traffic = (PullTrafficSummaryReq)e;
			logger.info("REQUEST FOR SUMMARY RECEIVED");
			handleSummarySending(traffic);
		}
	}
	
	
	private void handleSummarySending(PullTrafficSummaryReq traffic) {
		
		TrafficSummary ts = new TrafficSummary();
		ts.setIp(localIP);
		ts.setPortNum(socketToRegisterPort);
		ts.setType(Protocol.TRAFFIC_SUMMARY.typeCode());
		ts.setNumMesagesReceived(getNumMessagesReceived());
		ts.setNumMessagesSent(getNumMessagesSent());
		ts.setSumMessagesReceived(getSumMessagesReceived());
		ts.setSumMessagesSent(getSumMessagesSent());
		ts.setNumMessagesRelayed(getNumMessagesRelayed());
		
		
		/*logger.info("SUMMARY I SENT OUT: "+localIP+":"+socketToRegisterPort+"\t"+numMessagesSent+"\t"+numMessagesReceived+"\t"+sumMessagesSent
				+"\t"+sumMessagesReceived+"\t"+numMessagesRelayed);*/
		
		
		TCPSender sender = new TCPSender(socketToRegister,"overlay-messagingNode-"+hostName+".out");
		sender.sendBytes(ts.getBytes());
		logger.info("MESSAGING NODE SENT OUT TRAFFIC SUMMARY");
		
		
	}
	private void handleNumberRelaying(NumberRelay nr) {
		String nxtHop = findNextHop(nr.getPath());
		
		/* Number has arrived at destination*/
		if(nxtHop.equals("FINAL")) {
			String src = nr.getPath().split("//")[0];
			logger.info("I RECEIVED NUMBER "+ nr.getNum()+" FROM "+ src+" TOTAL:"+getNumMessagesReceived()+":"+getNumMessagesSent());
			incrementNumMessagesReceived(1);
			incrementSumMessagesReceived(nr.getNum());
		} else if(nxtHop.equals("ERROR")) {
			logger.severe("NEXT HOP NOT FOUND");
			
		} else {
			/* Message needs to be relayed */
			passOn(nr, nxtHop);
		}
		
	}

	private synchronized HostInfo getKnownHostEntry(String nxtHop) {
		return knownHostsMap.get(nxtHop);
	}

	private void passOn(NumberRelay nr, String nxtHop) {
		HostInfo nextHost = knownHostsMap.get(nxtHop);
		
		//TCPSender sender = new TCPSender(nextHost.getSock(),"overlay-messagingNode-"+hostName+".out");
		senderFinal.sendBytes(nextHost.getSock(),nr.getBytes());
		//logger.info("MESSAGING NODE RELAYED THE NUMBER "+nr.getNum());
		incrementnNumMessagesRelayed(1);
		
	}
	
	
	/* MIGHT HAVE TO SYNCHRONIZE */
	private void handleNumberSending(TaskInitiation task) {
		initializeCounters();
		int rounds = task.getRounds();
		Random r = new Random();
		
		
		for(int i=0; i< rounds; i++) {
			
			
			String node = dijk.getRandNode();
			
			/*Paths contains the order in which nodes are to be traversed*/
			String paths = stitchShortestPath(dijk.getShortestPath(node));
			String nxt = findNextHop(paths);
			//logger.info("THE NEXT HOP ON THE PATH IS: "+nxt);
			//logger.info("KNOWN HOSTS KEYS:");
			//String temp="";
			/*for(String hh: knownHostsMap.keySet()) {
				temp+=hh+"\n";
			}
			logger.info(temp);*/
			
			HostInfo nextHost = knownHostsMap.get(nxt);;
			
			if(nextHost == null) {
				logger.info("NEXT HOST NOT FOUND");
			}
			
			//TCPSender sender = new TCPSender(nextHost.getSock(),"overlay-messagingNode-"+hostName+".out");
			
			
			NumberRelay nr = null;
			/*CHANGE NEEDED*/
			for(int j=0;j<5;j++) {
				/*int num= r.nextInt(Integer.MAX_VALUE);
				int sn = r.nextInt(2);
				if(sn == 0){
					num*=1;
				} else {
					num*=-1;
				}*/
					
				int num = (int)r.nextLong();
				
				nr = new NumberRelay();
				nr.setType(Protocol.NUMBER_RELAY.typeCode());
				nr.setNum(num);
				nr.setPath(paths);
				
				senderFinal.sendBytes(nextHost.getSock(),nr.getBytes());
				logger.info("MESSAGING NODE SENT THE NUMBER "+num);
				logger.info("  ");
				incrementSumMessagesSent(num);
			}
			incrementNumMessagesSent(5);
			/*try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
		
		TaskComplete tc = new TaskComplete();
		tc.setIp(localIP);
		tc.setSockPortNum(socketToRegisterPort);
		tc.setType(Protocol.TASK_COMPLETE.typeCode());
		
		/*//NEW 2
		TCPSender sender = new TCPSender(socketToRegister,"overlay-messagingNode-"+hostName+".out");
		sender.sendBytes(tc.getBytes());*/
		
		senderFinal.sendBytes(socketToRegister,tc.getBytes());
		logger.info("MESSAGING NODE SENT OUT TASK COMPLETE MESSAGE");
	}
	
	private String findNextHop(String paths) {
		String myself = localIP+":"+serverPort;
		//logger.info("I AM "+ myself);
		//logger.info("I HAVE SELECTED PATH: "+paths);
		String[] tokens = paths.split("//");
		int i;
		
		for(i=0; i< tokens.length; i++) {
			if(myself.equals(tokens[i])) {
				break;
			}
		}
		
		if(i==tokens.length-1){
			return "FINAL";
		} else if(i < tokens.length-1){
			return tokens[i+1];
		} else {
			return "ERROR";
		}
		
	}

	

	



	private String stitchShortestPath(List<String> shortestPath) {
		// TODO Auto-generated method stub
		String str=shortestPath.get(0);
		int i=0;
		for(String s: shortestPath) {
			if(i==0){
				i++;
				continue;
			}
			str+="//"+s;
			
		}
		return str;
	}



	private void handleDijkstraCalculation(LinkWeights lw) {
		// TODO Auto-generated method stub
		String myself = localIP+":"+serverPort;
		dijk = new DecodeLinksInfo(lw.getLinksInfo(), myself, lw.getLinkNum());
		
	}



	private void handleIncomingConnections(ConnectionRequest req, Socket s) {
		// TODO Auto-generated method stub
		HostInfo h = new HostInfo();
		
		/* THIS IS THE SOCKET USED TO CONNECT TO THIS NEIGHBOR */
		h.setSock(s);
		/* The LISTENING PORT OF THE NEIGHBOR */
		logger.info("VALUE IN INFO STRING IS: "+req.getInfo());
		h.setServerSocketPortNo(Integer.valueOf(req.getInfo()));
		h.setIp(s.getInetAddress().toString().split("/")[1]);
		h.setSocketPortNo(s.getPort());
		String key = h.getIp()+":"+req.getInfo();
		
		addToHostsMap(h, key);
		printaddrToHostMap();
		
	}



	private void handleConnectingTheNeighbors(MessagingNodeList req) {
		
		int totalNum = req.getTotalNum();
		String infoString = req.getInfos();
		
		String[] lines = infoString.split("\n");
		
		if(lines.length!=totalNum) {
			logger.severe("INCORRECT LINES RECEIVED AS MESSAGING NODES LIST");
		} else {
			logger.info("MESSAGING NODE RECEIVED "+lines.length+" NEIGHBOR INFO");
		}
		
		for(String line: lines) {
			String tokens[] = line.split(":");
			String ip = tokens[0];
			
			/* THIS IS THE LISTENING PORT OF THE NEIGHBOR */
			String serverSocketToNode = tokens[2];
			
			sendConnectionToNeighbor(ip,serverSocketToNode);
		}
		
		
	}
	
	private void sendConnectionToNeighbor(String ip, String serverSocketToNode){
		try {
			
			/* CONNECTING TO SERVER SOCKET TO THE NEIGHBOR */
			
			Socket sock = new Socket(ip,Integer.valueOf(serverSocketToNode));
			String key = ip+":"+serverSocketToNode;
			HostInfo h = new HostInfo();
			h.setIp(ip);
			h.setServerSocketPortNo(Integer.valueOf(serverSocketToNode));
			h.setSock(sock);
			h.setSocketPortNo(sock.getPort());
			
			/*===========KEEP A LIST OF NEIGHBORS AND THEIR IP:LISTENING_PORT=============*/
			/*===========ADDING TO KNOWNHOSTS MAP===================*/
			
			addToHostsMap(h, key);
			
			ConnectionRequest req = new ConnectionRequest();
			req.setType(Protocol.CONNECTION_EST_REQ.typeCode());
			req.setMessage("HELLO I AM "+hostName);
			req.setInfo(String.valueOf(serverPort));
			
			/* CREATE A RECEIVER THERAD ON sock */
			TCPReceiver receiver = new TCPReceiver(sock, this);
			Thread rcvThread = new Thread(receiver);
			
			/* CREATE A MAP OF RECEIVER THREADS */
			addToReceiverThreadList(receiver);
			rcvThread.start();
			
			logger.info("CONNECTION REQUEST SENT TO NEIGHBOR: "+ip+":"+serverSocketToNode);
			/*=====================TCPSENDER TO SEND OUT CONNECTION REQUEST TO NEIGHBOR=============*/
			
			TCPSender sendr = new TCPSender(sock, "overlay-messagingNode-"+hostName+".out");
			sendr.sendBytes(req.getBytes());
			
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			logger.severe("ERROR CONNECTION TO NEIGHBOR BEFORE SENDING CONNECTION");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void main(String arg[]) {
		if(arg.length != 2) {
			System.out.println("Invalid number of Arguments Entered. Program Exiting.");
			return;
		}
		MessagingNode m = null;
		try {
			m = new MessagingNode(arg[0], arg[1]);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Messaging Node Initialized");
		m.startup();
		
	}
	
	
	public void startup() {
		
		/*
		 *  This is the Server Thread that is in charge of listening to incoming requests on this
		 * messaging node */
		
		messageNodeServer = new TCPServer(server, null, this);
		
		/* This is the Message Node Server Thread that listens to incoming connections */
		Thread msgNodeServerThread = new Thread(messageNodeServer);

		/*=====================SERVER THREAD STARTED==================================*/
		
		msgNodeServerThread.start();
		logger.info("Messaging Node Server Thread started");
		
		/* Starting Up the Thread In charge of reading from Console */
		
		consoleReader = new ConsoleReader(this);
		Thread consoleReaderThread = new Thread(consoleReader);
		
		/*=====================CONSOLE READER THREAD STARTED==================================*/
		
		consoleReaderThread.start();
		logger.info("Messaging Node Console Reader started");
		logger.info("REGISTRY IP IS========================:"+getRegistryHostIP());
		
		/*==================STARTING A RECEIVER THREAD=========================================*/
		
		socketToRegister = startupMessagingNodeLinkToRegistry();
		receiverFromServer = new TCPReceiver(socketToRegister, this);
		Thread receiverFromServerThread = new Thread(receiverFromServer);
		receiverFromServerThread.start();
		
		
		
		/*==================SENDING OUT REGISTRATION REQUEST==================================*/
		
		socketToRegisterPort = socketToRegister.getLocalPort();
		
		sendRegisterRequest(socketToRegister);
		
		
		
		try {
			
			msgNodeServerThread.join();
			consoleReaderThread.join();
			receiverFromServerThread.join();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/* ALLOWS MESSAGING NODE TO SEND REQUEST TO REGISTRY */
	public Socket startupMessagingNodeLinkToRegistry() {
		Socket s = null;
		try {
			System.out.println("REGISTRY IP IS========================:"+getRegistryHostIP());
			logger.info("REGISTRY IP IS========================:"+getRegistryHostIP());
			logger.info("REGISTRY_IP: "+getRegistryHostIP()+":::REGISTRY_PORT:"+getRegistryPort());
			s = new Socket(getRegistryHostIP(), Integer.valueOf(getRegistryPort()));
		} catch (UnknownHostException e) {
			logger.severe("Unable to connect to RegistryNode from "+ hostName+"(UnknownHost):"+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.severe("Unable to connect to RegistryNode from "+ hostName+"(IOException):"+e.getMessage());
			e.printStackTrace();
		}
		return s;
	}
	
	public void sendRegisterRequest(Socket s) {
		Register req = constructRegisterMessage(s);
		byte[] msg = req.getBytes();
		
		TCPSender sender = new TCPSender(s,"overlay-messagingNode-"+hostName+".out");
		
		sender.sendBytes(msg);
		logger.info("MESSAGING NODE HAS DISPATCHED REGISTRATION REQUEST....");
		//req.
	}
	
	public void sendDeregisterRequest(byte[] msg, Socket s) {
		
		TCPSender sender = new TCPSender(s,"overlay-messagingNode-"+hostName+".out");
		
		sender.sendBytes(msg);
		logger.info("MESSAGING NODE HAS DISPATCHED DEREGISTRATION REQUEST....");
	}
	
	public Register constructRegisterMessage(Socket s) {
		Register req = new Register();
		req.setMsgType(Protocol.REGISTRATION_REQUEST.typeCode());
		req.setIpAddress(localIP);
		req.setServerPortNum(serverPort);
		req.setSocketPortNum(socketToRegisterPort);
		
		return req;
	}


	public String getRegistryHostIP() {
		return registryHostIP;
	}



	public void setRegistryHostIP(String registryHostIP) {
		this.registryHostIP = registryHostIP;
	}



	public String getRegistryPort() {
		return registryPort;
	}



	public void setRegistryPort(String registryPort) {
		this.registryPort = registryPort;
	}



	public ServerSocket getServer() {
		return server;
	}



	public void setServer(ServerSocket server) {
		this.server = server;
	}



	public int getServerPort() {
		return serverPort;
	}



	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}



	public EventFactory getEventFactory() {
		return eventFactory;
	}



	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}



	public static Logger getLogger() {
		return logger;
	}



	public static void setLogger(Logger logger) {
		MessagingNode.logger = logger;
	}



	public Socket getSocketForRegister() {
		return socketToRegister;
	}



	public void setSocketForRegister(Socket socketForRegister) {
		this.socketToRegister = socketForRegister;
	}



	public Socket getSocketToRegister() {
		return socketToRegister;
	}



	public void setSocketToRegister(Socket socketToRegister) {
		this.socketToRegister = socketToRegister;
	}



	public int getSocketToRegisterPort() {
		return socketToRegisterPort;
	}



	public void setSocketToRegisterPort(int socketToRegisterPort) {
		this.socketToRegisterPort = socketToRegisterPort;
	}



	public String getHostName() {
		return hostName;
	}



	public void setHostName(String hostName) {
		this.hostName = hostName;
	}



	public String getLocalIP() {
		return localIP;
	}



	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}



	@Override
	public void onCommand(String s) {
		// TODO Auto-generated method stub
		
		if(s.equals("print-shortest-path")) {
			dijk.printAllShortestPaths();
		}
		
	}



	public synchronized Map<String, HostInfo> getKnownHostsMap() {
		return knownHostsMap;
	}

	public synchronized void addToHostsMap(HostInfo h, String key) {
		knownHostsMap.put(key,h);
	}


	public synchronized void setKnownHostsMap(Map<String, HostInfo> knownHostsMap) {
		this.knownHostsMap = knownHostsMap;
	}

	private synchronized void printaddrToHostMap() {
		
		System.out.println("CURRENT NEIGHBORS......");
		System.out.println("MYSELF: "+localIP+":"+serverPort);
		for(HostInfo h: knownHostsMap.values()) {
			System.out.println(h.getIp()+":"+h.getServerSocketPortNo());
		}
		
	}


	public synchronized List<TCPReceiver> getReceiverThreadList() {
		return receiverThreadList;
	}

	
	public synchronized void addToReceiverThreadList(TCPReceiver h) {
		this.receiverThreadList.add(h);
	}


	public synchronized void setReceiverThreadList(List<TCPReceiver> receiverThreadMap) {
		this.receiverThreadList = receiverThreadMap;
	}



	public synchronized DecodeLinksInfo getDijk() {
		return dijk;
	}



	public synchronized void setDijk(DecodeLinksInfo dijk) {
		this.dijk = dijk;
	}



	public synchronized TCPReceiver getReceiverFromServer() {
		return receiverFromServer;
	}



	public synchronized void setReceiverFromServer(TCPReceiver receiverFromServer) {
		this.receiverFromServer = receiverFromServer;
	}



	public synchronized long getNumMessagesSent() {
		return numMessagesSent;
	}



	public synchronized void setNumMessagesSent(long numMessagesSent) {
		this.numMessagesSent = numMessagesSent;
	}



	public synchronized long getNumMessagesReceived() {
		return numMessagesReceived;
	}



	public synchronized void setNumMessagesReceived(long numMessagesReceived) {
		this.numMessagesReceived = numMessagesReceived;
	}



	public synchronized long getSumMessagesSent() {
		return sumMessagesSent;
	}



	public synchronized void setSumMessagesSent(long sumMessagesSent) {
		this.sumMessagesSent = sumMessagesSent;
	}



	public synchronized long getSumMessagesReceived() {
		return sumMessagesReceived;
	}



	public synchronized void setSumMessagesReceived(long sumMessagesReceived) {
		this.sumMessagesReceived = sumMessagesReceived;
	}



	public synchronized long getNumMessagesRelayed() {
		return numMessagesRelayed;
	}



	public synchronized void setNumMessagesRelayed(long numMessagesRelayed) {
		this.numMessagesRelayed = numMessagesRelayed;
	}
	
}
