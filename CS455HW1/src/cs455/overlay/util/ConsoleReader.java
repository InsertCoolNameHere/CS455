package cs455.overlay.util;

import java.util.Scanner;
import java.util.logging.Logger;

import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;
import cs455.overlay.wireformats.EventFactory;

public class ConsoleReader implements Runnable{
	
	private Scanner sc;
	private volatile boolean getOut = false;
	private EventFactory ev;
	private Node callingNode;
	private static Logger logger;
	
	public ConsoleReader() {
		logger = LogFactory.getLogger(ConsoleReader.class.getName(), "console-reader.out");
		ev = EventFactory.getInstance();
		sc = new Scanner(System.in);
	}
	
	public ConsoleReader(Node callingNode) {
		ev = EventFactory.getInstance();
		sc = new Scanner(System.in);
		this.callingNode = callingNode;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!getOut) {
			String cmd = sc.nextLine();
			if(cmd.equals("exit-overlay")) {
				
				ev.reactInternal(callingNode,cmd);
				sc.close();
				setGetOut(true);
			} else {
				ev.reactInternal(callingNode,cmd);
			}
		}
		logger.info("CONSOLE READER EXITED");
		
	}

	public Scanner getSc() {
		return sc;
	}

	public void setSc(Scanner sc) {
		this.sc = sc;
	}

	public boolean isGetOut() {
		return getOut;
	}

	public void setGetOut(boolean getOut) {
		this.getOut = getOut;
	}

	public EventFactory getEv() {
		return ev;
	}

	public void setEv(EventFactory ev) {
		this.ev = ev;
	}

	public Node getCallingNode() {
		return callingNode;
	}

	public void setCallingNode(Node callingNode) {
		this.callingNode = callingNode;
	}

}
