package com.iteso.roma.agents;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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

/**
 * Class to represent a phase in SUMO.
 * <br/>
 * <br/>
 * A phase is an array that represents the state of the junction each step of the phase
 * <br/>
 * <br/>
 * Example:
 * <br/>
 * A cross street [+] 
 * Two possible ways to move NS and EW
 * When cars move NS the phase will look like "Gr" (green NS and red EW)
 * Then needs to be a yellow light like this "yr"
 * Then cars move EW the phase changes to "rG" (red NS and green EW)
 * Finally yellow for EW which is "ry"
 * And cycle repeats
 * So for this we have two arrays phaseValues and phaseTimes
 * phaseValues will contain {"Gr","yr","rG","ry"}
 * phaseTimes will contain {15,4,15,4} the times the phase should be active
 * <br/>
 * <br/> 
 * SUMO considers every single element in as a phase but for the purpose of optimizing
 * times for cars to move, the phase will be consider Green and yellow times like {"Gr","yr"} for NS
 * @author Francisco Amezcua
 *
 */
public class PhaseAgent extends Agent{
	
	private static final Logger logger = Logger.getLogger(JunctionAgent.class.getName());
	
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
			{0,0,0,0,1},
			{0,0,0,1,1},
			{0,0,1,1,2},
			{0,1,1,2,3},
			{1,1,2,3,4}};
	
	private int[][] dealTable = {
			{2  ,1  ,1  ,1  ,INF},
			{3  ,2  ,1  ,1  ,INF},
			{4  ,3  ,2  ,1  ,INF},
			{4  ,3  ,2  ,INF,INF},
			{INF,INF,INF,INF,INF}};
	
	/**
	 * Constructor
	 * @param phaseId The name of the phase
	 * @param junctionId The name of the junction this phase is attached
	 * @param phaseTimes The phases duration
	 * @param phaseValues The phases values
	 */
	public PhaseAgent(String phaseId, String junctionId, int[] phaseTimes, String[] phaseValues) {
		this.phaseId = phaseId;
		this.junctionId = junctionId;
		this.phaseTimes = phaseTimes;
		this.phaseValues = phaseValues;
		
		// A unit is the way  the phase knows how many seconds needs to deal or offer using dealTable and offerTable
		MIDDLE = phaseTimes[0];
		UNIT = MIDDLE / 5;
		MIN = MIDDLE - UNIT - UNIT;
		MAX = MIDDLE + UNIT + UNIT;
		
		// Check  how many lanes are set in green for this phase		
		lanesPriorities = new int[phaseValues[0].length()];
		lanesGreen = 0;
		
		/* Create an array where 1 is a green space in the phase
		 * Example:
		 * 
		 * PHASE VALUE: GGGrrrrrGGGrrrrr
		 * LANES GREEN: 6
		 * LANES PRIORITIES: [1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0]
		 * 
		 * PHASE VALUE: rrrGrrrrrrrGrrrr
		 * LANES GREEN: 2
		 * LANES PRIORITIES: [0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0]
		 */		
		int counter = 0;
		for(int i = 0; i < phaseValues[0].length(); i++) {
		    if(phaseValues[0].charAt(i) == 'G') {
		        counter++;
		        lanesPriorities[i] = 1;
		    }else{
		    	lanesPriorities[i] = 0;
		    }
		}
		lanesGreen += counter;
		phasePriority = calculatePhasePriority();		
	}
	
	/**
	 * Function to calculate all the lane priorities average
	 * @return the priority average
	 */
	private int calculatePhasePriority(){
		
//		int sum = 0;
//		for(int p: lanesPriorities){
//			sum += p;
//		}
//		return sum / lanesGreen;
		
		int max = 0;
		for(int p: lanesPriorities){
			if(p > max) max = p;
		}
		return max;
	}
	
	/**
	 * Checks currentTime to know the column space in table.
	 * @return
	 */
	private int calculateColumnPriority(){
		int ret = 0;
		int currentTime = MIN + UNIT;
		while(phaseTimes[0] >= currentTime){
			ret++;
			currentTime += UNIT;
		}
		return ret;
	}
	
	/**
	 * This class setups the agent
	 */
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
		
		addBehaviour(new RequestMessage());
		addBehaviour(new CoordinationCFP());
		
	}
	
	/**
	 * Behaviour to check every REQUEST message
	 * @author Francisco Amezcua
	 *
	 */
	private class RequestMessage extends CyclicBehaviour {
		/**
		 * Check for request messages
		 */
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);			
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String conversationId = msg.getConversationId();
				
				// Request to provide phase values and times
				if(conversationId.equals("phase-values-times")){					
					/*
					 * Sends the inform to junction about phase values and times
					 * 
					 * Type: INFORM
					 * To: Junction
					 * Subject: phase-values-times
					 * Message: [phaseValues]#[phaseTimes]
					 */
					ACLMessage reply = msg.createReply();
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
					/*
					 * Notify junction that accepts to change the priority and starts coordination
					 * 
					 * Type: INFORM
					 * To: Junction
					 * Subject: lane-change-priority
					 * Message: [phaseId]
					 */
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					reply.setContent(phaseId);
					//myAgent.send(reply);
					
					// Start coordination with change priority
					//myAgent.addBehaviour(new Coordination(msg.getContent()));
				}
			}			
		}
	}
	
	/**
	 * Class to coordinate the change of priority and negotiations
	 * This is for the phase that starts the coordination
	 * @author Francisco Amezcua
	 *
	 */
	private class Coordination extends Behaviour {
		
		private int step = 0;
		private String agentsAbove;
		private int currentAgent = 0;
		private String[] agents;
		private int offerTime = -1;
		private final int END_STEP = 5;
		// isGreen means this phase contains green for this lane
		private boolean isGreen = false;
		
		/**
		 * Constructor
		 * Starts the coordination
		 * @param msg Requires the string message that started the coordination
		 */
		public Coordination(String msg){
			super();
			if(!isNegotiating){
				
				// Convert message Format: priority,lanesAffectedArray,phaseList
				int priority = Integer.parseInt(msg.split("#")[0]);
				String lanesAffectedString = msg.split("#")[1];
				String phasesListString = msg.split("#")[2];
				
				String[] lanesAffected = lanesAffectedString.split(",");
				String[] phasesIds = phasesListString.split(",");		
				
				agentsAbove ="";
				boolean first = true;
				isGreen = false;
				
				/*
				 * Compare lanes affected vs lanes with green in this phase
				 * Example:
				 * new priority = 2
				 * 1,1,0,0
				 * GGrr
				 * 2,2,0,0
				 * this will change isGreen=true because the lanes affected match the lanes with green 
				 */				
				for(int i = 0; i < lanesAffected.length; i++){
					int intAffected = Integer.parseInt(lanesAffected[i]);
					char charAffected = phaseValues[0].charAt(i);
					if(intAffected == 1 && charAffected == 'G'){
						lanesPriorities[i] = priority;
						isGreen = true;
					}
				}
				
				// If this phaseId at top or one place from top this phase shouldn't start negotiation
				if(phasesIds[0].equals(phaseId) || phasesIds[1].equals(phaseId)){
					// logger.info("CANT NEGOTIATE " + phaseId + " is at the top of the list.");
					isGreen = false;
				}else{
					if(isGreen){
						// Check agents above to start negotiation
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
						
						// Reverse order for negotiation
						String[] agentsOrder = agentsAbove.split(",");
						String agentsAboveReverse = "";
						first = true;
						for(int i = agentsOrder.length - 1; i >= 0; i--){
							String phase = agentsOrder[i];				
							if(first){
								first = false;
								agentsAboveReverse += phase;
							}else{
								agentsAboveReverse += "," + phase;
							}					

						}
						
						this.agents = agentsAboveReverse.split(",");
						
						// Change current priority to start negotiation
						phasePriority = calculatePhasePriority();
						// logger.info("NEGOTIATION START " + phaseId + " priority: " + phasePriority +  " with: " + agentsAbove);
					}else{
						// logger.info("CANT NEGOTIATE " + phaseId + " not lanes in green");
					}
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
			
			// logger.info("PHASE: " + phaseId + " COL: " + (col + 1) + " ROW: " + (row + 1));
			// logger.info("OFFER: " + offerTable[row][col]);
			
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
						System.out.println("phaAge: " + phaseId + " UP PHASES: " + agentsAbove);
						
						if(offerTime == -1) offerTime = getOffer(); // Get offer time value only first time						
						if(offerTime > 0){							
							// Send CFP to next stageId in the top of the list
							
							ACLMessage request = ACLMessageFactory.createCFPMsg(AIDManager.getPhaseAID(agents[currentAgent], myAgent), "1", "stage-coordination");
							myAgent.send(request);
							step++;
							
							System.out.println("phaAge: " + phaseId + " offers 1 unit to  " + agents[currentAgent]);
							
						}else{							
							// If the phase cannot offer any time the coordination is cancelled.
							isNegotiating = false;
							step = END_STEP;
							System.out.println("NEGOTIATION CANCEL " + phaseId + " offer = 0");
						}
					}
					break;
				case 1:
					// STEP 1: Get refuse or propose from next agent in the top
					
					MessageTemplate mtr = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
					ACLMessage msg = myAgent.receive(mtr);
					if (msg != null) {
						
						
						 System.out.println("phaAge: " + phaseId + " receives refuse"); // DEBUG
						
						// If offer was refuse then coordination is over.
						isNegotiating = false;
						step = END_STEP;
						System.out.println("NEGOTIATION CANCEL " + phaseId + " offer refused");
					}else{
						MessageTemplate mtp = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
						msg = myAgent.receive(mtp);
						if (msg != null) {
							
							// If stage response with a propose
							
							String conversationId = msg.getConversationId();
							
							if(conversationId.equals("stage-coordination")){
								int timeProposal = Integer.parseInt(msg.getContent());
								int timeToSend = 1;
								
								System.out.println("phaAge: " + phaseId + " receives propose " + timeProposal + " unit"); // DEBUG
								
								ACLMessage message = null;
								
								// Check if with offerTime left stageAgent can accept the proposal
								
								if(timeProposal >= timeToSend){
									if(offerTime - timeProposal >= 0){
										timeToSend = timeProposal;
										offerTime -= timeProposal;
										message = ACLMessageFactory.createAcceptProposalMsg(AIDManager.getPhaseAID(agents[currentAgent], myAgent), Integer.toString(timeToSend), "stage-accept");
										step++;
										myAgent.send(message);
										System.out.println("phaAge: " + phaseId + " accepts offers " + timeToSend + " unit to stage"); // DEBUG
									}else{
										message = ACLMessageFactory.createRejectProposalMsg(AIDManager.getPhaseAID(agents[currentAgent], myAgent), "", "stage-reject");
										isNegotiating = false;
										step = END_STEP;
										myAgent.send(message);
										
										System.out.println("NEGOTIATION CANCEL " + phaseId + " rejects offer");
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
							
							System.out.println("phaAge: " + phaseId + " request up to junAge" + junctionId); // DEBUG
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
							System.out.println("phaAge: " + phaseId + " received agree from junAge" + junctionId); // DEBUG
						}
					}
					
					MessageTemplate mtre = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
					msg = myAgent.receive(mtre);
					if (msg != null) {
						
						// If intersection response with refuse
						
						String conversationId = msg.getConversationId();
						
						if(conversationId.equals("stage-up")){
							System.out.println("phaAge: " + phaseId + " received refuse from junAge" + junctionId); // DEBUG
							isNegotiating = false;
							step = END_STEP;
							System.out.println("NEGOTIATION CANCEL " + phaseId + " junction refuse");
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
							
							System.out.println("phaAge: " + phaseId + " received inform from junAge" + junctionId); // DEBUG
							
							if(currentAgent < agents.length){
								currentAgent++;
								step = 0;
							}else{
								isNegotiating = false;
								step = END_STEP;
								System.out.println("NEGOTIATION CANCEL " + phaseId + " cannot get up");
							}
						}
					}
					
					MessageTemplate mtf = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
					msg = myAgent.receive(mtf);
					if (msg != null) {
						
						// If intersection response failure to up position
						
						String conversationId = msg.getConversationId();
						
						if(conversationId.equals("stage-up")){
							System.out.println("phaAge: " + phaseId + " received failure from junAge" + junctionId); // DEBUG
							isNegotiating = false;
							step = END_STEP;
							System.out.println("NEGOTIATION CANCEL " + phaseId + " junction failed to get up");
						}
					}
					break;
			}
		}

		@Override
		public boolean done() {
			return step == END_STEP;
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
					
					System.out.println("phaAge: " + phaseId + " propose deal: " + timeProposal); // DEBUG
					
					if(timeProposal == INF){
						reply.setPerformative(ACLMessage.REFUSE);
						phaseTimes[0] += Integer.parseInt(msg.getContent()) * UNIT;
						reply.setContent(phaseId);
						myAgent.send(reply);
						System.out.println("phaAge: " + phaseId + " Refuses"); // DEBUG
					}else{
						int timeCFP = Integer.parseInt(msg.getContent());
						if(timeCFP > timeProposal)timeProposal = timeCFP;
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(Integer.toString(timeProposal));
						myAgent.send(reply);
						System.out.println("phaAge: " + phaseId + " Proposes"); // DEBUG
					}
				}
			}
			
			MessageTemplate mta = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			msg = myAgent.receive(mta);
			if (msg != null) {
				System.out.println("phaAge: " + phaseId + " receives accepts"); // DEBUG
				String conversationId = msg.getConversationId();
				if(conversationId.equals("stage-accept")){
					ACLMessage reply = msg.createReply();
					int timeAccepted = Integer.parseInt(msg.getContent());
					phaseTimes[0] += timeAccepted * UNIT;
					if(phaseTimes[0] > MAX) phaseTimes[0] = MAX;
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(Integer.toString(timeAccepted));
					myAgent.send(reply);
					
					System.out.println("phaAge: " + phaseId + " inform accepts"); // DEBUG
				}
			}
			
			MessageTemplate mtr = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
			msg = myAgent.receive(mtr);
			if (msg != null) {
				System.out.println("phaAge: " + phaseId + " receives reject"); // DEBUG
				String conversationId = msg.getConversationId();
				if(conversationId.equals("stage-reject")){
					// Do nothing
					System.out.println("phaAge: " + phaseId + " inform reject"); // DEBUG
				}
			}			
		}
	}
	
	public String getPhaseId(){
		return phaseId;
	}
	
}
