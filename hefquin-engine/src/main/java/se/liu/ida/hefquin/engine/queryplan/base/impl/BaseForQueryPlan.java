package se.liu.ida.hefquin.engine.queryplan.base.impl;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;

/**
 * This is an abstract base class for classes that implement concrete
 * specializations (sub-interfaces) of the {@link QueryPlan} interface.
 * This base class implements the {@link QueryPlanningInfo}-related methods
 * of {@link QueryPlan}.
 */
public abstract class BaseForQueryPlan implements QueryPlan
{
	private static int counter = 0;

	protected final int id;

	// to be created only when requested, or provided via the constructor
	private QueryPlanningInfo info;

	/**
	 * Use this constructor only if the plan is meant to be constructed with
	 * an already existing {@link QueryPlanningInfo} object. This object may
	 * later be extended with additional properties for this plan. Therefore,
	 * do not create multiple plans with the same {@link QueryPlanningInfo}
	 * object; instead, make copies of such an object if needed. One way to
	 * create such a copy is to use the {@link #BaseForQueryPlan(Iterable)}
	 * constructor instead, passing {@link QueryPlanningInfo#getProperties()}
	 * of the {@link QueryPlanningInfo} object to be copied.
	 */
	protected BaseForQueryPlan( final QueryPlanningInfo qpInfo ) {
		assert qpInfo != null;
		info = qpInfo;

		id = ++counter;
	}

	/**
	 * Use this constructor to create the plan with a {@link QueryPlanningInfo}
	 * object that is initialized with the given properties. This object may
	 * later be extended with additional properties for this plan.
	 *<p>
	 * If the given argument is {@code null} or an empty iterable, then this
	 * constructor does not associate a {@link QueryPlanningInfo} with the
	 * plan.
	 */
	protected BaseForQueryPlan( final Iterable<QueryPlanProperty> qpInfo ) {
		if ( qpInfo != null ) {
			final Iterator<QueryPlanProperty> it = qpInfo.iterator();
			if ( it.hasNext() ) {
				info = new QueryPlanningInfo();

				// Populate the new object with the given properties.
				while ( it.hasNext() ) {
					info.addProperty( it.next() );
				}
			}
		}

		id = ++counter;
	}

	protected BaseForQueryPlan() {
		info = null;

		id = ++counter;
	}

	@Override public int getID() {
		return id;
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

	@Override
	public boolean equals( final Object o ) {
		// Since every plan has a unique ID, two different Java objects
		// that represent query plans cannot be equal even if the plans
		// that they represent are identical (except for their IDs).
		return ( o == this );
	}

	@Override
	public int hashCode(){
		return id;
	}
}
