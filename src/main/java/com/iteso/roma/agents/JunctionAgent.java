package com.iteso.roma.agents;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.iteso.roma.agents.behaviours.JunctionChangePhaseBehaviour;
import com.iteso.roma.agents.behaviours.JunctionRequestMessageBehaviour;
import com.iteso.roma.jade.ServiceRegister;
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
public class JunctionAgent extends Agent{
	
	private static final Logger logger = Logger.getLogger(JunctionAgent.class.getName());
	
	private String junctionId;
	private SumoTrafficLight myself;
	
	private String[] phaseValues;
	private int[] phaseTimes;
	private String[] nextPhaseValues;
	private int[] nextPhaseTimes;
	private int phaseStep = 0;
	private int firstCycle;
	
	/**
	 * This list contains the next phase to put into the traffic light
	 * Remember phase consist of Green and yellow lights
	 */
	private ArrayList<PhaseAgent> phasesList = new ArrayList<PhaseAgent>();
	
	/**
	 * Constructor
	 * @param junctionId The name of the junction
	 * @param phaseTimes Array of int times for the current phase elements
	 * @param phaseValues Array of string configuration for the current phase
	 * @param phasesList ArrayList of PhaseAgents with reference and order of the phases to implement
	 */
	public JunctionAgent(String junctionId, int[] phaseTimes, String[] phaseValues, ArrayList<PhaseAgent> phasesList) {
		this.junctionId = junctionId;
		this.myself = new SumoTrafficLight(junctionId);
		this.phaseTimes = phaseTimes;
		this.phaseValues = phaseValues;
		this.phasesList = phasesList;
		firstCycle = phaseTimes[phaseStep];
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
	
	public void setNextPhaseValues(String[] nextPhaseValues) {
		this.nextPhaseValues = nextPhaseValues;
	}

	public String[] getNextPhaseValues() {
		return nextPhaseValues;
	}
	
	public void setNextPhaseTimes(int[] nextPhaseTimes) {
		this.nextPhaseTimes = nextPhaseTimes;
	}

	public int[] getNextPhaseTimes() {
		return nextPhaseTimes;
	}
	
	public void setPhaseStep(int phaseStep) {
		this.phaseStep = phaseStep;
	}

	public int getPhaseStep() {
		return phaseStep;
	}
	
	public ArrayList<PhaseAgent> getPhasesList() {
		return phasesList;
	}
}
