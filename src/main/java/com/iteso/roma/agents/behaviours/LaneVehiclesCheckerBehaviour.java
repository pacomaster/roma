package com.iteso.roma.agents.behaviours;

import java.util.logging.Logger;

import com.iteso.roma.agents.LaneAgent;
import com.iteso.roma.sumo.VehicleAttributes;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import trasmapi.sumo.SumoCom;

public class LaneVehiclesCheckerBehaviour extends TickerBehaviour{
	
	private static final Logger logger = Logger.getLogger(LaneVehiclesCheckerBehaviour.class.getName());
	
	private LaneAgent laneAgent;
	private int nextCycle;

	public LaneVehiclesCheckerBehaviour(Agent agent, long period, int firstCycle) {
		super(agent, period);
		this.laneAgent = (LaneAgent)agent;
		this.nextCycle = firstCycle;
	}

	@Override
	protected void onTick() {
		int sumoTimeFull = SumoCom.getCurrentSimStep();
		int sumoTime = sumoTimeFull / 1000;
		
		// Every 60 seconds in the simulation should check how many cars are in the lane and change the priority
		if(sumoTime > nextCycle){
			nextCycle += 60;			
			checkCarsInLane();
		}
	}
	
	private void checkCarsInLane(){
		VehicleAttributes vehAttributes = new VehicleAttributes();
		int priority = calculateLanePriority(laneAgent, vehAttributes);
		
		if(laneAgent.getCurrentPriority() != priority){
			laneAgent.setCurrentPriority(priority);			
			sendChangePriorityMessage();			
			logger.fine("lane-change-priority: " + laneAgent.getLaneId() + "," + laneAgent.getCurrentPriority());
		}
	}

	private int calculateLanePriority(LaneAgent laneAgent, VehicleAttributes vehicleAttributes) {
		
		int numVeh = laneAgent.getSumoLane().getNumVehicles();
		double laneLength = laneAgent.getSumoLane().getLength();
		double avgVehLength = vehicleAttributes.getAvgVehLength();
		double minGap = vehicleAttributes.getMinGap();
		
		return (int) (((avgVehLength * numVeh + minGap * numVeh) / laneLength) * 5.0) + 1;
	}
	
	private void sendChangePriorityMessage(){
		
		/*
		 * Sends message to Junction for priority
		 * 
		 * Type: REQUEST
		 * To: Affected junction
		 * Subject: lane-change-priority
		 * Message: [laneId],[priority]
		 */
		
		AID receiverJunction = AIDManager.getJunctionAID(laneAgent.getJunctionId(), myAgent);
		String messageContent = laneAgent.getLaneId() + "," + laneAgent.getCurrentPriority();
		String conversationId = "lane-change-priority";
		
		ACLMessage request = ACLMessageFactory.createRequestMsg(receiverJunction, messageContent, conversationId);
		myAgent.send(request);
	}

}
