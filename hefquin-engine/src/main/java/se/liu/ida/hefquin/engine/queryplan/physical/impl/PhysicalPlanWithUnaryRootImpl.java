package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithUnaryRoot;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;

public class PhysicalPlanWithUnaryRootImpl extends BaseForPhysicalPlan
                                           implements PhysicalPlanWithUnaryRoot
{
	private final UnaryPhysicalOp rootOp;
	private final PhysicalPlan subPlan;

	/**
	 * Instead of creating such a plan directly using
	 * this constructor, use {@link PhysicalPlanFactory}.
	 */
	protected PhysicalPlanWithUnaryRootImpl( final UnaryPhysicalOp rootOp,
	                                         final PhysicalPlan subPlan ) {
		assert rootOp != null;
		assert subPlan != null;

		this.rootOp = rootOp;
		this.subPlan = subPlan;
	}

	/**
	 * Instead of creating such a plan directly using
	 * this constructor, use {@link PhysicalPlanFactory}.
	 * <p>
	 * This constructor should be used only if the plan is meant to be
	 * constructed with an already existing {@link QueryPlanningInfo}
	 * object. Since this object may later be extended with additional
	 * properties for this plan, it is important not to create multiple
	 * plans with the same {@link QueryPlanningInfo} object.
	 */
	protected PhysicalPlanWithUnaryRootImpl( final UnaryPhysicalOp rootOp,
	                                         final QueryPlanningInfo qpInfo,
	                                         final PhysicalPlan subPlan ) {
		super(qpInfo);

		assert rootOp != null;
		assert subPlan != null;

		this.rootOp = rootOp;
		this.subPlan = subPlan;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof PhysicalPlanWithUnaryRoot) )
			return false; 

		final PhysicalPlanWithUnaryRoot oo = (PhysicalPlanWithUnaryRoot) o;
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
	public UnaryPhysicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return rootOp.getExpectedVariables( getSubPlan().getExpectedVariables() );
	}

	@Override
	public PhysicalPlan getSubPlan() {
		return subPlan;
	}

	@Override
	public int numberOfSubPlans() { return 1; }

	@Override
	public PhysicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return subPlan;
		else
			throw new NoSuchElementException( "this physical plan does not have a " + i + "-th sub-plan" );
	}

}
