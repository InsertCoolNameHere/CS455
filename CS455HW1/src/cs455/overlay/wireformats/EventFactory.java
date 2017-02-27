package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import cs455.overlay.node.Node;

public class EventFactory {
	
	private static EventFactory eventFactory;
	
	public static synchronized EventFactory getInstance() {
		if(eventFactory == null) {
			eventFactory = new EventFactory();
		}
		return eventFactory;
	}
	
	/* react to incoming message */
	
	public void react(byte[] msg, Node callingNode, Socket sock) {
		try {
			
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(msg);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			
			/* Getting the message type */
			int msgTypeInt = din.readInt();
			
			baInputStream.close();
			din.close();
			
			//System.out.println("======================MESSAGE TYPE IS:" +msgTypeInt+"===========================");
			/*============CASE OF REGISTRY RECEIVING A REGISTER REQUEST==================*/
			if(msgTypeInt == Protocol.REGISTRATION_REQUEST.typeCode()) {
				Register resgisterRequest = new Register(msg);
				callingNode.onEvent(resgisterRequest,sock);
			} else if(msgTypeInt == Protocol.REGISTRATION_RESPONSE.typeCode()) {
				RegisterResponse rsp = new RegisterResponse(msg);
				callingNode.onEvent(rsp, sock);
			} else if(msgTypeInt == Protocol.DEREGISTER_REQUEST.typeCode()) {
				DeregisterRequest req = new DeregisterRequest(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == Protocol.DEREGISTER_RESPONSE.typeCode()) {
				DeregisterResponse rsp = new DeregisterResponse(msg);
				callingNode.onEvent(rsp, sock);
			} else if(msgTypeInt == Protocol.MESSAGING_NODES_LIST.typeCode()) {
				MessagingNodeList req = new MessagingNodeList(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == Protocol.CONNECTION_EST_REQ.typeCode()) {
				ConnectionRequest req = new ConnectionRequest(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == Protocol.Link_Weights.typeCode()) {
				LinkWeights req = new LinkWeights(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == Protocol.TASK_INITIATE.typeCode()) {
				TaskInitiation req = new TaskInitiation(msg);
				callingNode.onEvent(req, sock);
			} else if(msgTypeInt == Protocol.NUMBER_RELAY.typeCode()) {
				NumberRelay nr = new NumberRelay(msg);
				callingNode.onEvent(nr, sock);
			} else if(msgTypeInt == Protocol.TASK_COMPLETE.typeCode()) {
				TaskComplete nr = new TaskComplete(msg);
				callingNode.onEvent(nr, sock);
			}  else if(msgTypeInt == Protocol.PULL_TRAFFIC_SUMMARY.typeCode()) {
				PullTrafficSummaryReq nr = new PullTrafficSummaryReq(msg);
				callingNode.onEvent(nr, sock);
			} else if(msgTypeInt == Protocol.TRAFFIC_SUMMARY.typeCode()) {
				TrafficSummary nr = new TrafficSummary(msg);
				callingNode.onEvent(nr, sock);
			} 
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void reactInternal(Node callingNode, String cmd) {
		if("exit-overlay".equals(cmd)) {
			DeregisterRequest d = new DeregisterRequest();
			callingNode.onEvent(d, null);
		} else {
			callingNode.onCommand(cmd);
		}
	}
	
	public void handleRegisterRequest(DataInputStream in) {
		
	}
	
	
	public static void main(String arg[]) {
		
	}

}
