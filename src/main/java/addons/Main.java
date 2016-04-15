package addons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import trasmapi.genAPI.Simulator;
import trasmapi.genAPI.TraSMAPI;
import trasmapi.genAPI.exceptions.TimeoutException;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;

import jade.BootProfileImpl;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {
	
	static boolean JADE_GUI = true;
	private static ProfileImpl profile;
	private static ContainerController mainContainer;
	
	public static void main(String[] args) throws UnimplementedMethod, InterruptedException, IOException, TimeoutException {	

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
		
		//
		ODManager manager = new ODManager(mainContainer);
		
		try {
			
			mainContainer.acceptNewAgent("MANAGER#1", manager).start();
			
		} catch (StaleProxyException e) {
			e.printStackTrace();
			return;
		}
		//"\uD83D\uDCA9"
		TraSMAPI api = new TraSMAPI(); 

		//Create SUMO
		Simulator sumo = new Sumo("guisim");
		List<String> params = new ArrayList<String>();
		params.add("-c=Map\\map.sumo.cfg");
		sumo.addParameters(params);
		sumo.addConnections("localhost", 8820);

		//Add Sumo to TraSMAPI
		api.addSimulator(sumo);
		
		//Launch and Connect all the simulators added
		api.launch();

		api.connect();

		api.start();
		
		Thread.sleep(1000);

		//instatiate Driver agents
		manager.addDrivers();
		manager.addPolygon();

		//simulation loop
		while(true)
			if(!api.simulationStep(0))
				break;
	}	
}
