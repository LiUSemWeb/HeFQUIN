package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.WrappedFederationMember;

/**
 * A physical operator that .. TODO
 */
public class PhysicalOpBindJoinViaWrapper extends BaseForPhysicalOpSingleInputJoin
{
	protected static final Factory factory = new Factory();

	public static Factory getFactory() { return factory; }

	protected PhysicalOpBindJoinViaWrapper( final LogicalOpGPAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof WrappedFederationMember;
		assert lop.hasParameterVariables();
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinViaWrapper oo && oo.lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables ... inputVars )
	{
		final LogicalOpGPAdd gpAdd = (LogicalOpGPAdd) lop;
		final WrappedFederationMember wfm = (WrappedFederationMember) gpAdd.getFederationMember();

		gpAdd.getPattern();
		gpAdd.getParameterVariables();

// TODO
		return null;
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "> wrapper-based bind join " + "(" + getID() + ") " +  lop.toString();
	}

	protected static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			assert inputVars != null;
			assert inputVars.length == 1;

			if(    lop instanceof LogicalOpGPAdd gpAdd
			    && gpAdd.hasParameterVariables()
			    && gpAdd.getFederationMember() instanceof WrappedFederationMember wfm
			    && wfm.isSupportedPattern(gpAdd.getPattern()) ) {
				// check that each of the parameter variables is certainly bound
				final Set<Var> certainInputVars = inputVars[0].getCertainVariables();
				int cnt = 0;
				for ( final Var v : gpAdd.getParameterVariables() ) {
					cnt++;
					if ( ! certainInputVars.contains(v) )
						return false;
				}

				// check that the number of parameter variables is supported
				if ( wfm.isSupportedNumberOfArguments(cnt) )
					return true;
			}

			return false;
		}

		@Override
		public PhysicalOpBindJoinViaWrapper create( final LogicalOperator lop ) {
			if ( lop instanceof LogicalOpGPAdd op ) {
				return new PhysicalOpBindJoinViaWrapper(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
