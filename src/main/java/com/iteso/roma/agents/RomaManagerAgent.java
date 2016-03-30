package com.iteso.roma.agents;

import java.util.HashMap;
import java.util.Map;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.sumo.SumoCom;

public class RomaManagerAgent extends Agent{
	
	private ContainerController mainContainer;
	private int romaManagerId;
	private Map<String, VehicleAgent> vehicleAgents =  new HashMap<String, VehicleAgent>(); 
	
	public RomaManagerAgent(int romaManagerId, ContainerController mainContainer) {
		this.romaManagerId = romaManagerId;
		this.mainContainer = mainContainer;		
	}
	
	public void createAgents(){
		
	}
	
	protected void setup() {
		
		addBehaviour(new TickerBehaviour(this, 1000) {
			protected void onTick() {
				for(String str : SumoCom.getAllVehiclesIds()){
					if(vehicleAgents.get(str) == null){
						VehicleAgent vehicle = new VehicleAgent(str);
						try {
							mainContainer.acceptNewAgent(str, vehicle).start();
						} catch (StaleProxyException e) {
							e.printStackTrace();
						}
						vehicleAgents.put(str, vehicle);
					}
				}
				System.out.println("Vehicles created so far: " + vehicleAgents.size());
			}
		});
		
	}
}
