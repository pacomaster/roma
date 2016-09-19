package com.iteso.roma.agents.behaviours;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.iteso.roma.agents.JunctionAgent;
import com.iteso.roma.agents.PhaseAgent;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class JunctionRequestMessageBehaviour extends CyclicBehaviour{
	
	private static final Logger logger = Logger.getLogger(JunctionRequestMessageBehaviour.class.getName());
	
	JunctionAgent junctionAgent;
	
	public JunctionRequestMessageBehaviour(Agent agent){
		junctionAgent = (JunctionAgent)agent;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			String conversationId = msg.getConversationId();
			
			// Request change of priority from LaneAgent				
			if(conversationId.equals("lane-change-priority")){
				
				// Convert message Format: laneId,priority	
				String content = msg.getContent();
				String laneId = content.split(",")[0];
				int priority = Integer.parseInt(content.split(",")[1]);
				
				// Create list with the lanes in junction affected by the priority change
				ArrayList<String> laneNames = junctionAgent.getSumoTrafficLight().getControlledLanes();
				String lanesAffected = "";
				boolean first = true;
				for(String laneName :laneNames){
					if(laneName.equals(laneId)){
						if(first){
							first = false;
							lanesAffected += 1;
						}else{
							lanesAffected += "," + 1;
						}							
					}else{
						if(first){
							first = false;
							lanesAffected += 0;
						}else{
							lanesAffected += "," + 0;
						}	
					}
				}
				
				// Create list of PhaseAgents in order.
				String phasesOrder = junctionAgent.getPhasesList().get(0).getPhaseId();
				for(int i = 1; i < junctionAgent.getPhasesList().size(); i++){
					phasesOrder += "," + junctionAgent.getPhasesList().get(i).getPhaseId();
				}
				
				/*
				 * Sends the request that a lane changed priority
				 * 
				 * Type: REQUEST
				 * To: Every phase
				 * Subject: lane-change-priority
				 * Message: [priority]#[lanesAffectedArray]#[phasesOrderArray]
				 */
				for(PhaseAgent phase:junctionAgent.getPhasesList()){						
					ACLMessage request = ACLMessageFactory.createRequestMsg(
							AIDManager.getPhaseAID(phase.getPhaseId(), myAgent), 
							priority + "#" + lanesAffected + "#" + phasesOrder,
							"lane-change-priority");
					myAgent.send(request);
					
					// logger.info("lane-change-priority: " + phase.getPhaseId() + " Msg:" + priority + "#" + lanesAffected + "#" + phasesOrder);
				}
			}
			
			// Request from a PhaseAgent to get up in the order
			if(conversationId.equals("stage-up")){
				String content = msg.getContent();
				// Convert message Format: phaseId
				String stageId = content;
				
				int index = 0;
				int i = 0;
				PhaseAgent auxPhaseAgent = null;
				
				// Get the position of the PhaseAgent in the list
				for(PhaseAgent phase:junctionAgent.getPhasesList()){
					if(phase.getPhaseId().equals(stageId)){
						auxPhaseAgent = phase;
						index = i;
						break;
					}
					i++;
				}
				
				// DEBUG See the content of the queue before change
				logger.info("");
				logger.info("OLD QUEUE - " + stageId + ":");
				for(PhaseAgent phase:junctionAgent.getPhasesList()){
					logger.info(phase.getPhaseId() + " ");
				}
				logger.info("");
				
				if(index > 1){						
					/*
					 * Sends agree message if PhaseAgent is not already at the top
					 * 
					 * Type: AGREE
					 * To: Phase that requested to go up one position
					 * Subject: stage-up
					 * Message: 
					 */
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.AGREE);
					reply.setContent("");
					myAgent.send(reply);
					
					// Move PhaseAgent up one position in the list
					junctionAgent.getPhasesList().remove(index);
					junctionAgent.getPhasesList().add(index - 1, auxPhaseAgent);
					
					/*
					 * Sends inform message if PhaseAgent notifying change was done.
					 * 
					 * Type: INFORM
					 * To: Phase that requested to go up one position
					 * Subject: stage-up
					 * Message: 
					 */
					reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("");
					myAgent.send(reply);
				}else{
					/*
					 * Sends refuse message if PhaseAgent already at the top
					 * 
					 * Type: REFUSE
					 * To: Phase that requested to go up one position
					 * Subject: stage-up
					 * Message: 
					 */
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("");
					myAgent.send(reply);
				}
				
				// DEBUG Check the content of the queue after change
				logger.info("");
				logger.info("NEW QUEUE - " + stageId + ":");
				for(PhaseAgent phase:junctionAgent.getPhasesList()){
					logger.info(phase.getPhaseId() + " ");
				}
				logger.info("");
			}
			
		}
		block();
	}

}
