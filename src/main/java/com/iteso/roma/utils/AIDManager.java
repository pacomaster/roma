package com.iteso.roma.utils;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class AIDManager {	
	public static AID getPhaseAID(String phaseId, Agent theAgent){
		// Search phase service receiver
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(phaseId);
		template.addServices(sd);
		AID receiver =  null;
		try {
			DFAgentDescription[] result = DFService.search(theAgent, template); 
			if(result.length > 0) receiver = result[0].getName();
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return receiver;
	}
	
	public static AID getJunctionAID(String junctionId, Agent theAgent){
		// Search junction service receiver
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(junctionId);
		template.addServices(sd);
		AID receiver =  null;
		try {
			DFAgentDescription[] result = DFService.search(theAgent, template); 
			if(result.length > 0) receiver = result[0].getName();
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return receiver;
	}

}
