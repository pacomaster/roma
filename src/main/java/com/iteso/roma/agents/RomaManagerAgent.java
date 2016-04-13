package com.iteso.roma.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoTrafficLight;
import trasmapi.sumo.SumoVehicle;

public class RomaManagerAgent extends Agent{
	
	private final int EXTRA_TIME = 64 * 1000;
	
	int nextCycle = 31;
	
	private ContainerController mainContainer;
	private int romaManagerId;
	private Map<String, VehicleAgent> vehicleAgents =  new HashMap<String, VehicleAgent>(); 
	
	private ArrayList<SumoTrafficLight> trafficLightsList = new ArrayList<SumoTrafficLight>();
	
	private int val = 0;
	private int inc = 1;
	private int automobileCounter=1;
	
	public RomaManagerAgent(int romaManagerId, ContainerController mainContainer) {
		this.romaManagerId = romaManagerId;
		this.mainContainer = mainContainer;
		
		
	}

	public void createAgents() throws UnimplementedMethod{
		ArrayList<String> tlsIds = SumoTrafficLight.getIdList();
		System.out.println("TRAFFIC LIGHTS:");
        for (String tlId : tlsIds) {
            System.out.println(tlId);
            trafficLightsList.add(new SumoTrafficLight(tlId));
        }
        
	}
	
	protected void setup() {
		
		AutomobileGeneratorAgent automobileGeneratorAgent1 = new AutomobileGeneratorAgent(1,"route01", true, this.mainContainer);
		AutomobileGeneratorAgent automobileGeneratorAgent3 = new AutomobileGeneratorAgent(3,"route03", true, this.mainContainer);
		AutomobileGeneratorAgent automobileGeneratorAgent5 = new AutomobileGeneratorAgent(5,"route05", false, this.mainContainer);
		AutomobileGeneratorAgent automobileGeneratorAgent7 = new AutomobileGeneratorAgent(7,"route07", false, this.mainContainer);
		try {
			mainContainer.acceptNewAgent("autoGenAge1", automobileGeneratorAgent1).start();
			mainContainer.acceptNewAgent("autoGenAge3", automobileGeneratorAgent3).start();
			mainContainer.acceptNewAgent("autoGenAge5", automobileGeneratorAgent5).start();
			mainContainer.acceptNewAgent("autoGenAge7", automobileGeneratorAgent7).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){
					for(SumoTrafficLight tl: trafficLightsList){
						tl.changePhase();
						nextCycle += tl.pTime[tl.getPhaseId()];
						System.out.println(sumoTime + " PHASE: " + tl.getPhaseId() + " nextCycle: " + nextCycle);						
					}
				}			
				
			}
		});
		
	}
	
	
}
