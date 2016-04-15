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
		/*
		ArrayList<String> tlsIds = SumoTrafficLight.getIdList();
		System.out.println("TRAFFIC LIGHTS:");
        for (String tlId : tlsIds) {
            System.out.println(tlId);
            trafficLightsList.add(new SumoTrafficLight(tlId));
        }
        */
        
	}
	
	protected void setup() {
		
		// Automobile Generator Agents		
		RouteAgent routeAgent1 = new RouteAgent("rou1", true, this.mainContainer);
		RouteAgent routeAgent3 = new RouteAgent("rou3", true, this.mainContainer);
		RouteAgent routeAgent5 = new RouteAgent("rou5", false, this.mainContainer);
		RouteAgent routeAgent7 = new RouteAgent("rou7", false, this.mainContainer);
		try {
			mainContainer.acceptNewAgent("rou1", routeAgent1).start();
			mainContainer.acceptNewAgent("rou3", routeAgent3).start();
			mainContainer.acceptNewAgent("rou5", routeAgent5).start();
			mainContainer.acceptNewAgent("rou7", routeAgent7).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		// Traffic Light Agents		
		int[] pTimes = {4,31,6,4,31,4,6,4};		
		String[] pValues = {
				"GGGgrrrrGGGgrrrr",
				"yyygrrrryyygrrrr",
				"rrrGrrrrrrrGrrrr",
				"rrryrrrrrrryrrrr",
				"rrrrGGGgrrrrGGGg",
				"rrrryyygrrrryyyg",
				"rrrrrrrGrrrrrrrG",
				"rrrrrrryrrrrrrry"};
		
		JunctionAgent junctonAgent5 = new JunctionAgent("J5", pTimes, pValues, mainContainer);
		try {
			mainContainer.acceptNewAgent("jun5", junctonAgent5).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){
					
				}
			}
		});
		
	}
	
	
}
