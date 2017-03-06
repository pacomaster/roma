package com.iteso.roma.agents;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.iteso.roma.utils.ACLMessageFactory;
import com.iteso.roma.utils.AIDManager;
import com.iteso.roma.utils.TimeManager;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import trasmapi.sumo.SumoCom;
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
	
	private int nextCycle;
	private int nextCarsCount = 100;
	
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
		nextCycle = phaseTimes[phaseStep];
	}
	
	/**
	 * This class setups the agent
	 */
	protected void setup(){
		// Register the junction service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(junctionId);
		sd.setName(junctionId);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}		
		
		// This ticker changes the current configuration of the traffic light after completes its time
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCycle){					
					changePhase(myAgent);					
					logger.info(sumoTime + " " + junctionId + "-" + phasesList.get(0).getPhaseId() + " P: " + phaseStep + " nextCycle: " + phaseTimes[phaseStep]);
				}
			}
		});
		
		// Create cars graphic
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				int sumoTimeFull = SumoCom.getCurrentSimStep();
				int sumoTime = sumoTimeFull / 1000;
				
				if(sumoTime > nextCarsCount){					
					String name = "cars_500";
					File f = new File("romaSimulations/data/" + name + ".csv");
					boolean isNew = false;
					if(!f.exists()) isNew = true;
					try(FileWriter fw = new FileWriter(f, true);
						    BufferedWriter bw = new BufferedWriter(fw);
						    PrintWriter out = new PrintWriter(bw)){
						
							List<String> vehIds = SumoCom.getAllVehiclesIds();
							int count = vehIds.size();
							
							if(isNew) out.println("time,cars");
						
						    out.println(nextCarsCount + "," + count);
						} catch (IOException e) {
						    //exception handling left as an exercise for the reader
						}
					nextCarsCount +=100;
				}
			}
		});
		
		addBehaviour(new RequestMessage());
	}
	
	/**
	 * Changes to the next element in the array in the current phase.
	 * @param myAgent Reference to the current agent
	 */
	public void changePhase(Agent myAgent){
		phaseStep++;
		// Check for last phase
		if(phaseStep == phaseTimes.length - 1){
			// Request next phase
			myAgent.addBehaviour(new RequestPhaseTime());
		}
		// If this is the last element change to the next phase
		if(phaseStep == phaseTimes.length){
			phaseStep = 0;
			phaseValues = nextPhaseValues;
			phaseTimes = nextPhaseTimes;
			phasesList.add(phasesList.get(0));
			phasesList.remove(0);
		}
		// Changes the phase in SUMO
		myself.setState(phaseValues[phaseStep]);
		nextCycle += phaseTimes[phaseStep];
	}
	
	/**
	 * Behaviour to get the next phaseValues and phaseTime when required.
	 * @author Francisco Amezcua
	 *
	 */
	private class RequestPhaseTime extends Behaviour{
		private int step = 0;
		
		/**
		 * Steps:
		 * <br/>
		 * 0 - Sends messages requesting phase values and times to next phase in queue.
		 * <br/>
		 * 1 - Receives response with phase values and times
		 */
		public void action() {
			switch(step){
				case 0:					
					AID receiver = AIDManager.getPhaseAID(phasesList.get(1).getPhaseId() , myAgent);					
					if(receiver != null){
						/*
						 * Sends the request for next phase values and times
						 * 
						 * Type: REQUEST
						 * To: Next phase in queue
						 * Subject: phase-values-times
						 * Message: Next Phase
						 */
						ACLMessage request = ACLMessageFactory.createRequestMsg(receiver, "Next Phase", "phase-values-times");
						myAgent.send(request);
						step++;
					}					
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					ACLMessage msg = myAgent.receive(mt);
					if (msg != null) {
						String conversationId = msg.getConversationId();
						if(conversationId.equals("phase-values-times")){
							// Convert message Format: GGrr,yyrr#31,4
							String msgContent = msg.getContent();
							String msgValues = msgContent.split("#")[0];
							String msgTimes = msgContent.split("#")[1];
							nextPhaseValues = msgValues.split(",");
							nextPhaseTimes = new int[msgTimes.split(",").length];
							int i = 0;
							for(String s : msgTimes.split(",")){
								nextPhaseTimes[i] = Integer.parseInt(s);
								i++;
							}
							step++;
						}
					}
					break;					
			}
		}
		
		public boolean done(){
			return step == 2;
		}
	}
	
	/**
	 * Behaviour to check every REQUEST message
	 * @author Francisco Amezcua
	 *
	 */
	private class RequestMessage extends CyclicBehaviour{
		/**
		 * Check for request messages
		 */
		public void action(){
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String conversationId = msg.getConversationId();
				
				// Request change of priority from LaneAgent				
				if(conversationId.equals("lane-change-priority")){
					
					// Convert message Format: laneId,priority	
					String content = msg.getContent();
					String laneId = content.split(",")[0];
					int priority = Integer.parseInt(content.split(",")[1]);
					
					// Create list with the lanes in junction affected by the priority change
					ArrayList<String> laneNames = myself.getControlledLanes();
					String lanesAffected = "";
					boolean first = true;
					for(String laneName :laneNames){
						if(laneName.equals(laneId)){
							if(first){
								first = false;
								lanesAffected += 1;
							}else{
								lanesAffected += "," + 1;
							}							
						}else{
							if(first){
								first = false;
								lanesAffected += 0;
							}else{
								lanesAffected += "," + 0;
							}	
						}
					}
					
					// Create list of PhaseAgents in order.
					String phasesOrder = phasesList.get(0).getPhaseId();
					for(int i = 1; i < phasesList.size(); i++){
						phasesOrder += "," + phasesList.get(i).getPhaseId();
					}
					
					/*
					 * Sends the request that a lane changed priority
					 * 
					 * Type: REQUEST
					 * To: Every phase
					 * Subject: lane-change-priority
					 * Message: [priority]#[lanesAffectedArray]#[phasesOrderArray]
					 */
					for(PhaseAgent phase:phasesList){						
						ACLMessage request = ACLMessageFactory.createRequestMsg(
								AIDManager.getPhaseAID(phase.getPhaseId(), myAgent), 
								priority + "#" + lanesAffected + "#" + phasesOrder,
								"lane-change-priority");
						myAgent.send(request);
						
						// logger.info("lane-change-priority: " + phase.getPhaseId() + " Msg:" + priority + "#" + lanesAffected + "#" + phasesOrder);
					}
				}
				
				// Request from a PhaseAgent to get up in the order
				if(conversationId.equals("stage-up")){
					String content = msg.getContent();
					// Convert message Format: phaseId
					String stageId = content;
					
					int index = 0;
					int i = 0;
					PhaseAgent auxPhaseAgent = null;
					
					// Get the position of the PhaseAgent in the list
					for(PhaseAgent phase:phasesList){
						if(phase.getPhaseId().equals(stageId)){
							auxPhaseAgent = phase;
							index = i;
							break;
						}
						i++;
					}
					
					// DEBUG See the content of the queue before change
					logger.info("");
					logger.info("OLD QUEUE - " + stageId + ":");
					for(PhaseAgent phase:phasesList){
						logger.info(phase.getPhaseId() + " ");
					}
					logger.info("");
					
					if(index > 1){						
						/*
						 * Sends agree message if PhaseAgent is not already at the top
						 * 
						 * Type: AGREE
						 * To: Phase that requested to go up one position
						 * Subject: stage-up
						 * Message: 
						 */
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						reply.setContent("");
						myAgent.send(reply);
						
						// Move PhaseAgent up one position in the list
						phasesList.remove(index);
						phasesList.add(index - 1, auxPhaseAgent);
						
						/*
						 * Sends inform message if PhaseAgent notifying change was done.
						 * 
						 * Type: INFORM
						 * To: Phase that requested to go up one position
						 * Subject: stage-up
						 * Message: 
						 */
						reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent("");
						myAgent.send(reply);
					}else{
						/*
						 * Sends refuse message if PhaseAgent already at the top
						 * 
						 * Type: REFUSE
						 * To: Phase that requested to go up one position
						 * Subject: stage-up
						 * Message: 
						 */
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("");
						myAgent.send(reply);
					}
					
					// DEBUG Check the content of the queue after change
					logger.info("");
					logger.info("NEW QUEUE - " + stageId + ":");
					for(PhaseAgent phase:phasesList){
						logger.info(phase.getPhaseId() + " ");
					}
					logger.info("");
				}
				
			}
			block();
		}
	}

}
