package com.iteso.roma.agents.behaviours;

import com.iteso.roma.agents.PhaseAgent;
import com.iteso.roma.jade.ConversationIds;
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

@SuppressWarnings("serial")
public class PhaseCoordinationBehaviour extends CyclicBehaviour {
	
	private PhaseAgent phaseAgent;
	private Phase phase;
	TableNegotiationResolver negotiationResolver = new TableNegotiationResolver(OfferTables.tableA, DealTables.tableA);
	
	public PhaseCoordinationBehaviour(Agent agent){
		phaseAgent = (PhaseAgent)agent;
		phase = phaseAgent.getPhase();
	}
	
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
					phase.setGreenTime(phase.getGreenTime() + Integer.parseInt(msg.getContent())*phaseAgent.UNIT);
					reply.setContent(phaseAgent.getPhaseId());
					myAgent.send(reply);
					System.out.println("phaAge: " + phaseAgent.getPhaseId() + " Refuses"); // DEBUG
				}else{
					int timeProposal = negotiationResolver.proposeCounterOffer(getPhaseStatus()).offeredUnits;
					if(offer.offeredUnits > timeProposal)timeProposal = offer.offeredUnits;
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(Integer.toString(timeProposal));
					myAgent.send(reply);
					System.out.println("phaAge: " + phaseAgent.getPhaseId() + " propose deal: " + timeProposal); // DEBUG
					System.out.println("phaAge: " + phaseAgent.getPhaseId() + " Proposes"); // DEBUG
				}
			}
		}
		
		MessageTemplate mta = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		msg = myAgent.receive(mta);
		if (msg != null) {
			System.out.println("phaAge: " + phaseAgent.getPhaseId() + " receives accepts"); // DEBUG
			String conversationId = msg.getConversationId();
			if(conversationId.equals("stage-accept")){
				ACLMessage reply = msg.createReply();
				int timeAccepted = Integer.parseInt(msg.getContent());
				phase.setGreenTime(phase.getGreenTime() + timeAccepted*phaseAgent.UNIT);
				if(phase.getGreenTime() > phaseAgent.MAX) phase.setGreenTime(phaseAgent.MAX);
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(Integer.toString(timeAccepted));
				myAgent.send(reply);
				
				System.out.println("phaAge: " + phaseAgent.getPhaseId() + " inform accepts"); // DEBUG
			}
		}
		
		MessageTemplate mtr = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
		msg = myAgent.receive(mtr);
		if (msg != null) {
			System.out.println("phaAge: " + phaseAgent.getPhaseId() + " receives reject"); // DEBUG
			String conversationId = msg.getConversationId();
			if(conversationId.equals("stage-reject")){
				// Do nothing
				System.out.println("phaAge: " + phaseAgent.getPhaseId() + " inform reject"); // DEBUG
			}
		}			
	}
	
	private PhaseStatus getPhaseStatus(){
		PhaseStatus status = new PhaseStatus();
		
		status.idealTime = phaseAgent.MIDDLE;
		status.priority = phase.getPhasePriority();
		status.secondsLeft = phase.getGreenTime();
		
		return status;
	}
}
