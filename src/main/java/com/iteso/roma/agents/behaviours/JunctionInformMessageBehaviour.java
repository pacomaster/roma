package com.iteso.roma.agents.behaviours;

import java.util.logging.Logger;

import com.iteso.roma.agents.JunctionAgent;
import com.iteso.roma.agents.PhaseAgent;
import com.iteso.roma.jade.ConversationIds;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class JunctionInformMessageBehaviour extends CyclicBehaviour{
	
	private static final Logger _logger = Logger.getLogger(JunctionInformMessageBehaviour.class.getName());
	
	JunctionAgent junctionAgent;
	
	public JunctionInformMessageBehaviour(Agent agent){
		junctionAgent = (JunctionAgent)agent;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg = junctionAgent.receive(mt);
		if (msg != null) {			
			if(msg.getConversationId().equals(ConversationIds.LANE_CHANGE_NUM_VEH)){
				laneChangedNumVeh(msg);
			}
			if(msg.getConversationId().equals(ConversationIds.PHASE_VALUES_VEH)){
				checkPhaseVeh(msg);
			}
		}
		block();
	}
	
	private void laneChangedNumVeh(ACLMessage msg) {		
		// Message Format: laneId,numVeh	
		String content = msg.getContent();
		String laneId = content.split(",")[0];
		int numVeh = Integer.parseInt(content.split(",")[1]);
		
		sendLaneChangedNumVehMessage(laneId, numVeh);
	}
	
	private void sendLaneChangedNumVehMessage(String laneId, int numVeh) {
		String messageContent = laneId + "," + numVeh;
		
		for(PhaseAgent phase : junctionAgent.getPhaseAgentsList()){
			for(String lane : phase.getLanesAffected()){
				if(lane.equals(laneId)){
					AID receiverPhase = AIDManager.getPhaseAID(phase.getPhaseId(), junctionAgent);
					
					ACLMessage inform = ACLMessageFactory.createInformMsg(receiverPhase, messageContent, ConversationIds.LANE_CHANGE_NUM_VEH);
					junctionAgent.send(inform);
					ACLMessageFactory.logMessage(inform);
					break;
				}
			}
		}
	}
	
	private void checkPhaseVeh(ACLMessage msg) {
		String content = msg.getContent();
		String phaseid = content.split(",")[0];
		int numVeh = Integer.parseInt(content.split(",")[1]);
	}
}
