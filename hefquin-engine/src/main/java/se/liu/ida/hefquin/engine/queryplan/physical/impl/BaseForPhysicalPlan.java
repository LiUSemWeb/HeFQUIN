package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

/**
 * A base class for implementations of {@link PhysicalPlan}.
 */
public abstract class BaseForPhysicalPlan implements PhysicalPlan
{
	// to be created only when requested, or provided via the constructor
	private QueryPlanningInfo info;

	/**
	 * Use this constructor only if the plan is meant to be constructed with
	 * an already existing {@link QueryPlanningInfo} object. This object may
	 * later be extended with additional properties for this plan. Therefore,
	 * do not create multiple plans with the same {@link QueryPlanningInfo}
	 * object; instead, make copies of such an object if needed.
	 */
	protected BaseForPhysicalPlan( final QueryPlanningInfo qpInfo ) {
		assert qpInfo != null;
		info = qpInfo;
	}

	protected BaseForPhysicalPlan() {
		info = null;
	}

	@Override
	public boolean hasQueryPlanningInfo() {
		return info != null;
	}

	@Override
	public QueryPlanningInfo getQueryPlanningInfo() {
		if ( info == null )
			info = new QueryPlanningInfo();

		return info;
	}
}
