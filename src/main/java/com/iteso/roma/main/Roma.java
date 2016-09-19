package com.iteso.roma.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.iteso.roma.agents.RomaManagerAgent;
import com.iteso.roma.utils.TimeManager;

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
	
	private static final Logger LOGGER = Logger.getLogger(Roma.class.getName());
	
	static boolean JADE_GUI = true;
	private static ProfileImpl _profile;
	private static ContainerController _mainContainer;

	public static void main(String[] args) throws 
			UnimplementedMethod, 
			IOException, 
			TimeoutException, 
			InterruptedException, 
			StaleProxyException, 
			SAXException, 
			ParserConfigurationException {
		
		setLoggerForRoma();
		printLogHeader();
		
		TimeManager.initialize(new File("romaSimulations/data/romaBasic.settings.xml"));		
		
		// Init JADE platform with or without GUI		
		initJADE();
		
		// Start traSMAPI
		TraSMAPI api = new TraSMAPI(); 

		//Create SUMO
		Simulator sumo = createSUMO();

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
	
	private static void initJADE() throws StaleProxyException{
		if(JADE_GUI){
			List<String> params = new ArrayList<String>();
			params.add("-gui");			
			_profile = new BootProfileImpl(params.toArray(new String[0]));
		} else {
			_profile = new ProfileImpl();
		}

		Runtime runtimeInstance = Runtime.instance();		
		_mainContainer = runtimeInstance.createMainContainer(_profile);		
		RomaManagerAgent romaManager = new RomaManagerAgent(1, _mainContainer);		
		_mainContainer.acceptNewAgent("_Roma_", romaManager).start();
	}
	
	private static Simulator createSUMO() throws UnimplementedMethod{
		Simulator sumo = new Sumo("guisim");
		List<String> params = new ArrayList<String>();
		params.add("-c=romaSimulations\\data\\romaBasic.sumo.cfg");
		params.add("--device.emissions.probability=1.0");
        params.add("--tripinfo-output=romaSimulations\\data\\trip.xml");
		sumo.addParameters(params);
		sumo.addConnections("localhost", 8820);
		return sumo;
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
	
	static void setLoggerForRoma(){
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
	}
	
	static void printLogHeader(){
		// ASCII art from: http://ascii.co.uk/art/roman
		LOGGER.info("          ___");
		LOGGER.info("          \\\\||");
		LOGGER.info("         ,'_,-\\");     
		LOGGER.info("         ;'____\\");    
		LOGGER.info("         || =\\=|");    
		LOGGER.info("         ||  - |");                               
		LOGGER.info("     ,---'._--''-,,---------.--.----_,");
		LOGGER.info("    / `-._- _--/,,|  ___,,--'--'._<");
		LOGGER.info("   /-._,  `-.__;,,|'");
		LOGGER.info("  /   ;\\      / , ;");                            
		LOGGER.info(" /  ,' | _ - ',/, ;");
		LOGGER.info("(  (   |     /, ,,;");
		LOGGER.info(" \\  \\  |     ',,/,;");
		LOGGER.info("  \\  \\ |    /, / ,;");
		LOGGER.info(" (| ,^.|   / ,, ,/;");
		LOGGER.info("  `-'./ `-._,, ,/,;");
		LOGGER.info("       ´-._ `-._,,;");
		LOGGER.info("       |/,,`-._ `-.");
		LOGGER.info("       |, ,;, ,`-._\\");		
		LOGGER.info("ROMA Rises...");
	}

}
