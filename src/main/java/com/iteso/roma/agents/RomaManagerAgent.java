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
		
		int[] pTimes1 = {31,4};
		String[] pValues1 = {"GGGrrrrrGGGrrrrr", "yyyrrrrryyyrrrrr"};	
		PhaseAgent phaseAgent1 = new PhaseAgent("pha1","jun5",pTimes1,pValues1);
		
		int[] pTimes2 = {6,4};
		String[] pValues2 = {"rrrGrrrrrrrGrrrr", "rrryrrrrrrryrrrr"};	
		PhaseAgent phaseAgent2 = new PhaseAgent("pha2","jun5",pTimes2,pValues2);
		
		int[] pTimes3 = {31,4};
		String[] pValues3 = {"rrrrGGGrrrrrGGGr", "rrrryyyrrrrryyyr"};	
		PhaseAgent phaseAgent3 = new PhaseAgent("pha3","jun5",pTimes3,pValues3);
		
		int[] pTimes4 = {6,4};
		String[] pValues4 = {"rrrrrrrGrrrrrrrG", "rrrrrrryrrrrrrry"};	
		PhaseAgent phaseAgent4 = new PhaseAgent("pha4","jun5",pTimes4,pValues4);
		
		ArrayList<PhaseAgent> phasesList = new ArrayList<PhaseAgent>();
		phasesList.add(phaseAgent1);
		phasesList.add(phaseAgent2);
		phasesList.add(phaseAgent3);
		phasesList.add(phaseAgent4);
		
		JunctionAgent junctonAgent5 = new JunctionAgent("J5", pTimes1, pValues1, phasesList, mainContainer);
		try {
			mainContainer.acceptNewAgent("pha1", phaseAgent1).start();
			mainContainer.acceptNewAgent("pha2", phaseAgent2).start();
			mainContainer.acceptNewAgent("pha3", phaseAgent3).start();
			mainContainer.acceptNewAgent("pha4", phaseAgent4).start();
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
