package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpLookupJoinViaWrapperWithParamVars;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpLookupJoinViaWrapperWithoutParamVars;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;

/**
 * A physical operator that .. TODO
 */
public class PhysicalOpLookupJoinViaWrapper extends BaseForPhysicalOpSingleInputJoin
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	protected PhysicalOpLookupJoinViaWrapper( final LogicalOpGPAdd lop ) {
		super(lop);

		// checks
		assert lop.getFederationMember() instanceof WrappedRESTEndpoint;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpLookupJoinViaWrapper oo && oo.lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables ... inputVars )
	{
		final LogicalOpGPAdd gpAdd = (LogicalOpGPAdd) lop;
		final WrappedRESTEndpoint ep = (WrappedRESTEndpoint) gpAdd.getFederationMember();

		if ( gpAdd.hasParameterVariables() )
			return new ExecOpLookupJoinViaWrapperWithParamVars( gpAdd.getPattern(),
			                                                    gpAdd.getParameterVariables(),
			                                                    ep,
			                                                    collectExceptions,
			                                                    qpInfo );
		else
			return new ExecOpLookupJoinViaWrapperWithoutParamVars( gpAdd.getPattern(),
			                                                       ep,
			                                                       collectExceptions,
			                                                       qpInfo );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "> wrapper-based lookup join " + "(" + getID() + ") " +  lop.toString();
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			assert inputVars != null;
			assert inputVars.length == 1;

			if(    lop instanceof LogicalOpGPAdd gpAdd
			    && gpAdd.getFederationMember() instanceof WrappedRESTEndpoint ep
			    && ep.isSupportedPattern(gpAdd.getPattern()) )
			{
				if ( gpAdd.hasParameterVariables() ) {
					final List<Var> paramVars = gpAdd.getParameterVariables();

					if ( ep.getNumberOfParameters() != paramVars.size() )
						return false;

					// check that each of the parameter variables is certainly bound
					final Set<Var> certainInputVars = inputVars[0].getCertainVariables();
					for ( final Var v : gpAdd.getParameterVariables() ) {
						if ( ! certainInputVars.contains(v) )
							return false;
					}

					return true;
				}
				else {
					return ( ep.getNumberOfParameters() == 0 );
				}
			}
			else {
				return false;
			}
		}

		@Override
		public PhysicalOpLookupJoinViaWrapper create( final UnaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpGPAdd op ) {
				return new PhysicalOpLookupJoinViaWrapper(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
