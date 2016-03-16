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
	private int streetId;
	private int lane;
	
	private int startStreetId;
	private int endStreetId;
	
	private int secondsToAppear;
	private int secondsToCross;
	private int secondsToStop;
	
	private int mps;
	
	private AutomobileStateEnum state = AutomobileStateEnum.STOP;	
	
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
			this.secondsToStop = INF;
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
		
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				secondsToAppear--;
				secondsToStop--;
				if(secondsToAppear == 0){
					//// System.out.println("AutAge" + automobileId + " enters the system");
					myAgent.addBehaviour(new RequestLane());
					state = AutomobileStateEnum.MOVING;
				}
				if(secondsToStop == 0){
					state = AutomobileStateEnum.WAITING;
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
							String[] message = msg.getContent().split("-");
							lane = Integer.parseInt(message[0]);
							int distance = Integer.parseInt(message[1]);
							int mps = Integer.parseInt(message[2]);
							secondsToStop = distance / mps;
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
