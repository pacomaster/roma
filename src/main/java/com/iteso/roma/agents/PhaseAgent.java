package com.iteso.roma.agents;

import org.apache.commons.lang3.StringUtils;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PhaseAgent extends Agent{
	
	private String phaseId;
	private String junctionId;
	
	private String[] phaseValues;
	private int[] phaseTimes;
	
	public PhaseAgent(String phaseId, String junctionId, int[] phaseTimes, String[] phaseValues) {
		this.phaseId = phaseId;
		this.junctionId = junctionId;
		this.phaseTimes = phaseTimes;
		this.phaseValues = phaseValues;
	}
	
	protected void setup(){
		// Register the phase service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(phaseId);
		sd.setName(phaseId);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new RequestPhase());
	}
	
	private class RequestPhase extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);			
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// REQUEST Message received. Process it
				String conversationId = msg.getConversationId();
				// Validate change request
				if(conversationId.equals("phase-values-times")){
					ACLMessage reply = msg.createReply();
					// Send values and times
					reply.setPerformative(ACLMessage.INFORM);
					String msgValues = StringUtils.join(phaseValues,",");
					String msgTimes = String.valueOf(phaseTimes[0]);
					for(int i = 1; i < phaseTimes.length; i++){
						msgTimes += "," + phaseTimes[i];
					}									
					reply.setContent(msgValues + "#" + msgTimes);					
					myAgent.send(reply);
				}
				
				// Request to change the priority from JunctionAgent				
//				if(conversationId.equals("change-priority")){
//					ACLMessage reply = msg.createReply();
//					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
//					reply.setContent(Integer.toString(stageId));
//					myAgent.send(reply);
//					
//					myAgent.addBehaviour(new Coordination(msg.getContent())); // Start coordination with change priority
//				}
			}			
		}
	}
	
	public String getPhaseId(){
		return phaseId;
	}
	
}
