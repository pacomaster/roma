package com.iteso.roma.agents;

import jade.core.Agent;
import trasmapi.sumo.SumoTrafficLight;

public class PhaseAgent extends Agent{
	
	private String phaseId;
	private String junctionId;
	
	private String[] phaseValue;
	private int[] phaseTime;
	
	public PhaseAgent(String phaseId, String junctionId, int[] phaseTime, String[] phaseValue) {
		this.phaseId = phaseId;
		this.junctionId = junctionId;
		this.phaseTime = phaseTime;
		this.phaseValue = phaseValue;
	}
	
}
