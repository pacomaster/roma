package com.iteso.roma.jade;

import java.util.logging.Logger;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class ServiceRegister {
	
	private static final Logger logger = Logger.getLogger(ServiceRegister.class.getName());
	
	public static void register(Agent agent, String name){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(agent.getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(name);
		sd.setName(name);
		dfd.addServices(sd);
		try {
			DFService.register(agent, dfd);
		}
		catch (FIPAException e) {
			logger.severe(e.getMessage());
		}
	}

}
