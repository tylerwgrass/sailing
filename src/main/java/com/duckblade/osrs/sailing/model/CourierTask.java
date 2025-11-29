package com.duckblade.osrs.sailing.model;

import lombok.Data;

@Data
public class CourierTask
{
	private final int taskSlot;
	private final Port fromPort;
	private final Port toPort;
	private final int cargoCrateItemID;
	private final int cargoAmount;
	private final int numCargoRetrieved;
	private final int numCargoDelivered;

	public boolean hasRetrievedAllCargo()
	{
		return numCargoRetrieved == cargoAmount;
	}

	public boolean hasDeliveredAllCargo()
	{
		return numCargoDelivered == cargoAmount;
	}
}
