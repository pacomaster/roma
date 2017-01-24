package com.iteso.roma.agents.behaviours;

import java.util.Iterator;
import java.util.logging.Logger;

import com.iteso.roma.agents.LaneAgent;
import com.iteso.roma.jade.ConversationIds;
import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import trasmapi.sumo.SumoCom;

@SuppressWarnings("serial")
public class LaneVehiclesCheckerBehaviour extends TickerBehaviour{
	
	private static final Logger _logger = Logger.getLogger(LaneVehiclesCheckerBehaviour.class.getName());
	
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
		
		// Every cycle in the simulation should check how many cars are in the lane and change the priority
		if(sumoTime > nextCycle){
			nextCycle += LaneAgent.cycleTimer;
			checkCarsInLane();
			//logLaneStatus();
		}
	}
	
	private void checkCarsInLane(){
		int numVeh = calculateLaneVehicles(laneAgent);
		if(numVeh != laneAgent.getNumberVehicles()){
			laneAgent.setNumberVehicles(numVeh);			
			sendNumberVehiclesInLane();
		}
	}

	private int calculateLaneVehicles(LaneAgent laneAgent) {
		
		int numVeh = laneAgent.getSumoLane().getNumVehicles();
		return numVeh;
	}
	
	private void sendNumberVehiclesInLane(){
		AID receiverJunction = AIDManager.getJunctionAID(laneAgent.getJunctionId(), laneAgent);
		String messageContent = laneAgent.getLaneId() + "," + laneAgent.getNumberVehicles();
		
		ACLMessage inform = ACLMessageFactory.createInformMsg(receiverJunction, messageContent, ConversationIds.LANE_CHANGE_NUM_VEH);
		laneAgent.send(inform);
		ACLMessageFactory.logMessage(inform);
	}
	
	private void logLaneStatus(){
		_logger.info(laneAgent.getLaneId() + " - " + laneAgent.getNumberVehicles());
	}

}
