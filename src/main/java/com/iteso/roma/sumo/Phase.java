package com.iteso.roma.sumo;

public class Phase {
	
	TrafficLightState[] states;
	private int phaseStep = 0;
	private int phasePriority = 1;
	
	public Phase(TrafficLightState[] cycles){
		this.states = cycles;
	}
	
	public Phase(String[] states, int[] times) {
		this.states = new TrafficLightState[times.length];
		for(int i = 0; i < times.length; i++){
			this.states[i] = new TrafficLightState(states[i], times[i]);
		}
	}
	
	public int nextState(){
		phaseStep++;
		if(phaseStep == states.length) phaseStep = 0;
		return phaseStep;
	}
	
	public String getCurrentState(){
		return states[phaseStep].getState();
	}
	
	public int getCurrentTime(){
		return states[phaseStep].getTime();
	}

	public int getGreenTime(){
		return states[0].getTime();
	}
	
	public void setGreenTime(int time){
		states[0].setTime(time);
	}
	
	public String getGreenState(){
		return states[0].getState();
	}
	
	public int getStatesLength(){
		return states.length;
	}
	
	public int getPhasePriority() {
		return phasePriority;
	}

	public void setPhasePriority(int phasePriority) {
		this.phasePriority = phasePriority;
	}
	
	public String[] getStates(){
		String[] states = new String[this.states.length];
		for(int i = 0; i < this.states.length; i++){
			states[i] = this.states[i].getState();
		}
		return states;
	}
	
	public int[] getTimes(){
		int[] times = new int[this.states.length];
		for(int i = 0; i < this.states.length; i++){
			times[i] = this.states[i].getTime();
		}
		return times;
	}

}
