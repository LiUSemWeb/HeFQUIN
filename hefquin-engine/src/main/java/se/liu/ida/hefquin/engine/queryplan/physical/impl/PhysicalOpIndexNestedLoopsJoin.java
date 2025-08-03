package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpIndexNestedLoopsJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpIndexNestedLoopsJoinSPARQL;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpIndexNestedLoopsJoinTPF;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;

/**
 * A physical operator that implements a version of the index nested loops
 * join algorithm where the federation member is used as an index to request
 * join partners for the input solution mappings.
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
 * <b>Algorithm description:</b> The outer loop iterates over the sequence
 * of input solution mappings. For each such solution mapping, join partners
 * within the result of the given graph pattern over the data of the federation
 * member are retrieved by using the federation member as a form of index. The
 * inner loop then iterates over these join partners and merges them with the
 * current input solution mapping from the outer loop. The retrieval of join
 * partners is done by applying the current input solution mapping from the
 * outer loop to the given graph pattern of the operator (i.e., substituting
 * the join variables in the pattern by the values assigned in the current
 * input solution mapping) and, then, performing a request with the graph
 * pattern resulting from the substitution. Hence, this is like a bind join
 * without batching.
 * </p>
 *
 * The actual algorithm of this operator is implemented in the following three
 * classes, where each of them is specific to a different type of federation
 * member.
 * <ul>
 * <li>{@link ExecOpIndexNestedLoopsJoinTPF}</li>
 * <li>{@link ExecOpIndexNestedLoopsJoinBRTPF}</li>
 * <li>{@link ExecOpIndexNestedLoopsJoinSPARQL}</li>
 * </ul>
 */
public class PhysicalOpIndexNestedLoopsJoin extends BaseForPhysicalOpSingleInputJoin
{
	public PhysicalOpIndexNestedLoopsJoin( final LogicalOpGPAdd lop ) {
		super(lop);
	}

	public PhysicalOpIndexNestedLoopsJoin( final LogicalOpGPOptAdd lop ) {
		super(lop);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpIndexNestedLoopsJoin
				&& ((PhysicalOpIndexNestedLoopsJoin) o).lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final QueryPlanningInfo qpInfo,
	                                       final ExpectedVariables ... inputVars )
	{
		final SPARQLGraphPattern gp;
		final TriplePattern tp;
		final FederationMember fm;
		final boolean useOuterJoin;

		if ( lop instanceof LogicalOpGPAdd gpAdd ) {
			gp = gpAdd.getPattern();
			fm = gpAdd.getFederationMember();
			useOuterJoin = false;

			if ( fm instanceof TPFServer || fm instanceof BRTPFServer )
				tp = gpAdd.getTP();
			else
				tp = null;
		}
		else if ( lop instanceof LogicalOpGPOptAdd gpAdd ) {
			gp = gpAdd.getPattern();
			fm = gpAdd.getFederationMember();
			useOuterJoin = true;

			if ( fm instanceof TPFServer || fm instanceof BRTPFServer )
				tp = gpAdd.getTP();
			else
				tp = null;
		}
		else {
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
		}

		if ( fm instanceof TPFServer || fm instanceof BRTPFServer ) {
			if ( tp == null ) {
				throw new IllegalArgumentException("For TPF and brTPF, the graph pattern must be a triple pattern, but it is: " + gp.getClass().getName() );
			}
		}

		if ( fm instanceof TPFServer tpf )
			return new ExecOpIndexNestedLoopsJoinTPF(tp, tpf, useOuterJoin, collectExceptions, qpInfo);
		else if ( fm instanceof BRTPFServer brtpf )
			return new ExecOpIndexNestedLoopsJoinBRTPF(tp, brtpf, useOuterJoin, collectExceptions, qpInfo);
		else if ( fm instanceof SPARQLEndpoint ep )
			return new ExecOpIndexNestedLoopsJoinSPARQL(gp, ep, useOuterJoin, collectExceptions, qpInfo);
		else
			throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString(){

		return "> indexNestedLoop" + lop.toString();
	}

}
