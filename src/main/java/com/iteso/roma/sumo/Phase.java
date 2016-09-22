package com.iteso.roma.sumo;

public class Phase {
	
	TrafficLightState[] states;
	private String[] phaseValues;
	private int[] phaseTimes;
	private int phaseStep = 0;
	private int phasePriority = 1;
	
	public Phase(TrafficLightState[] cycles){
		this.states = cycles;
	}
	
	public int nextState(){
		phaseStep++;
		if(phaseStep == phaseValues.length) phaseStep = 0;
		return phaseStep;
	}
	
	public String getCurrentState(){
		return phaseValues[phaseStep];
	}
	
	public int getCurrentTime(){
		return phaseTimes[phaseStep];
	}

	public Phase(int[] phaseTimes, String[] phaseValues) {
		this.phaseTimes = phaseTimes;
		this.phaseValues = phaseValues;
	}
	
	public String[] getPhaseValues() {
		return phaseValues;
	}
	
	public void setPhaseValues(String[] phaseValues) {
		this.phaseValues = phaseValues;
	}
	
	public void setPhaseTimes(int[] phaseTimes) {
		this.phaseTimes = phaseTimes;
	}

	public int[] getPhaseTimes() {
		return phaseTimes;
	}
	
	public int getGreenTime() {
		return phaseTimes[0];
	}
	
	public void setPhaseStep(int phaseStep) {
		this.phaseStep = phaseStep;
	}

	public int getPhaseStep() {
		return phaseStep;
	}
	
	public int getPhasePriority() {
		return phasePriority;
	}

	public void setPhasePriority(int phasePriority) {
		this.phasePriority = phasePriority;
	}

}
