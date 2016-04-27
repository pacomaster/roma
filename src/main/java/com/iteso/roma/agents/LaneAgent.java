package com.iteso.roma.agents;

import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;
import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoLane;
import trasmapi.sumo.SumoTrafficLight;

public class LaneAgent extends Agent{
	
	String laneId;
	SumoLane myself;
	String junctionId;
	
	int currentPriority = 1;
	
	private int nextCycle = 10;
	
	public LaneAgent(String laneId, String junctionId) {
		this.laneId = laneId;
		this.myself = new SumoLane(laneId);
		this.junctionId = junctionId;
	}
	
	protected void setup(){
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(laneId);
		sd.setName(laneId);
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
					nextCycle += 10;
					
					int numVeh = myself.getNumVehicles();
					double laneLength = myself.getLength();
					double avgVehLength = 6.0;
					
					int priority = (int) (((avgVehLength * numVeh) / laneLength) * 5.0);
					if(currentPriority != priority){
						currentPriority = priority;
						if(currentPriority > 2){
							// Send message to Junction for priority
							ACLMessage request = ACLMessageFactory.createRequestMsg(
									AIDManager.getJunctionAID(junctionId, myAgent), 
									laneId + "," + currentPriority,
									"lane-change-priority");
							myAgent.send(request);
						}
					}
					
				}
			}
		});
	}

}
