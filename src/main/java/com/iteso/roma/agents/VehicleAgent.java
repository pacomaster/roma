package com.iteso.roma.agents;

import jade.core.Agent;

public class VehicleAgent extends Agent{
	
	private String vehicleId;
	
	public VehicleAgent(String vehicleId) {
		this.vehicleId = vehicleId;
	}
	
	protected void setup(){
		// System.out.println("Create: " + vehicleId);
	}

}
