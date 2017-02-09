package com.iteso.roma.agents.behaviours;

import java.util.logging.Logger;

import com.iteso.roma.agents.PhaseAgent;
import com.iteso.roma.jade.ConversationIds;
import com.iteso.roma.sumo.Phase;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class PhaseInformMessageBehaviour extends CyclicBehaviour {
	
	private static final Logger _logger = Logger.getLogger(PhaseInformMessageBehaviour.class.getName());
	
	private PhaseAgent phaseAgent;
	private Phase phase;
	
	public PhaseInformMessageBehaviour(Agent agent){
		phaseAgent = (PhaseAgent)agent;
		phase = phaseAgent.getPhase();
	}

	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			String conversationId = msg.getConversationId();
			
			// Request to change the priority from JunctionAgent				
			if(conversationId.equals(ConversationIds.LANE_CHANGE_NUM_VEH)){
				/*
				 * Notify junction that accepts to change the priority and starts coordination
				 * 
				 * Type: INFORM
				 * To: Junction
				 * Subject: lane-change-num-veh
				 * Message: [laneId],numVeh
				 */				
				String content = msg.getContent();
				String laneId = content.split(",")[0];
				int numVeh = Integer.parseInt(content.split(",")[1]);
				
				String[] lanesAffected = phaseAgent.getLanesAffected();
				int[] lanesAffectedVeh = phaseAgent.getLanesAffectedVeh();
				
				int total = 0;
				for(int i = 0; i < lanesAffected.length; i++){
					if(lanesAffected[i].equals(laneId)){
						lanesAffectedVeh[i] = numVeh;
					}
					total += lanesAffectedVeh[i];
				}
				phaseAgent.setTotalVeh(total);
				
				//_logger.info(phaseAgent.getPhaseId() + " total: " + total);
			}
		}			
	}
}
