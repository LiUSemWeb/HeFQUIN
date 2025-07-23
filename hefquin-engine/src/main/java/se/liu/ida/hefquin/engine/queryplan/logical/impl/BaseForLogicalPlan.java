package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

/**
 * A base class for implementations of {@link LogicalPlan}.
 */
public abstract class BaseForLogicalPlan implements LogicalPlan
{
	// to be created only when requested
	private QueryPlanningInfo info = null;

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
