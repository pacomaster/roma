package com.iteso.roma.agents;

import com.iteso.roma.agents.behaviours.PhaseInformMessageBehaviour;
import com.iteso.roma.jade.ServiceRegister;
import com.iteso.roma.sumo.Phase;

import jade.core.Agent;

/**
 * Class to represent a phase in SUMO.
 * <br/>
 * <br/>
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
 * SUMO considers every single element in as a phase but for the purpose of optimizing
 * times for cars to move, the phase will be consider Green and yellow times like {"Gr","yr"} for NS
 * @author Francisco Amezcua
 *
 */
@SuppressWarnings("serial")
public class PhaseAgent extends Agent{	
	private String phaseId;	
	private Phase phase;
	private String[] lanesAffected;
	private int[] lanesAffectedVeh;
	private int totalVeh;

	/**
	 * Constructor
	 * @param phaseId The name of the phase
	 * @param junctionId The name of the junction this phase is attached
	 * @param phaseTimes The phases duration
	 * @param phaseValues The phases values
	 */
	public PhaseAgent(String phaseId, String junctionId, Phase phase, String[] lanesAffected) {
		this.phaseId = phaseId;
		this.phase = phase;
		this.lanesAffected = lanesAffected;
		this.lanesAffectedVeh = new int[lanesAffected.length];
		this.totalVeh = 0;
	}
	
	/**
	 * This class setups the agent
	 */
	protected void setup(){		
		ServiceRegister.register(this, phaseId);
		addBehaviour(new PhaseInformMessageBehaviour(this));
		//addBehaviour(new PhaseRequestMessageBehaviour(this));		
		//addBehaviour(new PhaseCoordinationBehaviour(this));
	}
	
	public String getPhaseId(){
		return phaseId;
	}
	
	public Phase getPhase() {
		return phase;
	}
	
	public String[] getLanesAffected() {
		return lanesAffected;
	}

	public int[] getLanesAffectedVeh() {
		return lanesAffectedVeh;
	}
	
	public void setLanesAffectedVeh(int[] lanesAffectedVeh) {
		this.lanesAffectedVeh = lanesAffectedVeh;
	}

	public int getTotalVeh() {
		return totalVeh;
	}

	public void setTotalVeh(int totalVeh) {
		this.totalVeh = totalVeh;
	}
}
