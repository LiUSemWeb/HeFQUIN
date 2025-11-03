package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpBindJoinWithRequestOps;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithVALUESorFILTER;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

/**
* A physical operator that implements (a batching version of) the bind
 * join algorithm. It starts by using a VALUES clause to capture the potential join
 * partners that are sent to the federation member. If this fails, it uses the bind join
 * algorithm with a FILTER clause instead.

 * <p>
 * <b>Algorithm description:</b> For a detailed description of the
 * actual algorithm associated with this physical operator, refer
 * to {@link ExecOpBindJoinSPARQLwithVALUESorFILTER}, which provides the
 * implementation of this algorithm.
 * </p>
 */
public class PhysicalOpBindJoinWithVALUESorFILTER extends BaseForPhysicalOpSingleInputJoinAtSPARQLEndpoint
{
	protected static final Factory factory = new Factory( BaseForExecOpBindJoinWithRequestOps.DEFAULT_BATCH_SIZE );
	public static PhysicalOpFactory getFactory() { return factory; }

	protected PhysicalOpBindJoinWithVALUESorFILTER( final LogicalOpGPAdd lop,
	                                                final int batchSize ) {
		super(lop, batchSize);
	}

	protected PhysicalOpBindJoinWithVALUESorFILTER( final LogicalOpGPOptAdd lop,
	                                                final int batchSize ) {
		super(lop, batchSize);
	}

	@Override
	public UnaryExecutableOp createExecOp( final SPARQLGraphPattern pattern,
	                                       final SPARQLEndpoint sparqlEndpoint,
	                                       final boolean useOuterJoinSemantics,
	                                       final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables... inputVars ) {
		return new ExecOpBindJoinSPARQLwithVALUESorFILTER( pattern,
		                                                   sparqlEndpoint,
		                                                   inputVars[0],
		                                                   useOuterJoinSemantics,
		                                                   batchSize,
		                                                   collectExceptions,
		                                                   qpInfo );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinWithVALUESorFILTER
				&& ((PhysicalOpBindJoinWithVALUESorFILTER) o).lop.equals(lop);
	}

	@Override
	public String toString() {
		return "> VALUESorFILTERBindJoin" + lop.toString();
	}

	public static class Factory implements PhysicalOpFactory
	{
		public final int batchSize;

		public Factory( final int batchSize ) {
			assert batchSize > 0;
			this.batchSize = batchSize;
		}

		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			if ( lop instanceof LogicalOpGPAdd op ) {
				return    op.getFederationMember() instanceof SPARQLEndpoint
				       && ! op.hasParameterVariables();
			}
			if ( lop instanceof LogicalOpGPOptAdd op ) {
				return op.getFederationMember() instanceof SPARQLEndpoint;
			}
			return false;
		}

		@Override
		public PhysicalOpBindJoinWithVALUESorFILTER create( final UnaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpGPAdd op ) {
				return new PhysicalOpBindJoinWithVALUESorFILTER(op, batchSize);
			}
			else if ( lop instanceof LogicalOpGPOptAdd op ) {
				return new PhysicalOpBindJoinWithVALUESorFILTER(op, batchSize);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
