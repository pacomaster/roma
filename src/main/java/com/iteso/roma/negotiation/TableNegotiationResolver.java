package com.iteso.roma.negotiation;

public class TableNegotiationResolver {	
	
	private int columns = 5;
	private int unit;
	
	private int[][] offerTable;	
	private int[][] dealTable;
	
	public TableNegotiationResolver(int[][] offerTable, int[][] dealTable){
		this.offerTable = offerTable;
		this.dealTable = dealTable;
		columns = offerTable[0].length;
	}
	
	public Offer proposeOffer(PhaseStatus status){
		unit = status.idealTime / columns;
		int col = calculateColumnPriority(status);
		int row = status.priority - 1;
		
		int offeredUnits = offerTable[row][col];		
		Offer offer = new Offer();
		offer.offeredUnits = offeredUnits;
		return offer;
	}
	
	public Offer proposeCounterOffer(PhaseStatus status){
		unit = status.idealTime / columns;
		int col = calculateColumnPriority(status);
		int row = status.priority - 1;
		
		int dealUnits = dealTable[row][col];		
		Offer offer = new Offer();
		offer.offeredUnits = dealUnits;
		return offer;
	}
	
	public boolean acceptOffer(PhaseStatus status, Offer offer){
		unit = status.idealTime / columns;
		int col = calculateColumnPriority(status);
		int row = status.priority - 1;
		
		int deal = dealTable[row][col];		
		if((offer.offeredUnits*unit) >= deal) return true;
		return false;
	}
	
	private int calculateColumnPriority(PhaseStatus status){
		int ret = 0;
		int min = status.idealTime - (unit*2);
		
		int currentTime = min + unit;
		while(status.secondsLeft >= currentTime){
			ret++;
			currentTime += unit;
		}
		return ret;
	}
}
