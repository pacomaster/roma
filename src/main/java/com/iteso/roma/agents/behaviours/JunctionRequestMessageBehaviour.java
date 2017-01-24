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
public class JunctionRequestMessageBehaviour extends CyclicBehaviour{
	
	private static final Logger logger = Logger.getLogger(JunctionRequestMessageBehaviour.class.getName());
	
	JunctionAgent junctionAgent;
	
	public JunctionRequestMessageBehaviour(Agent agent){
		junctionAgent = (JunctionAgent)agent;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage msg = junctionAgent.receive(mt);
		if (msg != null) {			
			if(msg.getConversationId().equals(ConversationIds.LANE_CHANGE_NUM_VEH)){
				laneChangedNumVeh(msg);
			}
			if(msg.getConversationId().equals(ConversationIds.PHASE_UP_QUEUE)){
				phaseRequestedUpInQueue(msg);
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
		String messageContent = laneId + "#" + numVeh;
		
		for(PhaseAgent phase:junctionAgent.getPhaseAgentsList()){			
			AID receiverPhase = AIDManager.getPhaseAID(phase.getPhaseId(), junctionAgent);
			
			ACLMessage request = ACLMessageFactory.createRequestMsg(receiverPhase, messageContent, ConversationIds.LANE_CHANGE_NUM_VEH);
			junctionAgent.send(request);
		}
	}
	
	//TODO: Fix up and down queue

	private void phaseRequestedUpInQueue(ACLMessage msg) {
		// Message Format: phaseId
		String phaseId = msg.getContent();
		
		printQueue("OLD", phaseId);
		
		int index = getPhasePositionInQueue(phaseId);		
		if(index > 1){
			sendAgreeToMoveUpInQueueMessage(msg);
			movePhaseUpInQueue(index);
			sendInformThatPhaseWasMoveUpInQueueMessage(msg);
		}else{
			sendRefuseToMoveUpInQueueMessage(msg);
		}
		
		printQueue("NEW", phaseId);
	}

	private int getPhasePositionInQueue(String phaseId) {
		int i = 0;		
		for(PhaseAgent phase:junctionAgent.getPhaseAgentsList()){
			if(phase.getPhaseId().equals(phaseId)) break;
			i++;
		}
		return i;
	}
	
	private void sendAgreeToMoveUpInQueueMessage(ACLMessage msg) {
		ACLMessage reply = msg.createReply();
		reply.setPerformative(ACLMessage.AGREE);
		reply.setContent("");
		junctionAgent.send(reply);
	}
	
	private void movePhaseUpInQueue(int index) {
		junctionAgent.getPhaseAgentsList().remove(index);
		junctionAgent.getPhaseAgentsList().add(index - 1, junctionAgent.getPhaseAgentsList().get(index));
	}
	
	private void sendInformThatPhaseWasMoveUpInQueueMessage(ACLMessage msg) {
		ACLMessage reply = msg.createReply();
		reply.setPerformative(ACLMessage.INFORM);
		reply.setContent("");
		junctionAgent.send(reply);
	}
	
	private void sendRefuseToMoveUpInQueueMessage(ACLMessage msg) {
		ACLMessage reply = msg.createReply();
		reply.setPerformative(ACLMessage.REFUSE);
		reply.setContent("");
		junctionAgent.send(reply);
	}

	private void printQueue(String idQueue, String phaseId) {
		logger.info("");
		logger.info(idQueue + " QUEUE - " + phaseId + ":");
		for(PhaseAgent phase:junctionAgent.getPhaseAgentsList()){
			logger.info(phase.getPhaseId() + " ");
		}
		logger.info("");
	}

}
