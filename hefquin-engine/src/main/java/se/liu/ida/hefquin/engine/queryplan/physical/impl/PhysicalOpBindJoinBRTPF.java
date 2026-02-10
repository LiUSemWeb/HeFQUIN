package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.BaseForExecOpSequentialBindJoin;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSequentialBindJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.members.BRTPFServer;

/**
 * A physical operator that implements (a batching version of) the bind
 * join algorithm for cases in which the federation member accessed by
 * the algorithm supports the brTPF interface.
 *
 * <p>
 * <b>Semantics:</b> This operator implements the logical operators gpAdd
 * (see {@link LogicalOpGPAdd}) and gpOptAdd (see {@link LogicalOpGPOptAdd}).
 * That is, for a given triple pattern, a federation  member, and an input
 * sequence of solution mappings (produced by the sub-plan under this
 * operator), the operator produces the solutions resulting from the join
 * (inner or left outer) between the input solutions and the solutions of
 * evaluating the given triple pattern over the data of the federation
 * member.
 * </p>
 *
 * <p>
 * <b>Algorithm description:</b> For a detailed description of the
 * actual algorithm associated with this physical operator, refer
 * to {@link ExecOpSequentialBindJoinBRTPF}, which provides the
 * implementation of this algorithm.
 * </p>
 */
public class PhysicalOpBindJoinBRTPF extends BaseForPhysicalOpSingleInputJoin
{
	protected static final Factory factory = new Factory( BaseForExecOpSequentialBindJoin.DEFAULT_BATCH_SIZE );
	public static PhysicalOpFactory getFactory() { return factory; }

	protected final int batchSize;

	protected PhysicalOpBindJoinBRTPF( final LogicalOpGPAdd lop,
	                                   final int batchSize ) {
		super(lop);

		if ( ! lop.containsTriplePatternOnly() )
			throw new IllegalArgumentException();

		if ( lop.hasParameterVariables() )
			throw new IllegalArgumentException();

		this.batchSize = batchSize;
	}

	protected PhysicalOpBindJoinBRTPF( final LogicalOpGPOptAdd lop,
	                                   final int batchSize ) {
		super(lop);

		if ( ! lop.containsTriplePatternOnly() )
			throw new IllegalArgumentException();

		this.batchSize = batchSize;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinBRTPF && ((PhysicalOpBindJoinBRTPF) o).lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables ... inputVars )
	{
		final TriplePattern tp;
		final FederationMember fm;
		final boolean useOuterJoinSemantics;

		if ( lop instanceof LogicalOpGPAdd gpAdd && gpAdd.containsTriplePatternOnly() ) {
			tp = gpAdd.getTP();
			fm = gpAdd.getFederationMember();
			useOuterJoinSemantics = false;
		}
		else if ( lop instanceof LogicalOpGPOptAdd gpAdd && gpAdd.containsTriplePatternOnly() ) {
			tp = gpAdd.getTP();
			fm = gpAdd.getFederationMember();
			useOuterJoinSemantics = true;
		}
		else {
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
		}

		if ( fm instanceof BRTPFServer brtpf )
			return new ExecOpSequentialBindJoinBRTPF( tp,
			                                brtpf,
			                                inputVars[0],
			                                useOuterJoinSemantics,
			                                batchSize,
			                                collectExceptions,
			                                qpInfo );
		else
			throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "> brTPF-based bind join " + "(" + getID() + ") " +  lop.toString();
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
			if( lop instanceof LogicalOpGPAdd op ){
				return    op.containsTriplePatternOnly()
				       && op.getFederationMember() instanceof BRTPFServer
				       && ! op.hasParameterVariables();
			}
			if( lop instanceof LogicalOpGPOptAdd op ){
				return    op.containsTriplePatternOnly()
				       && op.getFederationMember() instanceof BRTPFServer;
			}
			return false;
		}

		@Override
		public PhysicalOpBindJoinBRTPF create( final UnaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpGPAdd op ) {
				return new PhysicalOpBindJoinBRTPF(op, batchSize);
			}
			else if ( lop instanceof LogicalOpGPOptAdd op ) {
				return new PhysicalOpBindJoinBRTPF(op, batchSize);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}
}
