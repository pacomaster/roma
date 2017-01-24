package com.iteso.roma.utils;

import java.util.Iterator;
import java.util.logging.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ACLMessageFactory {
	
	private static final Logger _logger = Logger.getLogger(ACLMessageFactory.class.getName());

	public static ACLMessage createRequestMsg(AID receiver, String content, String conversationId){
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(receiver);
		request.setContent(content);
		request.setConversationId(conversationId);
		request.setReplyWith("request-"+System.currentTimeMillis()); // Unique value
		return request;
	}
	
	public static ACLMessage createRequestMsg(AID[] receivers, String content, String conversationId){
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		for(AID r : receivers) request.addReceiver(r);
		request.setContent(content);
		request.setConversationId(conversationId);
		request.setReplyWith("request-"+System.currentTimeMillis()); // Unique value
		return request;
	}
	
	public static ACLMessage createInformMsg(AID receiver, String content, String conversationId){
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.addReceiver(receiver);
		inform.setContent(content);
		inform.setConversationId(conversationId);
		inform.setReplyWith("inform-"+System.currentTimeMillis()); // Unique value
		return inform;
	}
	
	public static ACLMessage createInformMsg(AID[] receivers, String content, String conversationId){
		ACLMessage request = new ACLMessage(ACLMessage.INFORM);
		for(AID r : receivers) request.addReceiver(r);
		request.setContent(content);
		request.setConversationId(conversationId);
		request.setReplyWith("inform-"+System.currentTimeMillis()); // Unique value
		return request;
	}
	
	public static ACLMessage createCFPMsg(AID receiver, String content, String conversationId){
		ACLMessage request = new ACLMessage(ACLMessage.CFP);
		request.addReceiver(receiver);
		request.setContent(content);
		request.setConversationId(conversationId);
		request.setReplyWith("cfp-"+System.currentTimeMillis()); // Unique value
		return request;
	}
	
	public static ACLMessage createCFPMsg(AID[] receivers, String content, String conversationId){
		ACLMessage request = new ACLMessage(ACLMessage.CFP);
		for(AID r : receivers) request.addReceiver(r);
		request.setContent(content);
		request.setConversationId(conversationId);
		request.setReplyWith("cfp-"+System.currentTimeMillis()); // Unique value
		return request;
	}
	
	public static ACLMessage createAcceptProposalMsg(AID receiver, String content, String conversationId){
		ACLMessage request = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		request.addReceiver(receiver);
		request.setContent(content);
		request.setConversationId(conversationId);
		request.setReplyWith("accept_proposal-"+System.currentTimeMillis()); // Unique value
		return request;
	}
	
	public static ACLMessage createRejectProposalMsg(AID receiver, String content, String conversationId){
		ACLMessage request = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
		request.addReceiver(receiver);
		request.setContent(content);
		request.setConversationId(conversationId);
		request.setReplyWith("reject_proposal-"+System.currentTimeMillis()); // Unique value
		return request;
	}
	
	public static void logMessage(ACLMessage msg){
		if(false){
			Iterator<AID> ite = msg.getAllReceiver();
			while(ite.hasNext()){
				_logger.info(ACLMessage.getPerformative(msg.getPerformative()) + ": " 
						+ msg.getSender().getLocalName() + " -> " 
						+ ite.next().getLocalName() + " (" 
						+ msg.getContent() + ")");			
			}	
		}
	}
}
