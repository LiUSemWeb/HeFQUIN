package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithBinaryRoot;

public class LogicalPlanWithBinaryRootImpl extends BaseForQueryPlan
                                           implements LogicalPlanWithBinaryRoot
{
	private final BinaryLogicalOp rootOp;
	private final LogicalPlan subPlan1;
	private final LogicalPlan subPlan2;

	/**
	 * Constructor.
	 *
	 * @param rootOp - the root operator of the plan to be created
	 * @param qpInfo - query-planning-related properties for a
	 *                 {@link QueryPlanningInfo} object for the
	 *                 plan to be created; may be {@code null},
	 *                 in which case the plan does not get such
	 *                 an object initially
	 * @param subPlan1 - the first child plan of the plan to be created
	 * @param subPlan2 - the second child plan of the plan to be created
	 */
	public LogicalPlanWithBinaryRootImpl( final BinaryLogicalOp rootOp,
	                                      final Iterable<QueryPlanProperty> qpInfo,
	                                      final LogicalPlan subPlan1,
	                                      final LogicalPlan subPlan2 ) {
		super( qpInfo );

		assert rootOp != null;
		assert subPlan1 != null;
		assert subPlan2 != null;

		this.rootOp = rootOp;
		this.subPlan1 = subPlan1;
		this.subPlan2 = subPlan2;
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
