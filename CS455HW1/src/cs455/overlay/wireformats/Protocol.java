package cs455.overlay.wireformats;

public enum Protocol {
	REGISTRATION_REQUEST(1),
	REGISTRATION_RESPONSE(2),
	DEREGISTER_REQUEST(3),
	DEREGISTER_RESPONSE(4),
	/* Registry informing each node who its peer nodes are i.e. who it needs to connect to */
	MESSAGING_NODES_LIST(5),
	/* Informing all nodes of each link and their weights */
	Link_Weights(6),
	/* Telling nodes that overlay has been created and message passing can start */
	TASK_INITIATE(7),
	/* Each node telling the registry that it is done passing its message */
	TASK_COMPLETE(8),
	/* On receiving TASK_COMPLETE, registry sends this to all registered messaging nodes */
	PULL_TRAFFIC_SUMMARY(9),
	/* Each node to the registry */
	TRAFFIC_SUMMARY(10),
	CONNECTION_EST_REQ(11),
	NUMBER_RELAY(12)
	;
	private int type;
	
	Protocol(int type) {
		this.type = type;
	}
	
	public int typeCode() {
		return type;
	}
	
	public static void main(String arg[]) {
		System.out.println(Protocol.DEREGISTER_REQUEST.type);
	}

}
