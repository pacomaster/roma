package com.iteso.roma.agents.behaviours;

import java.util.ArrayList;
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
			if(msg.getConversationId().equals(ConversationIds.LANE_CHANGE_PRIORITY)){
				laneChangedPriority(msg);
			}
			if(msg.getConversationId().equals(ConversationIds.PHASE_UP_QUEUE)){
				phaseRequestedUpInQueue(msg);
			}
		}
		block();
	}
	
	private void laneChangedPriority(ACLMessage msg) {		
		// Message Format: laneId,priority	
		String content = msg.getContent();
		String laneId = content.split(",")[0];
		int priority = Integer.parseInt(content.split(",")[1]);
		
		String lanesAffected = getLanesAffectedInJunction(laneId);
		String phasesOrder = getCurrentPhasesOrderInJunction();
		sendLaneChangedPriorityMessage(lanesAffected, phasesOrder, priority);
	}

	private String getLanesAffectedInJunction(String laneId) {
		ArrayList<String> laneNames = junctionAgent.getSumoTrafficLight().getControlledLanes();
		
		String lanesAffected = String.valueOf(laneNames.get(0).equals(laneId) ? 1 : 0);
		for (int i = 1; i < laneNames.size(); i++) {
			lanesAffected += "," + (laneNames.get(i).equals(laneId) ? 1 : 0);
		}
		
		return lanesAffected;
	}
	
	private String getCurrentPhasesOrderInJunction() {
		ArrayList<PhaseAgent> phaseAgentsList = junctionAgent.getPhaseAgentsList();
		
		String phasesOrder = phaseAgentsList.get(0).getPhaseId();
		for(int i = 1; i < phaseAgentsList.size(); i++){
			phasesOrder += "," + phaseAgentsList.get(i).getPhaseId();
		}
		return phasesOrder;
	}
	
	private void sendLaneChangedPriorityMessage(String lanesAffected, String phasesOrder, int priority) {	
		String messageContent = priority + "#" + lanesAffected + "#" + phasesOrder;
		
		for(PhaseAgent phase:junctionAgent.getPhaseAgentsList()){			
			AID receiverPhase = AIDManager.getPhaseAID(phase.getPhaseId(), junctionAgent);
			
			ACLMessage request = ACLMessageFactory.createRequestMsg(receiverPhase, messageContent, ConversationIds.LANE_CHANGE_PRIORITY);
			junctionAgent.send(request);
		}
	}

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
