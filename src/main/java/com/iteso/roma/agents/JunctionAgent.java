package com.iteso.roma.agents;

import java.util.ArrayList;

import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;
import com.iteso.roma.utils.TimeManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ContainerController;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoTrafficLight;

public class JunctionAgent extends Agent{
	
	private String junctionId;
	private SumoTrafficLight myself;
	
	private String[] phaseValues;
	private int[] phaseTimes;
	private String[] nextPhaseValues;
	private int[] nextPhaseTimes;
	private int phaseStep = 0;
	
	private int nextCycle;
	
	private ArrayList<PhaseAgent> phasesList = new ArrayList<PhaseAgent>();
	
	private ContainerController mainContainer;
	
	public JunctionAgent(String junctionId, int[] phaseTimes, String[] phaseValues, ArrayList<PhaseAgent> phasesList, ContainerController mainContainer) {
		this.junctionId = junctionId;
		this.myself = new SumoTrafficLight(junctionId);
		this.phaseTimes = phaseTimes;
		this.phaseValues = phaseValues;
		this.phasesList = phasesList;
		this.mainContainer = mainContainer;
		nextCycle = phaseTimes[phaseStep];
	}
	
	protected void setup(){
		// Register the junction service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(junctionId);
		sd.setName(junctionId);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){					
					changePhase(myAgent);					
					System.out.println(sumoTime + " " + junctionId + "-" + phasesList.get(0).getPhaseId() + " P: " + phaseStep + " nextCycle: " + phaseTimes[phaseStep]);
				}
			}
		});
		
		addBehaviour(new RequestMessage());
	}
	
	public void changePhase(Agent myAgent){
		phaseStep++;
		// Check for last phase
		if(phaseStep == phaseTimes.length - 1){
			// Request next phase
			myAgent.addBehaviour(new RequestPhaseTime());
		}
		if(phaseStep == phaseTimes.length){
			phaseStep = 0;
			phaseValues = nextPhaseValues;
			phaseTimes = nextPhaseTimes;
			phasesList.add(phasesList.get(0));
			phasesList.remove(0);
		}
		myself.setState(phaseValues[phaseStep]);
		nextCycle += phaseTimes[phaseStep];
	}
	
	private class RequestPhaseTime extends Behaviour{
		private int step = 0;
		
		public void action() {
			switch(step){
				case 0:
					AID receiver = AIDManager.getPhaseAID(phasesList.get(1).getPhaseId() , myAgent);					
					if(receiver != null){
						// Send the request for next phase values and times
						ACLMessage request = ACLMessageFactory.createRequestMsg(receiver, "Next Phase", "phase-values-times");
						myAgent.send(request);
						step++;
					}					
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					ACLMessage msg = myAgent.receive(mt);
					if (msg != null) {
						// REQUEST Message received. Process it
						String conversationId = msg.getConversationId();
						if(conversationId.equals("phase-values-times")){
							// Convert message
							// GGrr,yyrr|31,4
							String msgContent = msg.getContent();
							String msgValues = msgContent.split("#")[0];
							String msgTimes = msgContent.split("#")[1];
							nextPhaseValues = msgValues.split(",");
							nextPhaseTimes = new int[msgTimes.split(",").length];
							int i = 0;
							for(String s : msgTimes.split(",")){
								nextPhaseTimes[i] = Integer.parseInt(s);
								i++;
							}
							step++;
						}
					}
					break;					
			}
		}
		
		public boolean done(){
			return step == 2;
		}
	}
	
	private class RequestMessage extends CyclicBehaviour{
		public void action(){
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {

				String conversationId = msg.getConversationId();
				
				// Request change of priority from laneAgent
				
				if(conversationId.equals("lane-change-priority")){
					
					// Get stageId and priority to change
					
					String content = msg.getContent();
					String laneId = content.split(",")[0];
					int priority = Integer.parseInt(content.split(",")[1]);
					
					// Create list of stageAgents that are on top of the stage with priority change 
					
					ArrayList<String> laneNames = myself.getControlledLanes();
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
					
					String phasesOrder = phasesList.get(0).getPhaseId();
					for(int i = 1; i < phasesList.size(); i++){
						phasesOrder += "," + phasesList.get(i).getPhaseId();
					}
					
					for(PhaseAgent phase:phasesList){
						ACLMessage request = ACLMessageFactory.createRequestMsg(
								AIDManager.getPhaseAID(phase.getPhaseId(), myAgent), 
								priority + "#" + lanesAffected + "#" + phasesOrder,
								"lane-change-priority");
						myAgent.send(request);
					}
					
					
				}
				
				if(conversationId.equals("stage-up")){
					String content = msg.getContent();
					String stageId = content;
					
					int index = 0;
					int i = 0;
					PhaseAgent auxPhaseAgent = null;
					for(PhaseAgent phase:phasesList){
						if(phase.getPhaseId().equals(stageId)){
							auxPhaseAgent = phase;
							index = i;
							break;
						}
						i++;
					}
					
					// DEBUG
					System.out.print("OLD QUEUE - " + stageId + ":");
					for(PhaseAgent phase:phasesList){
						System.out.print(phase.getPhaseId() + " ");
					}
					System.out.println();
					// DEBUG
					
					if(index > 1){						
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						reply.setContent("");
						myAgent.send(reply);
						
						phasesList.remove(index);
						phasesList.add(index - 1, auxPhaseAgent);
						
						reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent("");
						myAgent.send(reply);
					}else{
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("");
						myAgent.send(reply);
					}
					
					// DEBUG
					System.out.print("NEW QUEUE - " + stageId + ":");
					for(PhaseAgent phase:phasesList){
						System.out.print(phase.getPhaseId() + " ");
					}
					System.out.println();
					// DEBUG
				}
				
			}
			block();
		}
	}

}
