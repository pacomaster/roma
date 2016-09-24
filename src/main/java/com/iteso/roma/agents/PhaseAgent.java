package com.iteso.roma.agents;

import com.iteso.roma.agents.behaviours.PhaseCoordinationBehaviour;
import com.iteso.roma.agents.behaviours.PhaseRequestMessageBehaviour;
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
	
	public final int MIDDLE;
	public final int MAX;
	public final int UNIT;
	
	private String phaseId;	
	private Phase phase;

	private int[] lanesPriorities;
	
	/**
	 * Constructor
	 * @param phaseId The name of the phase
	 * @param junctionId The name of the junction this phase is attached
	 * @param phaseTimes The phases duration
	 * @param phaseValues The phases values
	 */
	public PhaseAgent(String phaseId, String junctionId, Phase phase) {
		this.phaseId = phaseId;
		this.phase = phase;
		
		// A unit is the way  the phase knows how many seconds needs to deal or offer using dealTable and offerTable
		MIDDLE = phase.getGreenTime();
		UNIT = MIDDLE / 5;
		MAX = MIDDLE + (UNIT*2);
		
		// Check  how many lanes are set in green for this phase		
		lanesPriorities = new int[phase.getStatesLength()];
		
		/* Create an array where 1 is a green space in the phase
		 * Example:
		 * 
		 * PHASE VALUE: GGGrrrrrGGGrrrrr
		 * LANES PRIORITIES: [1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0]
		 * 
		 * PHASE VALUE: rrrGrrrrrrrGrrrr
		 * LANES PRIORITIES: [0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0]
		 */		
		for(int i = 0; i < lanesPriorities.length; i++) {
		    if(phase.getGreenState().charAt(i) == 'G') {
		        lanesPriorities[i] = 1;
		    }else{
		    	lanesPriorities[i] = 0;
		    }
		}
		phase.setPhasePriority(calculatePhasePriority());		
	}
	
	/**
	 * Function to calculate all the lane priorities average
	 * @return the priority average
	 */
	private int calculatePhasePriority(){
		
		int max = 0;
		for(int p: lanesPriorities){
			if(p > max) max = p;
		}
		return max;
	}
	
	/**
	 * This class setups the agent
	 */
	protected void setup(){		
		ServiceRegister.register(this, phaseId);		
		addBehaviour(new PhaseRequestMessageBehaviour(this));
		addBehaviour(new PhaseCoordinationBehaviour(this));
	}
	
	public String getPhaseId(){
		return phaseId;
	}
	
	public Phase getPhase() {
		return phase;
	}	
}
