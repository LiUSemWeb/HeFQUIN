package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithUnaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;

public class LogicalPlanWithUnaryRootImpl extends BaseForQueryPlan
                                          implements LogicalPlanWithUnaryRoot
{
	private final UnaryLogicalOp rootOp;
	private final LogicalPlan subPlan;

	/**
	 * Constructor.
	 *
	 * @param rootOp - the root operator of the plan to be created
	 * @param qpInfo - query-planning-related properties for a
	 *                 {@link QueryPlanningInfo} object for the
	 *                 plan to be created; may be {@code null},
	 *                 in which case the plan does not get such
	 *                 an object initially
	 * @param subPlan - the child plan of the plan to be created
	 */
	public LogicalPlanWithUnaryRootImpl( final UnaryLogicalOp rootOp,
	                                     final Iterable<QueryPlanProperty> qpInfo,
	                                     final LogicalPlan subPlan ) {
		super( qpInfo );

		assert rootOp != null;
		assert subPlan != null;

		this.rootOp = rootOp;
		this.subPlan = subPlan;
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
