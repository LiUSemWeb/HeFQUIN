package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;

public class PhysicalPlanWithNullaryRootImpl implements PhysicalPlanWithNullaryRoot
{
	private final NullaryPhysicalOp rootOp;

	/**
	 * Instead of creating such a plan directly using
	 * this constructor, use {@link PhysicalPlanFactory}.
	 */
	protected PhysicalPlanWithNullaryRootImpl( final NullaryPhysicalOp rootOp ) {
		assert rootOp != null;
		this.rootOp = rootOp;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof PhysicalPlanWithNullaryRoot) )
			return false; 

		final PhysicalPlanWithNullaryRoot oo = (PhysicalPlanWithNullaryRoot) o;
		if ( oo == this )
			return true;
		else
			return oo.getRootOperator().equals(rootOp); 
	}

	@Override
	public int hashCode(){
		return rootOp.hashCode();
	}

	@Override
	public NullaryPhysicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return rootOp.getExpectedVariables();
	}

	@Override
	public int numberOfSubPlans() { return 0; }

	@Override
	public PhysicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		throw new NoSuchElementException( "this physical plan does not have any sub-plan" );
	}

}
