package com.iteso.roma.agents;

import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.sumo.SumoCom;
import trasmapi.sumo.SumoVehicle;

/**
 * Class to generate automobile agents for simulation
 * @author Francisco Amezcua
 * 
 */
@SuppressWarnings("serial")
public class AutomobileGeneratorAgent extends Agent{
	
	private final int ID = 1000000;
	
	private int automobileGeneratorId;
	private int startStreetId;
	private int endStreetId;
	private int timeToStart;
	
	private int val = 0;
	private int inc = 1;
	private int automobileCounter=1;
	private ContainerController mainContainer;
	
	public AutomobileGeneratorAgent(ContainerController mainContainer) {
		this.mainContainer = mainContainer;
	}
	
	// ARGS
	// (automobileGeneratorId, streetId,  timeToStart)
	protected void setup(){
		Object[] args =  getArguments();
		if(args.length > 0){
			this.automobileGeneratorId = Integer.parseInt((String)args[0]);
			this.startStreetId = Integer.parseInt((String)args[1]);
			this.endStreetId = Integer.parseInt((String)args[2]);
			this.timeToStart = Integer.parseInt((String)args[3]);
		}
		

		
	}
}
