package com.iteso.roma.agents.behaviours;

import com.iteso.roma.agents.JunctionAgent;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class JunctionRequestPhaseTimeBehaviour extends Behaviour{
	
	JunctionAgent junctionAgent;
	private int step = 0;
	
	public JunctionRequestPhaseTimeBehaviour(Agent agent){
		this.junctionAgent = (JunctionAgent)agent;
	}

	/**
	 * Steps:
	 * <br/>
	 * 0 - Sends messages requesting phase values and times to next phase in queue.
	 * <br/>
	 * 1 - Receives response with phase values and times
	 */
	@Override
	public void action() {
		switch(step){
		case 0:					
			AID receiver = AIDManager.getPhaseAID(junctionAgent.getPhasesList().get(1).getPhaseId() , myAgent);					
			if(receiver != null){
				/*
				 * Sends the request for next phase values and times
				 * 
				 * Type: REQUEST
				 * To: Next phase in queue
				 * Subject: phase-values-times
				 * Message: Next Phase
				 */
				ACLMessage request = ACLMessageFactory.createRequestMsg(receiver, "Next Phase", "phase-values-times");
				myAgent.send(request);
				step++;
			}					
			break;
		case 1:
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String conversationId = msg.getConversationId();
				if(conversationId.equals("phase-values-times")){
					// Convert message Format: GGrr,yyrr#31,4
					String msgContent = msg.getContent();
					String msgValues = msgContent.split("#")[0];
					String msgTimes = msgContent.split("#")[1];
					junctionAgent.setNextPhaseValues(msgValues.split(","));
					junctionAgent.setNextPhaseTimes(new int[msgTimes.split(",").length]);
					int i = 0;
					for(String s : msgTimes.split(",")){
						junctionAgent.getNextPhaseTimes()[i] = Integer.parseInt(s);
						i++;
					}
					step++;
				}
			}
			break;					
	}
	}

	@Override
	public boolean done() {
		return step == 2;
	}

}
