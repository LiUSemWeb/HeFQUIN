package se.liu.ida.hefquin.engine.queryplan.base.impl;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

/**
 * This is an abstract base class for classes that implement concrete
 * specializations (sub-interfaces) of the {@link QueryPlan} interface.
 * This base class implements the {@link QueryPlanningInfo}-related methods
 * of {@link QueryPlan}.
 */
public abstract class BaseForQueryPlan implements QueryPlan
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
	protected BaseForQueryPlan( final QueryPlanningInfo qpInfo ) {
		assert qpInfo != null;
		info = qpInfo;
	}

	protected BaseForQueryPlan() {
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
