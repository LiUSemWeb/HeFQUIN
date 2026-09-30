package se.liu.ida.hefquin.engine.queryplan.utils;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitorBase;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.federation.FederationMember;

/**
 * This class provides useful functionality related to logical plans
 */
public class LogicalPlanUtils
{
	/**
	 * Returns the federation members referenced by the given logical plan.
	 * Duplicate federation members are included only once in the returned set.
	 *
	 * @param lplan the logical plan whose federation members should be collected
	 * @return the set of federation members referenced by the logical plan
	 */
	public Set<FederationMember> getFederationMembers( final LogicalPlan lplan ) {
		final Set<FederationMember> fmsToCheck = new HashSet<>();

		LogicalPlanWalker.walk( lplan,
			new LogicalPlanVisitorBase() {
				@Override
				public void visit( final LogicalOpRequest<?,?> op ) {
					fmsToCheck.add( op.getFederationMember() );
				}

				@Override
				public void visit( final LogicalOpMultiRequest op ) {
					for ( final FederationMember fm : op.getFederationMembers() )
						fmsToCheck.add( fm );
				}

				@Override
				public void visit( final LogicalOpGPAdd op ) {
					fmsToCheck.add( op.getFederationMember() );
				}

				@Override
				public void visit( final LogicalOpGPOptAdd op ) {
					fmsToCheck.add( op.getFederationMember() );
				}
			},
			null );

		return fmsToCheck;
	}
}
