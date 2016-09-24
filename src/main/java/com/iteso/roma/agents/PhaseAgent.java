package com.iteso.roma.agents;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.iteso.roma.jade.ConversationIds;
import com.iteso.roma.jade.ServiceRegister;
import com.iteso.roma.negotiation.DealTables;
import com.iteso.roma.negotiation.Offer;
import com.iteso.roma.negotiation.OfferTables;
import com.iteso.roma.negotiation.PhaseStatus;
import com.iteso.roma.negotiation.TableNegotiationResolver;
import com.iteso.roma.sumo.Phase;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
@SuppressWarnings("serial")
public class PhaseAgent extends Agent{
	
	private final int MIDDLE;
	private final int MAX;
	private final int UNIT;
	
	private String phaseId;
	
	private Phase phase;
	
	private int[] lanesPriorities;
	
	TableNegotiationResolver negotiationResolver = new TableNegotiationResolver(OfferTables.tableA, DealTables.tableA);
	
	/**
	 * Constructor
	 * @param phaseId The name of the phase
	 * @param junctionId The name of the junction this phase is attached
	 * @param phaseTimes The phases duration
	 * @param phaseValues The phases values
	 */
	public PhaseAgent(String phaseId, String junctionId, Phase phase) {
		this.phaseId = phaseId;
		this.phase = phase;
		
		// A unit is the way  the phase knows how many seconds needs to deal or offer using dealTable and offerTable
		MIDDLE = phase.getGreenTime();
		UNIT = MIDDLE / 5;
		MAX = MIDDLE + (UNIT*2);
		
		// Check  how many lanes are set in green for this phase		
		lanesPriorities = new int[phase.getStatesLength()];
		
		/* Create an array where 1 is a green space in the phase
		 * Example:
		 * 
		 * PHASE VALUE: GGGrrrrrGGGrrrrr
		 * LANES PRIORITIES: [1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0]
		 * 
		 * PHASE VALUE: rrrGrrrrrrrGrrrr
		 * LANES PRIORITIES: [0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0]
		 */		
		for(int i = 0; i < lanesPriorities.length; i++) {
		    if(phase.getGreenState().charAt(i) == 'G') {
		        lanesPriorities[i] = 1;
		    }else{
		    	lanesPriorities[i] = 0;
		    }
		}
		phase.setPhasePriority(calculatePhasePriority());		
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
	 * This class setups the agent
	 */
	protected void setup(){		
		ServiceRegister.register(this, phaseId);		
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
				if(conversationId.equals(ConversationIds.PHASE_VALUES_TIMES)){					
					/*
					 * Sends the inform to junction about phase values and times
					 * 
					 * Type: INFORM
					 * To: Junction
					 * Subject: PHASE_VALUES_TIMES
					 * Message: [phaseValues]#[phaseTimes]
					 */
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					String msgValues = StringUtils.join(phase.getStates(),",");
					String msgTimes = String.valueOf(phase.getTimes()[0]);
					for(int i = 1; i < phase.getTimes().length; i++){
						msgTimes += "," + phase.getTimes()[i];
					}									
					reply.setContent(msgValues + "#" + msgTimes);					
					myAgent.send(reply);
					phase.setGreenTime(MIDDLE);
				}
				
				// Request to change the priority from JunctionAgent				
				if(conversationId.equals(ConversationIds.LANE_CHANGE_PRIORITY)){
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
	
	private class CoordinationCFP extends CyclicBehaviour {
		
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);			
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String conversationId = msg.getConversationId();
				if(conversationId.equals(ConversationIds.PHASE_COORDINATION)){
					ACLMessage reply = msg.createReply();
					
					Offer offer = new Offer();
					offer.offeredUnits = Integer.parseInt(msg.getContent()); 
					
					if(!negotiationResolver.acceptOffer(getPhaseStatus(), offer)){
						reply.setPerformative(ACLMessage.REFUSE);
						phase.setGreenTime(phase.getGreenTime() + Integer.parseInt(msg.getContent())*UNIT);
						reply.setContent(phaseId);
						myAgent.send(reply);
						System.out.println("phaAge: " + phaseId + " Refuses"); // DEBUG
					}else{
						int timeProposal = negotiationResolver.proposeCounterOffer(getPhaseStatus()).offeredUnits;
						if(offer.offeredUnits > timeProposal)timeProposal = offer.offeredUnits;
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(Integer.toString(timeProposal));
						myAgent.send(reply);
						System.out.println("phaAge: " + phaseId + " propose deal: " + timeProposal); // DEBUG
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
					phase.setGreenTime(phase.getGreenTime() + timeAccepted*UNIT);
					if(phase.getGreenTime() > MAX) phase.setGreenTime(MAX);
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
	
	private PhaseStatus getPhaseStatus(){
		PhaseStatus status = new PhaseStatus();
		
		status.idealTime = MIDDLE;
		status.priority = phase.getPhasePriority();
		status.secondsLeft = phase.getGreenTime();
		
		return status;
	}
	
	
}
