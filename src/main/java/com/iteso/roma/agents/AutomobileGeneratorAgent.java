package com.iteso.roma.agents;

import com.iteso.roma.utils.TimeManager;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

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
		

		// Behavior to wait for the generation of automobiles to start and then generate them
		addBehaviour(new TickerBehaviour(this, TimeManager.getSeconds(1)) {
			protected void onTick() {
				if(timeToStart >=0){
					if(timeToStart == 0){
						
						addBehaviour(new TickerBehaviour(myAgent, TimeManager.getSeconds(64)){
							protected void onTick() {
								val += inc;
								if(val > 32){
									inc = -1;
									val -= 2;
								}
								if(val == 0){
									inc = 1;
									val += 2;
								}
								
								int automobilesToCreate = val * 1;
								//System.out.println("NUM: " + automobilesToCreate);
								
								for(int i = 0; i < automobilesToCreate; i++){
									
									int id = (ID * automobileGeneratorId) + automobileCounter;
									Object[] objs = new Object[4];
									objs[0] = id;
									objs[1] = startStreetId;
									objs[2] = endStreetId;
									
									ContainerController cc = getContainerController();
									AgentController ac;
									try {
										ac = cc.createNewAgent("AutAge" + id, "com.iteso.roma.agents.AutomobileAgent", objs);
										ac.start();
									} catch (StaleProxyException e) {
										e.printStackTrace();
									}
									
									//System.out.println("Create AutAge" + id);
									automobileCounter++;
								}
							}
						});
						timeToStart--;
					}else{
						timeToStart--;
					}
				}
			}
		});	
	}
}
