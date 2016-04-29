package com.iteso.roma.agents;

import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoVehicle;

public class VehicleAgent extends Agent{
	
	private final int DELETE_TIME = 200;
	
	private String vehicleId;
	private SumoVehicle myself;
	private VehicleAgent vehicle;
	private int nextCycle;
	
	public VehicleAgent(String vehicleId) {
		this.vehicleId = vehicleId;
		this.myself = new SumoVehicle(vehicleId);
		vehicle = this;
		
		int sumoTimeFull = SumoCom.getCurrentSimStep();
		int sumoTime = sumoTimeFull / 1000;
		
		nextCycle = sumoTime + DELETE_TIME;
	}
	
	protected void setup(){
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){
					vehicle.doDelete();
				}
			}
		});
	}

}
