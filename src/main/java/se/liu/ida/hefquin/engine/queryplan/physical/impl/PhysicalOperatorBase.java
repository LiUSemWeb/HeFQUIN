package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;

public abstract class PhysicalOperatorBase implements PhysicalOperator 
{
	private static int counter = 0;
	protected final int id;
	
	public PhysicalOperatorBase() {
		this.id = counter++;
	}
	
	@Override
	public int getID() {
		return id;
	}
}
