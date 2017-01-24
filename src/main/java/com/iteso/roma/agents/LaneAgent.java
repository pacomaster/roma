package com.iteso.roma.agents;

import com.iteso.roma.agents.behaviours.LaneVehiclesCheckerBehaviour;
import com.iteso.roma.jade.ServiceRegister;
import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import trasmapi.sumo.SumoLane;

/**
 * Lane represents a lane in the street an street can be composed by multiple lanes.
 * @author Francisco Amezcua
 *
 */
@SuppressWarnings("serial")
public class LaneAgent extends Agent{
	
	SumoLane myself;
	String laneId;	
	String junctionId;	
	int currentPriority = 1;
	int numberVehicles = 0;
	public static int cycleTimer = 15;
	
	/**
	 * Constructor
	 * @param laneId The name of the lane
	 * @param junctionId Juntion name associated to this lane (Or parent Edge)
	 */
	public LaneAgent(String laneId, String junctionId) {
		this.laneId = laneId;
		this.myself = new SumoLane(laneId);
		this.junctionId = junctionId;
	}
	
	/**
	 * This class setups the agent
	 */
	protected void setup(){
		ServiceRegister.register(this, laneId);
		addBehaviour(new LaneVehiclesCheckerBehaviour(this, TimeManager.getSeconds(1), cycleTimer));
	}
	
	public SumoLane getSumoLane(){
		return myself;
	}
	
	public String getLaneId() {
		return laneId;
	}

	public String getJunctionId() {
		return junctionId;
	}
	
	public void setNumberVehicles(int numberVehicles){
		this.numberVehicles = numberVehicles;
	}
	
	public int getNumberVehicles(){
		return numberVehicles;
	}

}
