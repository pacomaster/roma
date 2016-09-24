package com.iteso.roma.agents;

import java.util.ArrayList;

import com.iteso.roma.agents.behaviours.JunctionChangePhaseBehaviour;
import com.iteso.roma.agents.behaviours.JunctionRequestMessageBehaviour;
import com.iteso.roma.jade.ServiceRegister;
import com.iteso.roma.sumo.Phase;
import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import trasmapi.sumo.SumoTrafficLight;

/**
 * This represents an intersection with multiple edges converging in it.
 * Usually contains a traffic light to control
 * <br/>
 * <br/>
 * This class changes the phase presented by the traffic light
 * A phase is an array that represents the state of the junction each step of the phase
 * <br/>
 * <br/>
 * Example:
 * <br/>
 * A cross street [+] 
 * Two possible ways to move NS and EW
 * When cars move NS the phase will look like "Gr" (green NS and red EW)
 * Then needs to be a yellow light like this "yr"
 * Then cars move EW the phase changes to "rG" (red NS and green EW)
 * Finally yellow for EW which is "ry"
 * And cycle repeats
 * So for this we have two arrays phaseValues and phaseTimes
 * phaseValues will contain {"Gr","yr","rG","ry"}
 * phaseTimes will contain {15,4,15,4} the times the phase should be active
 * <br/>
 * <br/>
 * SUMO considers every single element in phaseValues as a phase but for the purpose of optimizing
 * times for cars to move the phase will be consider Green and yellow times like {"Gr","yr"} for NS
 * <br/>
 * <br/>
 * The ticker changes the phase according to the time in phaseTimes
 * If this is the last element in the array starts looking for the next phase (green and yellow)
 * After the last element in the array is changed the new phase takes its place.
 * @author Francisco Amezcua
 *
 */
@SuppressWarnings("serial")
public class JunctionAgent extends Agent{
	
	private String junctionId;
	private SumoTrafficLight myself;
	private Phase currentPhase;
	private Phase nextPhase;
	private int firstCycle;

	/**
	 * This list contains the next phase to put into the traffic light
	 * Remember phase consist of Green and yellow lights
	 */
	private ArrayList<PhaseAgent> phaseAgentsList = new ArrayList<PhaseAgent>();
	
	/**
	 * Constructor
	 * @param junctionId The name of the junction
	 * @param phaseTimes Array of int times for the current phase elements
	 * @param phaseValues Array of string configuration for the current phase
	 * @param phasesList ArrayList of PhaseAgents with reference and order of the phases to implement
	 */
	public JunctionAgent(String junctionId, ArrayList<PhaseAgent> phasesList, Phase firstPhase) {
		this.junctionId = junctionId;
		this.myself = new SumoTrafficLight(junctionId);
		this.currentPhase = firstPhase;
		this.nextPhase = currentPhase;
		this.phaseAgentsList = phasesList;
		firstCycle = this.currentPhase.getGreenTime();
	}
	
	/**
	 * This class setups the agent
	 */
	protected void setup(){		
		ServiceRegister.register(this, junctionId);		
		addBehaviour(new JunctionChangePhaseBehaviour(this, TimeManager.getSeconds(1), firstCycle));		
		addBehaviour(new JunctionRequestMessageBehaviour(this));
	}
	
	public String getJunctionId() {
		return junctionId;
	}

	public SumoTrafficLight getSumoTrafficLight() {
		return myself;
	}
	
	public ArrayList<PhaseAgent> getPhaseAgentsList() {
		return phaseAgentsList;
	}
	
	public Phase getCurrentPhase() {
		return currentPhase;
	}

	public Phase getNextPhase() {
		return nextPhase;
	}
	
	public void setNextPhase(Phase nextPhase) {
		this.nextPhase = nextPhase;
	}

	public void setCurrentPhase(Phase currentPhase) {
		this.currentPhase = currentPhase;
		
	}
}
