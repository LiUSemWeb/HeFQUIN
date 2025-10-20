package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithBoundJoin;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;

/**
 * A physical operator that implements (a batching version of) the bound-join
 * algorithm that uses UNION clauses with variable renaming (as proposed in
 * the FedX paper by Schwarte et al. 2011). The variable that is renamed can
 * be any non-join variable.
 *
 *
 * <p>
 * <b>Semantics:</b> This operator implements the logical operators gpAdd
 * (see {@link LogicalOpGPAdd}) and gpOptAdd (see {@link LogicalOpGPOptAdd}).
 * That is, for a given graph pattern, a federation  member, and an input
 * sequence of solution mappings (produced by the sub-plan under this
 * operator), the operator produces the solutions resulting from the join
 * (inner or left outer) between the input solutions and the solutions of
 * evaluating the given graph pattern over the data of the federation
 * member.
 * </p>
 *
 * <p>
 * <b>Algorithm description:</b> For a detailed description of the
 * actual algorithm associated with this physical operator, refer
 * to {@link ExecOpBindJoinWithBoundJoin}, which provides the
 * implementation of this algorithm.
 * </p>
 */
public class PhysicalOpBindJoinWithBoundJoin extends BaseForPhysicalOpSingleInputJoinAtSPARQLEndpoint
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	protected PhysicalOpBindJoinWithBoundJoin( final LogicalOpGPAdd lop ) {
		super(lop);
	}

	protected PhysicalOpBindJoinWithBoundJoin( final LogicalOpGPOptAdd lop ) {
		super(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final SPARQLGraphPattern pattern,
	                                       final SPARQLEndpoint sparqlEndpoint,
	                                       final boolean useOuterJoinSemantics,
	                                       final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables... inputVars ) {
		return new ExecOpBindJoinSPARQLwithBoundJoin( pattern,
		                                              sparqlEndpoint,
		                                              inputVars[0],
		                                              useOuterJoinSemantics,
		                                              ExecOpBindJoinSPARQLwithBoundJoin.DEFAULT_BATCH_SIZE,
		                                              collectExceptions,
		                                              qpInfo );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinWithBoundJoin
				&& ((PhysicalOpBindJoinWithBoundJoin) o).lop.equals(lop);
	}

	@Override
	public String toString() {
		return "> BoundJoinBindJoin" + lop.toString();
	}

	protected static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			if ( lop instanceof LogicalOpGPAdd op ) {
				return    isSupported( op.getFederationMember(), op.getPattern(), inputVars[0] )
				       && ! op.hasParameterVariables();
			}
			if ( lop instanceof LogicalOpGPOptAdd op ) {
				return isSupported( op.getFederationMember(), op.getPattern(), inputVars[0] );
			}
			return false;
		}

		@Override
		public PhysicalOpBindJoinWithBoundJoin create( final UnaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpGPAdd op ) {
				return new PhysicalOpBindJoinWithBoundJoin(op);
			}
			else if ( lop instanceof LogicalOpGPOptAdd op ) {
				return new PhysicalOpBindJoinWithBoundJoin(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}

		private boolean isSupported( final FederationMember fm,
		                             final SPARQLGraphPattern pattern,
		                             final ExpectedVariables vars )
		{
			return (fm instanceof SPARQLEndpoint) && hasNonJoiningVar(pattern, vars);
		}

		private boolean hasNonJoiningVar( final SPARQLGraphPattern pattern, final ExpectedVariables vars ) {
			if ( vars == null )
				return true;

			final Set<Var> certainVars = vars.getCertainVariables();
			final Set<Var> possibleVars = vars.getPossibleVariables();

			for ( final Var v : pattern.getCertainVariables() ) {
				if ( ! certainVars.contains(v) && ! possibleVars.contains(v) ) {
					return true;
				}
			}
			return false;
		}
	}
}
