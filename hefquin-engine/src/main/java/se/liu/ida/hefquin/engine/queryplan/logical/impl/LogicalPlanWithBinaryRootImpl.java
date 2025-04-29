package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithBinaryRoot;

public class LogicalPlanWithBinaryRootImpl implements LogicalPlanWithBinaryRoot
{
	private final BinaryLogicalOp rootOp;
	private final LogicalPlan subPlan1;
	private final LogicalPlan subPlan2;

	public LogicalPlanWithBinaryRootImpl( final BinaryLogicalOp rootOp,
	                                      final LogicalPlan subPlan1,
	                                      final LogicalPlan subPlan2 ) {
		assert rootOp != null;
		assert subPlan1 != null;
		assert subPlan2 != null;

		this.rootOp = rootOp;
		this.subPlan1 = subPlan1;
		this.subPlan2 = subPlan2;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof LogicalPlanWithBinaryRoot) )
			return false; 

		final LogicalPlanWithBinaryRoot oo = (LogicalPlanWithBinaryRoot) o;
		if ( oo == this )
			return true;
		else
			return oo.getRootOperator().equals(rootOp)
					&& oo.getSubPlan1().equals(subPlan1)
					&& oo.getSubPlan2().equals(subPlan2); 
	}

	@Override
	public int hashCode(){
		return rootOp.hashCode() ^ subPlan1.hashCode() ^ subPlan2.hashCode();
	}

	@Override
	public BinaryLogicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return rootOp.getExpectedVariables(
				getSubPlan1().getExpectedVariables(),
				getSubPlan2().getExpectedVariables() );
	}

	@Override
	public LogicalPlan getSubPlan1() {
		return subPlan1;
	}

	@Override
	public LogicalPlan getSubPlan2() {
		return subPlan2;
	}

	@Override
	public int numberOfSubPlans() { return 2; }

	@Override
	public LogicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return subPlan1;
		else if ( i == 1 )
			return subPlan2;
		else
			throw new NoSuchElementException( "this logical plan does not have a " + i + "-th sub-plan" );
	}

}
