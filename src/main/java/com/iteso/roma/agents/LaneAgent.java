package com.iteso.roma.agents;

import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;
import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoLane;

public class LaneAgent extends Agent{
	
	String laneId;
	SumoLane myself;
	
	private int nextCycle = 10;
	
	public LaneAgent(String laneId) {
		this.laneId = laneId;
	}
	
	protected void setup(){
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){
					nextCycle += 10;
					
					int numVeh = myself.getNumVehicles();
					double laneLength = myself.getLength();
					double avgVehLength = 6.0;
					
					int level = (int) (((avgVehLength * numVeh) / laneLength) * 5.0);
					
					if(level > 2){
						// Send message to Juntion for priority
					}
					
				}
			}
		});
	}

}
