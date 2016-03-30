package com.iteso.roma.agents;

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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;
import com.iteso.roma.utils.TimeManager;

public class IntersectionAgent extends Agent{
	
	private final int SECONDS_TO_REQUEST_NEXT_TIME = 4;
	private final int DEFAULT_CYCLE_SECONDS = 16;
	
	private final boolean IS_STATIC = false;
	
	private int intersectionId;
	private List<Integer> stagesIds;
	private List<Integer> streetIds;
	private int secondsLeft;
	private int nextTime;
	
	// ARGS
	// (intersectionId, no. stages, [stages Ids], ... , [], streets, [streets Ids], ... , [])
	protected void setup(){
		Object[] args =  getArguments();
		stagesIds = new ArrayList<Integer>();
		streetIds = new ArrayList<Integer>();
		
		if(args.length > 0){
			this.intersectionId = Integer.parseInt((String)args[0]);
			int stages = Integer.parseInt((String)args[1]);
			int j = 2;
			for(int i = 0; i < stages; i++){
				String a = (String)args[j];
				stagesIds.add(Integer.parseInt(a));
				j++;
			}
			int streets = Integer.parseInt((String)args[j]);
			j++;
			for(int i = 0; i < streets; i++){
				String a = (String)args[j];
				streetIds.add(Integer.parseInt(a));
				j++;
			}
		}else{
			this.intersectionId = 1;				
			stagesIds.add(1);
			stagesIds.add(2);
			stagesIds.add(3);
			stagesIds.add(4);			
			}		
		secondsLeft = DEFAULT_CYCLE_SECONDS;
		nextTime = DEFAULT_CYCLE_SECONDS;
		System.out.println("Stage\t1");
		
		// System.out.println("IntAge" + this.intersectionId + " created");
		
		// Register the intersection service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("intersection-" + this.intersectionId);
		sd.setName("intersection-" + this.intersectionId);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new AcceptStageChangePriority());
		addBehaviour(new RequestChangePriority());
		
		// Add a TickerBehaviour that stage time
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				secondsLeft--;
				// System.out.println("IntAge" + intersectionId  + " serving StgAge" + stagesIds.get(0).toString() + " " + secondsLeft + "sec.");
				if(secondsLeft == 0){
					int lastId = stagesIds.remove(0);				
					secondsLeft = nextTime;
					nextTime = DEFAULT_CYCLE_SECONDS;
					stagesIds.add(lastId);
					
					// System.out.println("stgAge\t" + lastId + " - " + secondsLeft + " sec."); // DEBUG TEST
					
					// INFORM streets					
					ArrayList<AID> receivers = new ArrayList<AID>();
					for(int id : streetIds){						
						AID str = AIDManager.getStreetAID(id, myAgent);
						if(str != null) receivers.add(str);
					}
										
					if(receivers.size() > 0){
						AID list[] = new AID[receivers.size()];
						list = receivers.toArray(list);
						ACLMessage request = ACLMessageFactory.createInformMsg(list, Integer.toString(lastId), "inform-stage");
						myAgent.send(request);
						
						// Print stage in progress DEBUG
						System.out.println("stgAge" + lastId + " - " + nextTime + " sec.");
						
						// System.out.println("IntAge" + intersectionId + " send-inform StrAge1");
					}
					
				}
				if(secondsLeft == SECONDS_TO_REQUEST_NEXT_TIME){
					myAgent.addBehaviour(new RequestStageTime());
				}
			}
		});		
	}
	
	private class AcceptStageChangePriority extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// System.out.println("IntAge" + intersectionId + " received-accept_proposal");
				// REQUEST Message received. Process it
				String conversationId = msg.getConversationId();
				// Validate change request
				if(conversationId.equals("change-priority")){
					// System.out.println("IntAge" + intersectionId + " change priority complete");
				}
			}
			block();
		}
	}
	
	private class RequestChangePriority extends CyclicBehaviour{
		@SuppressWarnings("unused")
		public void action(){
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {

				String conversationId = msg.getConversationId();
				
				// Request change of priority from streetAgent
				
				if(conversationId.equals("request-priority")){
					
					// Get stageId and priority to change
					
					String content = msg.getContent();
					int stageId = Integer.parseInt(content.split(",")[0]);
					int priority = Integer.parseInt(content.split(",")[1]);
					
					// System.out.println("intAge" + intersectionId + "received message: " + stageId +"," + priority); // DEBUG
					
					// Create list of stageAgents that are on top of the stage with priority change 
					
					String stages = "";
					int i = 1;
					int s = stagesIds.get(i);
					while(s != stageId){
						stages += s + ","; 
						i++;
						if(i == stagesIds.size()) break;
						s = stagesIds.get(i);
					}
					
					// DEBUG
//					System.out.println("QUEUE:");
//					for(Integer stage : stagesIds){
//						System.out.println(stage);
//					}					
					// DEBUG
					
					// If stageId is not at the top
					if(stages != ""){
						stages = stages.substring(0,stages.length() - 1); // Remove last comma
						// System.out.println("TOP: " + stages); // DEBUG
						ACLMessage request = ACLMessageFactory.createRequestMsg(AIDManager.getStageAID(stageId, myAgent), priority + "-" + stages, "change-priority");
						myAgent.send(request);
					}
				}
				if(conversationId.equals("stage-up")){
					String content = msg.getContent();
					int stageId = Integer.parseInt(content);
					int index = stagesIds.indexOf(stageId);
					
					// DEBUG
//					System.out.println("OLD QUEUE - " + stageId);
//					for(int i : stagesIds){
//						System.out.println(i);
//					}
					// DEBUG
					
					if(index > 1){						
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						reply.setContent("");
						myAgent.send(reply);
						
						stagesIds.remove(index);
						stagesIds.add(index - 1, stageId);
						
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
//					System.out.println("NEW QUEUE");
//					for(int i : stagesIds){
//						System.out.println(i);
//					}
					// DEBUG
				}
			}
			block();
		}
	}
	
	private class RequestStageTime extends Behaviour{
		private int step = 0;
		
		public void action() {
			switch(step){
				case 0:
					AID receiver = AIDManager.getStageAID(stagesIds.get(1) , myAgent);					
					if(receiver != null){
						// Send the request to change priority
						ACLMessage request = ACLMessageFactory.createRequestMsg(receiver, "Next Time", "stage-time");
						myAgent.send(request);
						// System.out.println("IntAge" + intersectionId + " send-request StgAge" + stagesIds.get(1));
						step++;
					}					
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					ACLMessage msg = myAgent.receive(mt);
					if (msg != null) {
						// System.out.println("IntAge" + intersectionId + " received-inform");
						// REQUEST Message received. Process it
						String conversationId = msg.getConversationId();
						// Validate change request
						if(conversationId.equals("stage-time")){
							nextTime = Integer.parseInt(msg.getContent());
							// System.out.println("IntAge" + intersectionId + " get next time complete");
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
}
