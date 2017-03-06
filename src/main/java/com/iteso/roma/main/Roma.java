package com.iteso.roma.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.iteso.roma.agents.RomaManagerAgent;

import jade.BootProfileImpl;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import trasmapi.genAPI.Simulator;
import trasmapi.genAPI.TraSMAPI;
import trasmapi.genAPI.exceptions.TimeoutException;
import trasmapi.genAPI.exceptions.UnimplementedMethod;
import trasmapi.sumo.Sumo;

/**
 * This class start all the necessary frameworks for the project
 * @author Francisco Amezcua
 *
 */
public class Roma {
	
	private static final Logger logger = Logger.getLogger(Roma.class.getName());
	
	static boolean JADE_GUI = true;
	private static ProfileImpl profile;
	private static ContainerController mainContainer;

	/**
	 * Main method
	 * 
	 * @param args This program doesn't require any arguments
	 * @throws UnimplementedMethod
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws UnimplementedMethod, IOException, TimeoutException, InterruptedException {
		
		// Set the log level for the project
		// This should be done using a file but at the moment this will need to do
		Logger log = LogManager.getLogManager().getLogger("");
		for (Handler h : log.getHandlers()) {
			// Set log format
			h.setFormatter(new RomaFormatter());
			/*
			Set log level:
		    SEVERE (highest)
		    WARNING
		    INFO
		    CONFIG
		    FINE
		    FINER
		    FINEST
			*/
		    h.setLevel(Level.INFO);
		}
		
		// ASCII art from: http://ascii.co.uk/art/roman
		logger.info("          ___");
		logger.info("          \\\\||");
		logger.info("         ,'_,-\\");     
		logger.info("         ;'____\\");    
		logger.info("         || =\\=|");    
		logger.info("         ||  - |");                               
		logger.info("     ,---'._--''-,,---------.--.----_,");
		logger.info("    / `-._- _--/,,|  ___,,--'--'._<");
		logger.info("   /-._,  `-.__;,,|'");
		logger.info("  /   ;\\      / , ;");                            
		logger.info(" /  ,' | _ - ',/, ;");
		logger.info("(  (   |     /, ,,;");
		logger.info(" \\  \\  |     ',,/,;");
		logger.info("  \\  \\ |    /, / ,;");
		logger.info(" (| ,^.|   / ,, ,/;");
		logger.info("  `-'./ `-._,, ,/,;");
		logger.info("       ´-._ `-._,,;");
		logger.info("       |/,,`-._ `-.");
		logger.info("       |, ,;, ,`-._\\");		
		logger.info("ROMA Rises...");
		
		//Init JADE platform with or without GUI
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
		
		// Start traSMAPI
		TraSMAPI api = new TraSMAPI(); 

		//Create SUMO
		Simulator sumo = new Sumo("guisim");
		List<String> params = new ArrayList<String>();
		params.add("-c=romaSimulations\\data\\romaBasic.sumo.cfg");
		params.add("--device.emissions.probability=1.0");
        params.add("--tripinfo-output=romaSimulations\\data\\trip_500.xml");
		sumo.addParameters(params);
		sumo.addConnections("localhost", 8820);

		//Add Sumo to TraSMAPI
		api.addSimulator(sumo);
		
		//Launch and Connect all the simulators added
		api.launch();

		api.connect();

		api.start();
		
		Thread.sleep(1000);

		while(true)
			if(!api.simulationStep(0))
				break;

	}
	
	/**
	 * Class to define the format for the log.
	 * @author Francisco Amezcua
	 * 
	 */
	static class RomaFormatter extends Formatter 
	{   
	    public RomaFormatter() { super(); }

	    @Override 
	    public String format(final LogRecord record) 
	    {
	        return record.getMessage() + "\n";
	    }   
	}

}
