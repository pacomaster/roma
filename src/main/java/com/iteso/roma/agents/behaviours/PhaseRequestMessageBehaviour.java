package com.iteso.roma.agents.behaviours;

import com.iteso.roma.agents.PhaseAgent;
import com.iteso.roma.jade.ConversationIds;
import com.iteso.roma.sumo.Phase;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class PhaseRequestMessageBehaviour extends CyclicBehaviour {
	
	private PhaseAgent phaseAgent;
	private Phase phase;
	
	public PhaseRequestMessageBehaviour(Agent agent){
		phaseAgent = (PhaseAgent)agent;
		phase = phaseAgent.getPhase();
	}

	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);			
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			String conversationId = msg.getConversationId();
			
			// Request to provide phase values and times
			if(conversationId.equals(ConversationIds.PHASE_VALUES_VEH)){					
				/*
				 * Sends the inform to junction about phase veh
				 * 
				 * Type: INFORM
				 * To: Junction
				 * Subject: PHASE_VALUES_VEH
				 * Message: phaseId,vehNum
				 */
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);									
				reply.setContent(phaseAgent.getPhaseId() + "," + phaseAgent.getTotalVeh());					
				myAgent.send(reply);
			}
		}					
	}
}
