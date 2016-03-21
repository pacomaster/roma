package com.iteso.roma.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class StageAgent extends Agent{
	
	private final int DEFAULT_CYCLE_SECONDS = 16;
	private final int MIN_CYCLE_SECONDS = 10;
	private final int MAX_CYCLE_SECONDS = 20;
	private final int INFINITY = Integer.MAX_VALUE;
	
	private int stageId;
	private int intersectionId;
	private int cycleSeconds;
	private int priority;
	private Map<Integer, Integer> priorities;

	// ARGS
	// (stageId, intersectionId, priority)
	protected void setup(){
		Object[] args =  getArguments();
		 priorities = new HashMap<Integer, Integer>();
		if(args.length > 0){
			this.stageId = Integer.parseInt((String)args[0]);
			this.intersectionId = Integer.parseInt((String)args[1]);
			this.priority = Integer.parseInt((String)args[2]);
		}else{
			this.stageId = 1;
			this.intersectionId = 1;			
			this.priority = 1;
		}
		this.cycleSeconds = DEFAULT_CYCLE_SECONDS;
		
		// System.out.println("StgAge" + this.stageId + " created");		
		
		// Register stage service
		// !!! IMPORTANT: MAYBE NOT NEEDED IF I CAN USE NAME !!!
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("stage-" + this.stageId);
		sd.setName("stage-" + this.stageId);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new RequestStage());
		addBehaviour(new CoordinationCFP());
		
	}
	
	private class RequestStage extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);			
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// REQUEST Message received. Process it
				String conversationId = msg.getConversationId();
				// Validate change request
				if(conversationId.equals("stage-time")){
					ACLMessage reply = msg.createReply();
					// Send priority
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(Integer.toString(cycleSeconds));
					
					// Print stage in progress DEBUG
					System.out.println("stgAge" + stageId + "(" + priority + ") - " + cycleSeconds + " sec. P=" + priority);
					
					// cycleSeconds = DEFAULT_CYCLE_SECONDS;
					myAgent.send(reply);
				}
				
				// Request to change the priority from intersectationAgent
				
				if(conversationId.equals("change-priority")){
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					reply.setContent(Integer.toString(stageId));
					myAgent.send(reply);
					
					myAgent.addBehaviour(new Coordination(msg.getContent())); // Start coordination with change priority
				}
			}			
		}
	}
	
	private class CoordinationCFP extends CyclicBehaviour {
		
		/**
		 * Function to calculate the min deal to accept
		 * @return number of min seconds to accept
		 */
		private int getDeal(){
			switch (priority){
				case 1:
					if(cycleSeconds >= 18) return INFINITY;
					return 0;
				case 2:
					if(cycleSeconds >= 20) return INFINITY;
					return 0;
				case 3:
					if(cycleSeconds >= 10 && cycleSeconds <= 12) return 1;
					else if(cycleSeconds >= 13 && cycleSeconds <= 15)return 2;
					else if(cycleSeconds == 16) return 4;
					else if(cycleSeconds >= 17 && cycleSeconds <= 18)return 2;
					if(cycleSeconds >= 19) return INFINITY;
					return 0;
				case 4:
					if(cycleSeconds == 10) return 4;
					else if(cycleSeconds >= 11 && cycleSeconds <= 13)return 3;
					if(cycleSeconds >= 14) return INFINITY;
					return 0;
				case 5:
					if(cycleSeconds == 10) return 6;
					if(cycleSeconds == 11) return 4;
					if(cycleSeconds == 12) return 3;
					if(cycleSeconds >= 13) return INFINITY;
					return 0;
			}
			return 0;
		}
		
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);			
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String conversationId = msg.getConversationId();
				if(conversationId.equals("stage-coordination")){
					ACLMessage reply = msg.createReply();
					int timeProposal = getDeal();
					
					// System.out.println("stgAge" + stageId + " propose deal: " + timeProposal); // DEBUG
					
					if(timeProposal == INFINITY){
						reply.setPerformative(ACLMessage.REFUSE);
						cycleSeconds += Integer.parseInt(msg.getContent());
						reply.setContent(Integer.toString(stageId));
						myAgent.send(reply);
						// System.out.println("stgAge" + stageId + " Refuses"); // DEBUG
					}else{
						int timeCFP = Integer.parseInt(msg.getContent());
						if(timeCFP > timeProposal)timeProposal = timeCFP;
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(Integer.toString(timeProposal));
						myAgent.send(reply);
						// System.out.println("stgAge" + stageId + " Proposes"); // DEBUG
					}
				}
			}
			
			MessageTemplate mta = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			msg = myAgent.receive(mta);
			if (msg != null) {
				// System.out.println("stgAge" + stageId + " receives accepts"); // DEBUG
				String conversationId = msg.getConversationId();
				if(conversationId.equals("stage-accept")){
					ACLMessage reply = msg.createReply();
					int timeAccepted = Integer.parseInt(msg.getContent());
					cycleSeconds += timeAccepted;
					if(cycleSeconds > MAX_CYCLE_SECONDS) cycleSeconds = MAX_CYCLE_SECONDS;
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(Integer.toString(timeAccepted));
					myAgent.send(reply);
					
					// System.out.println("stgAge" + stageId + " inform accepts"); // DEBUG
				}
			}
			
			MessageTemplate mtr = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
			msg = myAgent.receive(mtr);
			if (msg != null) {
				// System.out.println("stgAge" + stageId + " receives reject"); // DEBUG
				String conversationId = msg.getConversationId();
				if(conversationId.equals("stage-reject")){
					// Do nothing
					// System.out.println("stgAge" + stageId + " inform reject"); // DEBUG
				}
			}			
		}
	}
	
	private class Coordination extends Behaviour {
		
		private int step = 0;
		private String agentsAbove;
		private int currentAgent = 0;
		private String[] agents;
		private boolean[] agentsAccepted;
		private int offerTime = -1;
		private final int END_STEP = 5;
		
		public Coordination(String msg){
			super();
			priority = Integer.parseInt(msg.split("-")[0]);
			this.agentsAbove = msg.split("-")[1];
			this.agents = this.agentsAbove.split(",");
			this.agentsAccepted = new boolean[agents.length];
		}
		
		/**
		 * Method to calculate the max offer to present to other agents
		 * @return the max number of seconds to offer
		 */
		private int getOffer(){
			switch (priority){
				case 1:
					return 0;
				case 2:
					return 0;
				case 3:
					if(cycleSeconds == 15) return 1;
					else if(cycleSeconds >= 16 && cycleSeconds <= 18)return 2;
					else if(cycleSeconds >= 19) return 4;
					return 0;
				case 4:
					if(cycleSeconds == 13) return 1;
					else if(cycleSeconds >= 14 && cycleSeconds <= 17)return 2;
					else if(cycleSeconds >= 18) return 4;
					return 0;
				case 5:
					if(cycleSeconds == 11) return 1;
					else if(cycleSeconds >= 12 && cycleSeconds <= 13)return 2;
					else if(cycleSeconds >= 14 && cycleSeconds <= 18) return 4;
					else if(cycleSeconds >= 19) return 6;
					return 0;					
			}
			return 0;
		}
		
		public void action() {
			switch(step){
				case 0:
					// STEP 0: Calculate offer and send that to next stage in the top
					
					if(offerTime == -1) offerTime = getOffer(); // Get offer time value only first time
					if(offerTime > 0){
						
						// Send CFP to next stageId in the top of the list
						
						ACLMessage request = ACLMessageFactory.createCFPMsg(AIDManager.getStageAID(Integer.parseInt(agents[currentAgent]), myAgent), "1", "stage-coordination");
						myAgent.send(request);
						step++;
						
						// System.out.println("stgAge" + stageId + " offerTime: " + offerTime);
						// System.out.println("stgAge" + stageId + " offers 1s to  " + agents[currentAgent]);
						
					}else{
						
						// If the stage cannot offer any time the coordination is cancelled.
						
						step = END_STEP;
					}
					break;
				case 1:
					// STEP 1: Get refuse or propose from next agent in the top
					
					MessageTemplate mtr = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
					ACLMessage msg = myAgent.receive(mtr);
					if (msg != null) {
						
						
						// System.out.println("stgAge" + stageId + " receives refuse"); // DEBUG
						
						// If offer was refuse then coordination is over.
						
						step = END_STEP;
					}else{
						MessageTemplate mtp = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
						msg = myAgent.receive(mtp);
						if (msg != null) {
							
							// If stage response with a propose
							
							String conversationId = msg.getConversationId();
							
							if(conversationId.equals("stage-coordination")){
								int timeProposal = Integer.parseInt(msg.getContent());
								int timeToSend = 1;
								
								// System.out.println("stgAge" + stageId + " receives propose " + timeProposal + "s"); // DEBUG
								
								ACLMessage message = null;
								
								// Check if with offerTime left stageAgent can accept the proposal
								
								if(timeProposal >= timeToSend){
									if(offerTime - timeProposal >= 0){
										timeToSend = timeProposal;
										offerTime -= timeProposal;
										message = ACLMessageFactory.createAcceptProposalMsg(AIDManager.getStageAID(Integer.parseInt(agents[currentAgent]), myAgent), Integer.toString(timeToSend), "stage-accept");
										step++;
										myAgent.send(message);
										// System.out.println("stgAge" + stageId + " accepts offers " + timeToSend + "s to stage"); // DEBUG
									}else{
										message = ACLMessageFactory.createRejectProposalMsg(AIDManager.getStageAID(Integer.parseInt(agents[currentAgent]), myAgent), "", "stage-reject");
										step = END_STEP;
										myAgent.send(message);
										
										// System.out.println("stgAge" + stageId + " rejects offer"); // DEBUG
									}
								}
								
							}
						}
					}
					break;
				case 2:
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					msg = myAgent.receive(mt);
					if (msg != null) {
						
						// If stage response with inform accept
						
						String conversationId = msg.getConversationId();
						
						if(conversationId.equals("stage-accept")){
							int timeAccepted = Integer.parseInt(msg.getContent());
							
							offerTime -= timeAccepted;
							cycleSeconds -= timeAccepted;
							
							ACLMessage message = ACLMessageFactory.createRequestMsg(AIDManager.getIntersectionAID(intersectionId, myAgent), Integer.toString(stageId), "stage-up");
							myAgent.send(message);
							step++;
							
							// System.out.println("stgAge" + stageId + " request up to intAge" + intersectionId); // DEBUG
						}
					}
					break;
				case 3:
					MessageTemplate mta = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
					msg = myAgent.receive(mta);
					if (msg != null) {
						
						// If intersection response with agree
						
						String conversationId = msg.getConversationId();
						
						if(conversationId.equals("stage-up")){
							step++;
							// System.out.println("stgAge" + stageId + " received agree from intAge" + intersectionId); // DEBUG
						}
					}
					
					MessageTemplate mtre = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
					msg = myAgent.receive(mtre);
					if (msg != null) {
						
						// If intersection response with refuse
						
						String conversationId = msg.getConversationId();
						
						if(conversationId.equals("stage-up")){
							// System.out.println("stgAge" + stageId + " received refuse from intAge" + intersectionId); // DEBUG
							step = END_STEP;
						}
					}
					break;
				case 4:
					MessageTemplate mti = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					msg = myAgent.receive(mti);
					if (msg != null) {
						
						// If intersection response inform up position
						
						String conversationId = msg.getConversationId();
						
						if(conversationId.equals("stage-up")){
							
							// System.out.println("stgAge" + stageId + " received inform from intAge" + intersectionId); // DEBUG
							
							if(currentAgent < agents.length){
								currentAgent++;
								step = 0;
							}else{
								step = END_STEP;
							}
						}
					}
					
					MessageTemplate mtf = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
					msg = myAgent.receive(mtf);
					if (msg != null) {
						
						// If intersection response failure to up position
						
						String conversationId = msg.getConversationId();
						
						if(conversationId.equals("stage-up")){
							// System.out.println("stgAge" + stageId + " received failure from intAge" + intersectionId); // DEBUG
							step = END_STEP;
						}
					}
					break;
			}
		}

		@Override
		public boolean done() {
			// System.out.println("stgAge" + stageId + " end coordination"); // DEBUG
			return step == END_STEP;
		}
	}	
}
