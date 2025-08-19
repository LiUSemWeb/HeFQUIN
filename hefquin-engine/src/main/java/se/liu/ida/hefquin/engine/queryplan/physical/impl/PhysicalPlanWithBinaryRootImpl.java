package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithBinaryRoot;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;

public class PhysicalPlanWithBinaryRootImpl extends BaseForQueryPlan
                                            implements PhysicalPlanWithBinaryRoot
{
	private final BinaryPhysicalOp rootOp;
	private final PhysicalPlan subPlan1;
	private final PhysicalPlan subPlan2;

	/**
	 * Instead of creating such a plan directly using
	 * this constructor, use {@link PhysicalPlanFactory}.
	 */
	protected PhysicalPlanWithBinaryRootImpl( final BinaryPhysicalOp rootOp,
	                                          final PhysicalPlan subPlan1,
	                                          final PhysicalPlan subPlan2 ) {
		assert rootOp != null;
		assert subPlan1 != null;
		assert subPlan2 != null;

		this.rootOp = rootOp;
		this.subPlan1 = subPlan1;
		this.subPlan2 = subPlan2;
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
	protected PhysicalPlanWithBinaryRootImpl( final BinaryPhysicalOp rootOp,
	                                          final QueryPlanningInfo qpInfo,
	                                          final PhysicalPlan subPlan1,
	                                          final PhysicalPlan subPlan2 ) {
		super(qpInfo);

		assert rootOp != null;
		assert subPlan1 != null;
		assert subPlan2 != null;

		this.rootOp = rootOp;
		this.subPlan1 = subPlan1;
		this.subPlan2 = subPlan2;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof PhysicalPlanWithBinaryRoot) )
			return false; 

		final PhysicalPlanWithBinaryRoot oo = (PhysicalPlanWithBinaryRoot) o;
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
	public BinaryPhysicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return rootOp.getExpectedVariables(
				getSubPlan1().getExpectedVariables(),
				getSubPlan2().getExpectedVariables() );
	}

	@Override
	public PhysicalPlan getSubPlan1() {
		return subPlan1;
	}

	@Override
	public PhysicalPlan getSubPlan2() {
		return subPlan2;
	}

	@Override
	public int numberOfSubPlans() { return 2; }

	@Override
	public PhysicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return subPlan1;
		else if ( i == 1 )
			return subPlan2;
		else
			throw new NoSuchElementException( "this physical plan does not have a " + i + "-th sub-plan" );
	}

}
