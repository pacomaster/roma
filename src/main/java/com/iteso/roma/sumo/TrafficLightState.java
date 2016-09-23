package com.iteso.roma.sumo;

public class TrafficLightState {
	
	private String state;
	private int time;
	
	public TrafficLightState(String state, int time) {
		this.state = state;
		this.time = time;
	}
	
	public String getState() {
		return state;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	
}
