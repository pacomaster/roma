package com.iteso.roma.agents;

import org.apache.commons.lang3.StringUtils;

import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PhaseAgent extends Agent{
	
	private final int MAX_CYCLE_SECONDS = 20;
	private final int INF = Integer.MAX_VALUE;
	
	
	private final int MIN;
	private final int MIDDLE;
	private final int MAX;
	private final int UNIT;
	
	private String phaseId;
	private String junctionId;
	
	private String[] phaseValues;
	private int[] phaseTimes;
	
	private int phasePriority = 1;
	private int lanesGreen;
	private int[] lanesPriorities;
	
	private boolean isNegotiating = false;
	
	private int[][] offerTable = {
			{0,0,0,0,0},
			{0,0,0,0,1},
			{0,0,0,1,2},
			{0,0,1,2,3},
			{0,1,2,3,4}};
	
	private int[][] dealTable = {
			{INF,INF,INF,INF,INF},
			{4  ,INF,INF,INF,INF},
			{3  ,4  ,INF,INF,INF},
			{2  ,3  ,4  ,INF,INF},
			{1  ,2  ,3  ,4  ,INF}};
	
	public PhaseAgent(String phaseId, String junctionId, int[] phaseTimes, String[] phaseValues) {
		this.phaseId = phaseId;
		this.junctionId = junctionId;
		this.phaseTimes = phaseTimes;
		this.phaseValues = phaseValues;
		
		MIDDLE = phaseTimes[0];
		UNIT = MIDDLE / 5;
		MIN = MIDDLE - UNIT - UNIT;
		MAX = MIDDLE + UNIT + UNIT;
		
		lanesPriorities = new int[phaseValues[0].length()];
		lanesGreen = 0;
		
		for(String value : phaseValues){
			int counter = 0;
			for(int i = 0; i < value.length(); i++) {
			    if(value.charAt(i) == 'G') {
			        counter++;
			        lanesPriorities[i] = 0;
			    }else{
			    	lanesPriorities[i] = 1;
			    }
			lanesGreen += counter;
			}
		}		
		phasePriority = calculatePhasePriority();
		
	}
	
	private int calculatePhasePriority(){
		int sum = 0;
		for(int p: lanesPriorities){
			sum += p;
		}
		return sum / lanesGreen;
	}
	
	private int calculateColumnPriority(){
		int ret = 0;
		int currentTime = MIN + UNIT;
		while(phaseTimes[0] >= currentTime){
			ret++;
			currentTime += UNIT;
		}
		return ret;
	}
	
	protected void setup(){
		// Register the phase service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(phaseId);
		sd.setName(phaseId);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new RequestPhase());
		addBehaviour(new CoordinationCFP());
		
	}
	
	private class RequestPhase extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);			
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// REQUEST Message received. Process it
				String conversationId = msg.getConversationId();
				// Validate change request
				if(conversationId.equals("phase-values-times")){
					ACLMessage reply = msg.createReply();
					// Send values and times
					reply.setPerformative(ACLMessage.INFORM);
					String msgValues = StringUtils.join(phaseValues,",");
					String msgTimes = String.valueOf(phaseTimes[0]);
					for(int i = 1; i < phaseTimes.length; i++){
						msgTimes += "," + phaseTimes[i];
					}									
					reply.setContent(msgValues + "#" + msgTimes);					
					myAgent.send(reply);
					phaseTimes[0] =MIDDLE;
				}
				
				// Request to change the priority from JunctionAgent				
				if(conversationId.equals("lane-change-priority")){
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					reply.setContent(phaseId);
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
			int col = calculateColumnPriority();
			int row = phasePriority - 1;
			
			return dealTable[row][col];
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
					
					if(timeProposal == INF){
						reply.setPerformative(ACLMessage.REFUSE);
						phaseTimes[0] += Integer.parseInt(msg.getContent()) * UNIT;
						reply.setContent(phaseId);
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
					phaseTimes[0] += timeAccepted * UNIT;
					if(phaseTimes[0] > MAX) phaseTimes[0] = MAX;
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
		private boolean isGreen = false;
		
		public Coordination(String msg){
			super();
			if(!isNegotiating){				
				
				String lanesAffectedString = msg.split("#")[1];
				String phasesListString = msg.split("#")[2];
				
				// TODO: Use lanes affected to modify lanesPriorities
				
				String[] lanesAffected = lanesAffectedString.split(",");
				String[] phasesIds = phasesListString.split(",");		
				
				agentsAbove ="";
				boolean first = true;
				isGreen = false;
				
				for(int i = 0; i < lanesAffected.length; i++){
					int intAffected = Integer.parseInt(lanesAffected[i]);
					char charAffected = phaseValues[0].charAt(i);
					if(intAffected == 1 && charAffected == 'G'){
						isGreen = true;
						break;
					}
				}
				
				if(phasesIds[0].equals(phaseId) || phasesIds[1].equals(phaseId)){
					isGreen = false;
				}else{			
					for(int i = 1; i < phasesIds.length; i++){
						String phase = phasesIds[i];				
						if(!phaseId.equals(phase)){
							if(first){
								first = false;
								agentsAbove += phase;
							}else{
								agentsAbove += "," + phase;
							}					
						}else{
							break;
						}
					}
					this.agents = agentsAbove.split(",");
					this.agentsAccepted = new boolean[agents.length];
					phasePriority = Integer.parseInt(msg.split("#")[0]);
				}			
			}else{
				isGreen = false;
			}
		}
		
		/**
		 * Method to calculate the max offer to present to other agents
		 * @return the max number of seconds to offer
		 */
		private int getOffer(){
			int col = calculateColumnPriority();
			int row = phasePriority - 1;
			
			return offerTable[row][col];
		}
		
		public void action() {
			switch(step){
				case 0:
					// STEP 0: Calculate offer and send that to next stage in the top
					
					if(!isGreen){
						step = END_STEP;
					}else{
						isNegotiating = true;
						// System.out.println("PHASE AFFECTED: " + phaseId + " UP PHASES: " + agentsAbove);
						
						if(offerTime == -1) offerTime = getOffer(); // Get offer time value only first time
						if(offerTime > 0){
							
							// Send CFP to next stageId in the top of the list
							
							ACLMessage request = ACLMessageFactory.createCFPMsg(AIDManager.getPhaseAID(agents[currentAgent], myAgent), "1", "stage-coordination");
							myAgent.send(request);
							step++;
							
							System.out.println("stgAge" + phaseId + " offerTime: " + offerTime);
							System.out.println("stgAge" + phaseId + " offers 1s to  " + agents[currentAgent]);
							
						}else{
							
							// If the stage cannot offer any time the coordination is cancelled.
							isNegotiating = false;
							step = END_STEP;
						}
					}
					break;
				case 1:
					// STEP 1: Get refuse or propose from next agent in the top
					
					MessageTemplate mtr = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
					ACLMessage msg = myAgent.receive(mtr);
					if (msg != null) {
						
						
						// System.out.println("stgAge" + stageId + " receives refuse"); // DEBUG
						
						// If offer was refuse then coordination is over.
						isNegotiating = false;
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
										message = ACLMessageFactory.createAcceptProposalMsg(AIDManager.getPhaseAID(agents[currentAgent], myAgent), Integer.toString(timeToSend), "stage-accept");
										step++;
										myAgent.send(message);
										// System.out.println("stgAge" + stageId + " accepts offers " + timeToSend + "s to stage"); // DEBUG
									}else{
										message = ACLMessageFactory.createRejectProposalMsg(AIDManager.getPhaseAID(agents[currentAgent], myAgent), "", "stage-reject");
										isNegotiating = false;
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
							phaseTimes[0] -= timeAccepted * UNIT;
							if(phaseTimes[0] < MIN) phaseTimes[0] = MIN;
							
							ACLMessage message = ACLMessageFactory.createRequestMsg(AIDManager.getJunctionAID(junctionId, myAgent), phaseId, "stage-up");
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
							isNegotiating = false;
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
								isNegotiating = false;
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
							isNegotiating = false;
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
	
	public String getPhaseId(){
		return phaseId;
	}
	
}
