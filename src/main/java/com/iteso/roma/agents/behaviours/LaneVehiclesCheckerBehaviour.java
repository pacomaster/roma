package com.iteso.roma.agents.behaviours;

import java.util.logging.Logger;

import com.iteso.roma.agents.LaneAgent;
import com.iteso.roma.jade.ConversationIds;
import com.iteso.roma.sumo.VehicleAttributes;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import trasmapi.sumo.SumoCom;

@SuppressWarnings("serial")
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
			nextCycle += LaneAgent.cycleTimer;
			checkCarsInLane();
		}
	}
	
	private void checkCarsInLane(){
		int numVeh = calculateLaneVehicles(laneAgent);
		laneAgent.setNumberVehicles(numVeh);			
		sendNumberVehiclesInLane();			
//		logger.fine(ConversationIds.LANE_CHANGE_NUM_VEH + ": " + laneAgent.getLaneId() + "," + laneAgent.getNumberVehicles());
//		System.out.println(ConversationIds.LANE_CHANGE_NUM_VEH + ": " + laneAgent.getLaneId() + "," + laneAgent.getNumberVehicles());
	}

	private int calculateLaneVehicles(LaneAgent laneAgent) {
		
		int numVeh = laneAgent.getSumoLane().getNumVehicles();
		return numVeh;
	}
	
	private void sendNumberVehiclesInLane(){
		AID receiverJunction = AIDManager.getJunctionAID(laneAgent.getJunctionId(), laneAgent);
		String messageContent = laneAgent.getLaneId() + "," + laneAgent.getNumberVehicles();
		
		ACLMessage request = ACLMessageFactory.createRequestMsg(receiverJunction, messageContent, ConversationIds.LANE_CHANGE_NUM_VEH);
		laneAgent.send(request);
	}

}
