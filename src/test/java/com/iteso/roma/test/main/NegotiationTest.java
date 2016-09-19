package com.iteso.roma.test.main;

import org.junit.Test;

import com.iteso.roma.negotiation.Offer;
import com.iteso.roma.negotiation.PhaseStatus;
import com.iteso.roma.negotiation.TableNegotiationResolver;

import junit.framework.TestCase;

public class NegotiationTest extends TestCase{
	
	private final int INF = Integer.MAX_VALUE;
	
	private int[][] offerTable = {
			{0,0,0,0,1},
			{0,0,0,1,1},
			{0,0,1,1,2},
			{0,1,1,2,3},
			{1,1,2,3,4}};
	
	private int[][] dealTable = {
			{2  ,1  ,1  ,1  ,INF},
			{3  ,2  ,1  ,1  ,INF},
			{4  ,3  ,2  ,1  ,INF},
			{4  ,3  ,2  ,INF,INF},
			{INF,INF,INF,INF,INF}};
	
	@Test
	public void testAcceptNegotiation(){
		PhaseStatus status = new PhaseStatus();
		status.idealTime=15;
		status.priority=1;
		status.secondsLeft=9;
		
		Offer offer = new Offer();
		offer.offeredUnits=2;
		
		TableNegotiationResolver negotiationResolver = new TableNegotiationResolver(offerTable, dealTable);
		
		assertTrue("The Resolver should accept the offer", negotiationResolver.acceptOffer(status, offer));
		
		status.priority=4;
		status.secondsLeft=18;
		
		assertFalse("The Resolver should reject the offer", negotiationResolver.acceptOffer(status, offer));
	}
	
	@Test
	public void testOfferTable(){
		PhaseStatus status = new PhaseStatus();
		status.idealTime=15;
		status.priority=5;
		status.secondsLeft=21;
		
		TableNegotiationResolver negotiationResolver = new TableNegotiationResolver(offerTable, dealTable);
		
		int proposeUnits = 4;
		
		assertEquals("The Resolver should propose " + proposeUnits + " units", proposeUnits, (negotiationResolver.proposeOffer(status)).offeredUnits);
		
		proposeUnits = 1;
		status.priority=3;
		status.secondsLeft=15;
		
		assertEquals("The Resolver should propose " + proposeUnits + " units", proposeUnits, (negotiationResolver.proposeOffer(status)).offeredUnits);
		
		proposeUnits = 0;
		status.priority=1;
		status.secondsLeft=9;
		
		assertEquals("The Resolver should propose " + proposeUnits + " units", proposeUnits, (negotiationResolver.proposeOffer(status)).offeredUnits);
	}
	
	@Test
	public void testDealTable(){
		PhaseStatus status = new PhaseStatus();
		status.idealTime=15;
		status.priority=5;
		status.secondsLeft=21;
		
		TableNegotiationResolver negotiationResolver = new TableNegotiationResolver(offerTable, dealTable);
		
		int proposeUnits = INF;
		
		assertEquals("The Resolver should propose " + proposeUnits + " units", proposeUnits, (negotiationResolver.proposeCounterOffer(status)).offeredUnits);
		
		proposeUnits = 2;
		status.priority=3;
		status.secondsLeft=15;
		
		assertEquals("The Resolver should propose " + proposeUnits + " units", proposeUnits, (negotiationResolver.proposeCounterOffer(status)).offeredUnits);
		
		proposeUnits = 4;
		status.priority=4;
		status.secondsLeft=9;
		
		assertEquals("The Resolver should propose " + proposeUnits + " units", proposeUnits, (negotiationResolver.proposeCounterOffer(status)).offeredUnits);
	}

}
