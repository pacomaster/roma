package com.iteso.roma.agents.behaviours;

import org.apache.commons.lang3.StringUtils;

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
				phase.setGreenTime(phaseAgent.MIDDLE);
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
				reply.setContent(phaseAgent.getPhaseId());
				myAgent.send(reply);
				
				// Start coordination with change priority
				myAgent.addBehaviour(new PhaseCoordinationBehaviour(myAgent));
			}
		}			
	}
}
