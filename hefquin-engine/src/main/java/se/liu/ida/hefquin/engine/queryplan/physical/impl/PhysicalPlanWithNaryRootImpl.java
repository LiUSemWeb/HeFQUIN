package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;

public class PhysicalPlanWithNaryRootImpl extends BaseForQueryPlan
                                          implements PhysicalPlanWithNaryRoot
{
	protected final NaryPhysicalOp rootOp;
	protected final List<PhysicalPlan> subPlans;

	/**
	 * Instead of creating such a plan directly using
	 * this constructor, use {@link PhysicalPlanFactory}.
	 */
	protected PhysicalPlanWithNaryRootImpl( final NaryPhysicalOp rootOp,
	                                        final PhysicalPlan... subPlans ) {
		this( rootOp, Arrays.asList(subPlans) );
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
	protected PhysicalPlanWithNaryRootImpl( final NaryPhysicalOp rootOp,
	                                        final QueryPlanningInfo qpInfo,
	                                        final PhysicalPlan... subPlans ) {
		this( rootOp, qpInfo, Arrays.asList(subPlans) );
	}

	/**
	 * Instead of creating such a plan directly using
	 * this constructor, use {@link PhysicalPlanFactory}.
	 */
	protected PhysicalPlanWithNaryRootImpl( final NaryPhysicalOp rootOp,
	                                        final List<PhysicalPlan> subPlans ) {
		assert rootOp != null;
		assert subPlans != null;
		assert ! subPlans.isEmpty();

		this.rootOp = rootOp;
		this.subPlans = subPlans;
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
	protected PhysicalPlanWithNaryRootImpl( final NaryPhysicalOp rootOp,
	                                        final QueryPlanningInfo qpInfo,
	                                        final List<PhysicalPlan> subPlans ) {
		super(qpInfo);

		assert rootOp != null;
		assert subPlans != null;
		assert ! subPlans.isEmpty();

		this.rootOp = rootOp;
		this.subPlans = subPlans;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		if (    o instanceof PhysicalPlanWithNaryRoot oo
		     && oo.getRootOperator().equals(rootOp)
		     && oo.numberOfSubPlans() == subPlans.size() )
		{
			final Iterator<PhysicalPlan> it1 = subPlans.iterator();
			final Iterator<PhysicalPlan> it2 = oo.getSubPlans();
			while ( it1.hasNext() ) {
				if ( ! it1.next().equals(it2.next()) )
					return false;
			}

			return true;
		}
		else
			return false;
	}

	@Override
	public int hashCode(){
		int code = rootOp.hashCode();
		final Iterator<PhysicalPlan> it = subPlans.iterator();
		while ( it.hasNext() )
			code = code ^ it.next().hashCode();
		return code;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		final ExpectedVariables[] e = new ExpectedVariables[ subPlans.size() ];
		for ( int i = 0; i < subPlans.size(); ++i ) {
			e[i] = subPlans.get(i).getExpectedVariables();
		}
		return rootOp.getExpectedVariables(e);
	}

	@Override
	public int numberOfSubPlans() {
		return subPlans.size();
	}

	@Override
	public PhysicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		return subPlans.get(i);
	}

	@Override
	public Iterator<PhysicalPlan> getSubPlans() {
		return subPlans.iterator();
	}

	@Override
	public NaryPhysicalOp getRootOperator() {
		return rootOp;
	}

}
