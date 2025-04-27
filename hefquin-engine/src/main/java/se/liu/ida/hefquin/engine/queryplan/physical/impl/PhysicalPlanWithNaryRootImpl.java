package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;

public class PhysicalPlanWithNaryRootImpl implements PhysicalPlanWithNaryRoot
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
	 */
	protected PhysicalPlanWithNaryRootImpl( final NaryPhysicalOp rootOp,
	                                        final List<PhysicalPlan> subPlans ) {
		assert rootOp != null;
		assert subPlans != null;
		assert ! subPlans.isEmpty();

		this.rootOp = rootOp;
		this.subPlans = subPlans;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( ! (o instanceof PhysicalPlanWithNaryRoot) )
			return false; 

		final PhysicalPlanWithNaryRoot oo = (PhysicalPlanWithNaryRoot) o;
		if ( oo == this )
			return true;

		if ( oo.numberOfSubPlans() != subPlans.size() )
			return false;

		if ( ! oo.getRootOperator().equals(rootOp) )
			return false;

		for ( int i = 0; i < subPlans.size(); ++i ) {
			if ( ! oo.getSubPlan(i).equals(subPlans.get(i)) ) {
				return false;
			}
		}

		return true;
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
	public NaryPhysicalOp getRootOperator() {
		return rootOp;
	}

}
