package com.iteso.roma.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.iteso.roma.agents.RomaManagerAgent;

import jade.BootProfileImpl;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import start.ODManager;
import trasmapi.genAPI.Simulator;
import trasmapi.genAPI.TraSMAPI;
import trasmapi.genAPI.exceptions.TimeoutException;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;

public class Roma {
	
	static boolean JADE_GUI = true;
	private static ProfileImpl profile;
	private static ContainerController mainContainer;

	public static void main(String[] args) throws UnimplementedMethod, IOException, TimeoutException, InterruptedException {
		
		//Init JADE platform w/ or w/out GUI
		if(JADE_GUI){
			List<String> params = new ArrayList<String>();
			params.add("-gui");
			profile = new BootProfileImpl(params.toArray(new String[0]));
		} else
			profile = new ProfileImpl();

		Runtime rt = Runtime.instance();
		
		//mainContainer - agents' container
		mainContainer = rt.createMainContainer(profile);
		
		RomaManagerAgent romaManager = new RomaManagerAgent(1, mainContainer);
		try {			
			mainContainer.acceptNewAgent("_Roma_", romaManager).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			return;
		}
		
		//"\uD83D\uDCA9"
		TraSMAPI api = new TraSMAPI(); 

		//Create SUMO
		Simulator sumo = new Sumo("guisim");
		List<String> params = new ArrayList<String>();
		params.add("-c=romaSimulations\\data\\romaBasic.sumo.cfg");
		params.add("--device.emissions.probability=1.0");
        params.add("--tripinfo-output=romaSimulations\\data\\trip.xml");
		sumo.addParameters(params);
		sumo.addConnections("localhost", 8820);

		//Add Sumo to TraSMAPI
		api.addSimulator(sumo);
		
		//Launch and Connect all the simulators added
		api.launch();

		api.connect();

		api.start();
		
		Thread.sleep(1000);
		
		//instatiate agents
		romaManager.createAgents();

		while(true)
			if(!api.simulationStep(0))
				break;

	}

}
