package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithUnaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalPlanWithUnaryRootImpl extends BaseForQueryPlan
                                          implements LogicalPlanWithUnaryRoot
{
	private final UnaryLogicalOp rootOp;
	private final LogicalPlan subPlan;

	public LogicalPlanWithUnaryRootImpl( final UnaryLogicalOp rootOp,
	                                     final LogicalPlan subPlan ) {
		assert rootOp != null;
		assert subPlan != null;

		this.rootOp = rootOp;
		this.subPlan = subPlan;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof LogicalPlanWithUnaryRoot) )
			return false; 

		final LogicalPlanWithUnaryRoot oo = (LogicalPlanWithUnaryRoot) o;
		if ( oo == this )
			return true;
		else
			return oo.getRootOperator().equals(rootOp)
					&& oo.getSubPlan().equals(subPlan); 
	}

	@Override
	public int hashCode(){
		return rootOp.hashCode() ^ subPlan.hashCode();
	}

	@Override
	public UnaryLogicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return rootOp.getExpectedVariables( getSubPlan().getExpectedVariables() );
	}

	@Override
	public LogicalPlan getSubPlan() {
		return subPlan;
	}

	@Override
	public int numberOfSubPlans() { return 1; }

	@Override
	public LogicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return subPlan;
		else
			throw new NoSuchElementException( "this logical plan does not have a " + i + "-th sub-plan" );
	}

}
