package se.liu.ida.hefquin.federation.access.impl;

import se.liu.ida.hefquin.federation.access.DataRetrievalInterface;

public abstract class DataRetrievalInterfaceBase implements DataRetrievalInterface
{
	private static int counter = 0;
	protected final int id;
	
	public DataRetrievalInterfaceBase() {
		this.id = counter++;
	}
	
	@Override
	public int getID() {
		return id;
	}
}
