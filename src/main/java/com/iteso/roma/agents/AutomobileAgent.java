package com.iteso.roma.agents;

import java.util.ArrayList;

import com.iteso.roma.enums.AutomobileStateEnum;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;
import com.iteso.roma.utils.TimeManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class AutomobileAgent extends Agent{
	
	public final int CAR_LENGTH = 500;
	public final int INF = Integer.MAX_VALUE;

	private int automobileId;
	private int currentStreetId;
	private int nextStreetId;
	private int lane;
	
	private int distanceToNextStreet;
	
	private int startStreetId;
	private int endStreetId;
	
	private int secondsToAppear;
	private int secondsToCross;
	
	private int mps;
	
	private AutomobileStateEnum state = AutomobileStateEnum.STOP;	
	
	// ARGS
	// (automobileId, startStreetId, endStreetId)
	protected void setup(){
		Object[] args =  getArguments();
		if(args.length > 0){
			this.automobileId = (Integer)args[0];
			this.startStreetId = (Integer)args[1];
			this.endStreetId = (Integer)args[2];
			
			// Just for 1 intersection
			this.currentStreetId = this.startStreetId;
			this.nextStreetId = this.endStreetId;
			
			this.lane = 0;
			this.secondsToCross = 0;
			this.distanceToNextStreet = INF;
		}
		
		// System.out.println("AutAge" + this.automobileId + " created");
		
		// Register the automobile service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("automobile-" + this.automobileId);
		sd.setName("automobile-" + this.automobileId);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new RequestLane());
		
		addBehaviour(new InformCross());		
		
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {				
				if(state == AutomobileStateEnum.MOVING){
					distanceToNextStreet -= mps;
					if(distanceToNextStreet <= 0){
						currentStreetId = nextStreetId;
						// TODO: check for next street id
						myAgent.addBehaviour(new RequestLane());
					}
				}
				
			}
		});	
	}
	
	private class InformCross extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// System.out.println("AutAge" + automobileId + " received-inform");
				// REQUEST Message received. Process it
				String conversationId = msg.getConversationId();
				// Validate change request
				if(conversationId.equals("inform-cross")){
					secondsToCross = secondsToAppear * (-1);
					currentStreetId = endStreetId;
					System.out.println(automobileId + "\t" +secondsToCross); // DEBUG TEST
				}
			}
			block();
		}
	}
	
	private class RequestLane extends Behaviour{
		private int step = 0;
		
		public void action() {
			switch(step){
				case 0:
					AID receiver = AIDManager.getStreetAID(currentStreetId , myAgent);					
					if(receiver != null){
						// Send the request to change priority
						ACLMessage request = ACLMessageFactory.createRequestMsg(receiver, Integer.toString(automobileId) + "-" + Integer.toString(endStreetId), "request-lane");
						myAgent.send(request);
						step++;
					}					
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					ACLMessage msg = myAgent.receive(mt);
					if (msg != null) {
						// REQUEST Message received. Process it
						String conversationId = msg.getConversationId();
						// Validate change request
						if(conversationId.equals("request-lane")){
							String[] message = msg.getContent().split("-");
							lane = Integer.parseInt(message[0]);
							int distance = Integer.parseInt(message[1]);
							int mps = Integer.parseInt(message[2]);
							// secondsToStop = distance / mps;
							step++;
						}
					}
					break;					
			}
		}
		
		public boolean done(){
			return step == 2;
		}
	}
}
