package com.iteso.roma.agents;

import java.util.logging.Logger;

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

/**
 * Lane represents a lane in the street an street can be composed by multiple lanes.
 * @author Francisco Amezcua
 *
 */
public class LaneAgent extends Agent{
	
	private static final Logger logger = Logger.getLogger(LaneAgent.class.getName());
	
	String laneId;
	SumoLane myself;
	String junctionId;
	
	int currentPriority = 1;
	
	private int nextCycle = 60;
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
		
		// Every 60 seconds in the simulation should check how many cars are in the lane and change the priority
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){
					nextCycle += 60;
					
					int numVeh = myself.getNumVehicles();
					double laneLength = myself.getLength();
					// TODO: Changes to dynamic values
					double avgVehLength = 6.0;
					double minGap = 2.5;
					
					int priority = (int) (((avgVehLength * numVeh + minGap * numVeh) / laneLength) * 5.0) + 1;
					//if(currentPriority != priority){
					if(true){
						currentPriority = priority;
						/*
						 * Sends message to Junction for priority
						 * 
						 * Type: REQUEST
						 * To: Affected junction
						 * Subject: lane-change-priority
						 * Message: [laneId],[priority]
						 */
						ACLMessage request = ACLMessageFactory.createRequestMsg(
								AIDManager.getJunctionAID(junctionId, myAgent), 
								laneId + "," + currentPriority,
								"lane-change-priority");
						myAgent.send(request);
						
						logger.fine("lane-change-priority: " + laneId + "," + currentPriority);
					}
					
				}
			}
		});
	}

}
