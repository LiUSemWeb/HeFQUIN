package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithFILTER;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithVALUES;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;

/**
 * A physical operator that implements (a batching version of) the bind
 * join algorithm using a VALUES clause to capture the potential join
 * partners that are sent to the federation member.
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
 * to {@link ExecOpBindJoinSPARQLwithVALUES}, which provides the
 * implementation of this algorithm.
 * </p>
 */
public class PhysicalOpBindJoinWithVALUES extends BaseForPhysicalOpSingleInputJoinAtSPARQLEndpoint
{
	public PhysicalOpBindJoinWithVALUES( final LogicalOpGPAdd lop ) {
		super(lop);
	}

	public PhysicalOpBindJoinWithVALUES( final LogicalOpGPOptAdd lop ) {
		super(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final SPARQLGraphPattern pattern,
	                                       final SPARQLEndpoint sparqlEndpoint,
	                                       final boolean useOuterJoinSemantics,
	                                       final boolean collectExceptions,
	                                       final ExpectedVariables... inputVars ) {
		return new ExecOpBindJoinSPARQLwithFILTER( pattern,
		                                           sparqlEndpoint,
		                                           inputVars[0],
		                                           useOuterJoinSemantics,
		                                           ExecOpBindJoinSPARQLwithVALUES.DEFAULT_BATCH_SIZE,
		                                           collectExceptions );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinWithVALUES
				&& ((PhysicalOpBindJoinWithVALUES) o).lop.equals(lop);
	}

	@Override
	public String toString() {
		return "> VALUESBindJoin" + lop.toString();
	}

}
