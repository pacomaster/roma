package com.iteso.roma.agents;

import java.util.ArrayList;
import java.util.List;

import com.iteso.roma.components.StreetNode;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class StreetAgent extends Agent{
	
	private final int CAR_SPACE = 100;
	private final int CAR_LENGTH = 500;
	private final int LANE_LENGTH = 70000;	
	private final int MPS = 11; 

	private int streetId;
	private int intersectionId;
	private int lanes;
	private ArrayList<ArrayList<Integer>> lanesQueue;
	private ArrayList<Integer> nextStreetId;
	private ArrayList<Integer> maxLaneCapacity;
	private ArrayList<Integer> stageIds;
	private ArrayList<Integer> laneLength;
	private ArrayList<Integer> lanePriority; //Start at 1
	private int AutomobilesNextCycle = 5;
	
	private ArrayList<StreetNode> in;
	private ArrayList<StreetNode> out;
	
	// ARGS
	// (streetId, intersectionId, no. lanes, nextStreetIds ..., maxLaneCapacity ..., stageIds ... )
	protected void setup(){
		Object[] args =  getArguments();
		lanesQueue =  new ArrayList<ArrayList<Integer>>();
		nextStreetId =  new ArrayList<Integer>();
		maxLaneCapacity =  new ArrayList<Integer>();
		laneLength =  new ArrayList<Integer>();
		stageIds =  new ArrayList<Integer>();
		lanePriority =  new ArrayList<Integer>();
		if(args.length > 0){
			this.streetId = Integer.parseInt((String)args[0]);
			this.intersectionId = Integer.parseInt((String)args[1]);
			this.lanes = Integer.parseInt((String)args[2]);
			for (int i = 0; i < this.lanes; i++) lanesQueue.add(new ArrayList<Integer>());
			int j = 2;
			for (int i = 0; i < this.lanes; i++){
				j++;
				nextStreetId.add(Integer.parseInt((String)args[j]));
			}
			for (int i = 0; i < this.lanes; i++){
				j++;
				maxLaneCapacity.add(Integer.parseInt((String)args[j]));
			}
			for (int i = 0; i < this.lanes; i++){
				j++;
				stageIds.add(Integer.parseInt((String)args[j]));
				lanePriority.add(1);
				laneLength.add(LANE_LENGTH);
			}
		}
		
		// System.out.println("StrAge" + this.streetId + " created");
		
		
		// Register the street service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("street-" + this.streetId);
		sd.setName("street-" + this.streetId);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		addBehaviour(new RequestLane());
		addBehaviour(new InformChange());
	}
	
	private class InformChange extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				// INFORM Message received. Process it
				
				String conversationId = msg.getConversationId();				
				if(conversationId.equals("inform-stage")){
					int stageId = Integer.parseInt(msg.getContent());
					int lane = stageIds.indexOf(stageId);
					
					// Checks capacity for all lanes
					
					for(int index = 0; index < lanes; index++){
						
						// Check capacity of lane and then set priority
						
						/*
						if(stageId == 2){
							System.out.println("stgAge" + stageId);
							System.out.println("laneQueue: " + lanesQueue.get(index).size());
							System.out.println("maxLaneCapacity: " + maxLaneCapacity.get(index));
							System.out.println("unidad: " + maxLaneCapacity.get(index) / 5);
						}
						*/
						int nextPriority = (lanesQueue.get(index).size() / ( maxLaneCapacity.get(index) / 5 )) + 1;
						/*
						System.out.println("StrAge" + streetId + " MAX:" + maxLaneCapacity.get(index));
						System.out.println("StrAge" + streetId + " IN:" + lanesQueue.get(index).size());
						System.out.println("StrAge" + streetId + " P:" + nextPriority);
						*/
						if(nextPriority > 5) nextPriority = 5;
						
						//if(stageId == 2) nextPriority = 5; // DEBUG
						
						/**
						 * AL PONER ESTA LINEA GENERA QUE stgAge1 se salga del rango seguro stage 2 propone subir a stage 1 pero algo sale mal
						 */
						
						
						if(nextPriority != lanePriority.get(index)){
							
							// Send priority change for particular stage
							
							lanePriority.set(index, nextPriority);
							ACLMessage request = ACLMessageFactory.createRequestMsg(AIDManager.getIntersectionAID(intersectionId, myAgent), Integer.toString(stageIds.get(index)) + "," + Integer.toString(lanePriority.get(index)), "request-priority");
							myAgent.send(request);
							
							// System.out.println("strAge" + streetId + " change priority with message: " + Integer.toString(stageIds.get(index)) + "," + Integer.toString(lanePriority.get(index))); // DEBUG
						}
					}					
					
					// One of the stages for this street was activaded
					
					if(lane != -1){						
						ArrayList<AID> receivers = new ArrayList<AID>();
						int i = 0;			
						while(lanesQueue.get(lane).size() > 0 && i < AutomobilesNextCycle){
							receivers.add(AIDManager.getAutomobileAID(lanesQueue.get(lane).get(0) , myAgent));
							lanesQueue.get(lane).remove(0);
							i++;
						}
											
						if(receivers.size() > 0){
							AID list[] = new AID[receivers.size()];
							list = receivers.toArray(list);
							ACLMessage request = ACLMessageFactory.createInformMsg(list, "", "inform-cross");
							myAgent.send(request);
							// System.out.println("IntAge" + intersectionId + " send-inform StrAge1");
						}
					}
					
				}
			}
			block();
		}
	}
	
	private class RequestLane extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// REQUEST Message received. Process it
				String conversationId = msg.getConversationId();
				// Validate change request
				if(conversationId.equals("request-lane")){
					String content = msg.getContent();
					int automobileId = Integer.parseInt(content.split("-")[0]);					
					int nextId = Integer.parseInt(content.split("-")[1]);
					int index = nextStreetId.indexOf(nextId);
					(lanesQueue.get(index)).add(automobileId);
					laneLength.set(index, (lanesQueue.get(index).size() * CAR_LENGTH) + (lanesQueue.get(index).size() * CAR_SPACE));
					
					// Calculate capacity
					AutomobilesNextCycle = 5;
					
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(Integer.toString(index) + "-" + Integer.toString(laneLength.get(index)) + "-" + Integer.toString(MPS));
					myAgent.send(reply);
				}
			}
			block();
		}
	}	
}
