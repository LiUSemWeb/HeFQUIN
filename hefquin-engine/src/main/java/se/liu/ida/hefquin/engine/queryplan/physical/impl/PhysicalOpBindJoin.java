package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;

/**
 * A physical operator that implements (a batching version of) the bind
 * join algorithm for cases in which the federation member accessed by
 * the algorithm supports the brTPF interface.
 *
 * <p>
 * <b>Semantics:</b> This operator implements the logical operators tpAdd
 * (see {@link LogicalOpTPAdd}) and tpOptAdd (see {@link LogicalOpTPOptAdd}).
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
 * to {@link ExecOpBindJoinBRTPF}, which provides the
 * implementation of this algorithm.
 * </p>
 */
public class PhysicalOpBindJoin extends BaseForPhysicalOpSingleInputJoin
{
	public PhysicalOpBindJoin( final LogicalOpGPAdd lop ) {
		super(lop);

		if ( ! lop.containsTriplePatternOnly() )
			throw new IllegalArgumentException();
	}

	public PhysicalOpBindJoin( final LogicalOpGPOptAdd lop ) {
		super(lop);

		if ( ! lop.containsTriplePatternOnly() )
			throw new IllegalArgumentException();
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoin && ((PhysicalOpBindJoin) o).lop.equals(lop);
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
			return new ExecOpBindJoinBRTPF( tp,
			                                brtpf,
			                                inputVars[0],
			                                useOuterJoinSemantics,
			                                ExecOpBindJoinBRTPF.DEFAULT_BATCH_SIZE,
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
		return "> bindJoin" + lop.toString();
	}

}
