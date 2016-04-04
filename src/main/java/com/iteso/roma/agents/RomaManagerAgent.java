package com.iteso.roma.agents;

import java.util.HashMap;
import java.util.Map;

import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoVehicle;

public class RomaManagerAgent extends Agent{
	
	private ContainerController mainContainer;
	private int romaManagerId;
	private Map<String, VehicleAgent> vehicleAgents =  new HashMap<String, VehicleAgent>(); 
	
	private int val = 0;
	private int inc = 1;
	private int automobileCounter=1;
	
	public RomaManagerAgent(int romaManagerId, ContainerController mainContainer) {
		this.romaManagerId = romaManagerId;
		this.mainContainer = mainContainer;		
	}
	
	public void createAgents(){
		
	}
	
	protected void setup() {
		
		// Behavior to wait for the generation of automobiles to start and then generate them
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(64)) {
			protected void onTick() {								

				val += inc;
				if(val > 32){
					inc = -1;
					val -= 2;
				}
				if(val == 0){
					inc = 1;
					val += 2;
				}
				
				int automobilesToCreate = val * 1;
				//System.out.println("NUM: " + automobilesToCreate);
				
				for(int i = 0; i < automobilesToCreate; i++){
					
					int id = automobileCounter;
					byte b = 0;
					
					SumoVehicle sumoVehicle = new SumoVehicle(id, "CarA", "route01", SumoCom.getCurrentSimStep(), 0, b);
					SumoCom.addVehicleToSimulation(sumoVehicle);
					
					SumoCom.
					
					String str = "veh" + id;											
					VehicleAgent vehicleAgent = new VehicleAgent(str);
					try {
						mainContainer.acceptNewAgent(str, vehicleAgent).start();
					} catch (StaleProxyException e) {
						e.printStackTrace();
					}
					
					//System.out.println("Create AutAge" + id);
					automobileCounter++;
				}
			}
		});	
		
	}
}
