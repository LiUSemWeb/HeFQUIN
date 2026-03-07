package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;

public class LogicalPlanWithNullaryRootImpl extends BaseForQueryPlan
                                            implements LogicalPlanWithNullaryRoot
{
	private final NullaryLogicalOp rootOp;

	/**
	 * Constructor.
	 *
	 * @param rootOp - the root operator of the plan to be created
	 * @param qpInfo - query-planning-related properties for a
	 *                 {@link QueryPlanningInfo} object for the
	 *                 plan to be created; may be {@code null},
	 *                 in which case the plan does not get such
	 *                 an object initially
	 */
	public LogicalPlanWithNullaryRootImpl( final NullaryLogicalOp rootOp,
	                                       final Iterable<QueryPlanProperty> qpInfo ) {
		super( qpInfo );

		assert rootOp != null;
		this.rootOp = rootOp;
	}

	@Override
	public NullaryLogicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return rootOp.getExpectedVariables();
	}

	@Override
	public int numberOfSubPlans() { return 0; }

	@Override
	public LogicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		throw new NoSuchElementException( "this logical plan does not have any sub-plans" );
	}

}
