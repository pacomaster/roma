package com.iteso.roma.agents.behaviours;

import com.iteso.roma.agents.JunctionAgent;
import com.iteso.roma.jade.ConversationIds;
import com.iteso.roma.sumo.Phase;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class JunctionRequestPhaseTimeBehaviour extends Behaviour{
	
	JunctionAgent junctionAgent;
	private int step = 0;
	private final int FINAL_STEP = 2;
	
	public JunctionRequestPhaseTimeBehaviour(Agent agent){
		this.junctionAgent = (JunctionAgent)agent;
	}

	@Override
	public void action() {
		switch(step){
		case 0:					
			AID receiver = AIDManager.getPhaseAID(junctionAgent.getPhaseAgentsList().get(1).getPhaseId(), junctionAgent);	
			
			if(receiver != null){
				requestPhaseInformation(receiver);
				step++;
			}else{
				step = FINAL_STEP;
			}
			
			break;
		case 1:
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = junctionAgent.receive(mt);
			
			if(msg != null && msg.getConversationId().equals(ConversationIds.PHASE_VALUES_VEH)){				
				processMessage(msg);				
				step++;
			}else{
				step = FINAL_STEP;
			}
			
			break;					
		}
	}

	@Override
	public boolean done() {
		return step == FINAL_STEP;
	}
	
	private void requestPhaseInformation(AID receiver){
		/*
		 * Sends the request for next phase values and times
		 * 
		 * Type: REQUEST
		 * To: Next phase in queue
		 * Subject: PHASE_VALUES_TIMES
		 * Message: Next Phase
		 */
		ACLMessage request = ACLMessageFactory.createRequestMsg(receiver, "Next Phase", ConversationIds.PHASE_VALUES_VEH);
		myAgent.send(request);
	}
	
	private void processMessage(ACLMessage msg){
		Phase phase = getPhaseFromMessage(msg);
		junctionAgent.setNextPhase(phase);
	}
	
	private Phase getPhaseFromMessage(ACLMessage msg){
		
		// Message Format: GGrr,yyrr#31,4
		
		String msgContent = msg.getContent();
		String msgStates = msgContent.split("#")[0];
		String msgTimes = msgContent.split("#")[1];
		
		String[] states = msgStates.split(",");		
		int[] times = arrayStringToInt(msgTimes.split(","));
		
		return new Phase(states, times);		
	}
	
	private int[] arrayStringToInt(String[] array){
		int[] retArray = new int[array.length];
		for(int i = 0; i < array.length; i++){
			retArray[i] = Integer.parseInt(array[i]);
		}
		return retArray;
	}

}
