package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

/**
 * A base class for implementations of {@link PhysicalPlan}.
 */
public abstract class BaseForPhysicalPlan implements PhysicalPlan
{
	// to be created only when requested
	private QueryPlanningInfo info = null;

	@Override
	public QueryPlanningInfo getQueryPlanningInfo() {
		if ( info == null )
			info = new QueryPlanningInfo();

		return info;
	}
}
