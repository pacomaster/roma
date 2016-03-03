package com.iteso.roma.agents;

import java.util.ArrayList;

import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

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

	private int automobileId;
	private int streetId;
	private int lane;
	
	private int startStreetId;
	private int endStreetId;
	
	private int secondsToAppear;
	private int secondsToCross;
	
	public float CAR_LENGTH = 5.0f;
	
	// ARGS
	// (automobileId, streetId, startStreetId, endStreetId, secondsToAppear)
	protected void setup(){
		Object[] args =  getArguments();
		if(args.length > 0){
			this.automobileId = Integer.parseInt((String)args[0]);
			this.streetId = Integer.parseInt((String)args[1]);
			this.startStreetId = Integer.parseInt((String)args[2]);
			this.endStreetId = Integer.parseInt((String)args[3]);
			this.secondsToAppear = Integer.parseInt((String)args[4]);
			this.lane = 0;
			this.secondsToCross = 0; 
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
		
		addBehaviour(new InformCross());
		
		addBehaviour(new TickerBehaviour(this, 100) {
			protected void onTick() {
				secondsToAppear--;
				if(secondsToAppear == 0){
					//// System.out.println("AutAge" + automobileId + " enters the system");
					myAgent.addBehaviour(new RequestLane());
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
					streetId = endStreetId;
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
					AID receiver = AIDManager.getStreetAID(streetId , myAgent);					
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
							lane = Integer.parseInt(msg.getContent());
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
