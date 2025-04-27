package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithVALUES;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

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
public class PhysicalOpBindJoinWithVALUES extends BaseForPhysicalOpSingleInputJoin
{
	public PhysicalOpBindJoinWithVALUES( final LogicalOpTPAdd lop ) {
		super(lop);
		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithVALUES( final LogicalOpTPOptAdd lop ) {
		super(lop);
		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithVALUES( final LogicalOpBGPAdd lop ) {
		super(lop);
		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithVALUES( final LogicalOpBGPOptAdd lop ) {
		super(lop);
		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithVALUES( final LogicalOpGPAdd lop ) {
		super(lop);
		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithVALUES( final LogicalOpGPOptAdd lop ) {
		super(lop);
		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinWithVALUES
				&& ((PhysicalOpBindJoinWithVALUES) o).lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final ExpectedVariables... inputVars ) {
		assert inputVars.length == 1;
		if ( ! inputVars[0].getPossibleVariables().isEmpty() ) {
			// The executable operator for this physical operator (i.e., ExecOpBindJoinSPARQLwithVALUES)
			// can work correctly only in cases in which all input solution mappings are for the exact
			// same set of variables. This can be guaranteed only if the set of possible variables from
			// the child operator is empty.
			throw new IllegalArgumentException("Nonempty set of possible variables.");
		}

		final SPARQLGraphPattern pt;
		final FederationMember fm;
		final boolean useOuterJoinSemantics;

		if ( lop instanceof LogicalOpTPAdd tpAdd ) {
			pt = tpAdd.getTP();
			fm = tpAdd.getFederationMember();
			useOuterJoinSemantics = false;
		}
		else if ( lop instanceof LogicalOpTPOptAdd tpOptAdd ) {
			pt = tpOptAdd.getTP();
			fm = tpOptAdd.getFederationMember();
			useOuterJoinSemantics = true;
		}
		else if ( lop instanceof LogicalOpBGPAdd bgpAdd ) {
			pt = bgpAdd.getBGP();
			fm = bgpAdd.getFederationMember();
			useOuterJoinSemantics = false;
		}
		else if ( lop instanceof LogicalOpBGPOptAdd bgpOptAdd ) {
			pt = bgpOptAdd.getBGP();
			fm = bgpOptAdd.getFederationMember();
			useOuterJoinSemantics = true;
		}
		else if ( lop instanceof LogicalOpGPAdd gpAdd ) {
			pt = gpAdd.getPattern();
			fm = gpAdd.getFederationMember();
			useOuterJoinSemantics = false;
		}
		else if ( lop instanceof LogicalOpGPOptAdd gpOptAdd ) {
			pt = gpOptAdd.getPattern();
			fm = gpOptAdd.getFederationMember();
			useOuterJoinSemantics = true;
		}
		else {
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
		}

		return createExecOp(pt, fm, useOuterJoinSemantics, collectExceptions);
	}

	protected UnaryExecutableOp createExecOp( final SPARQLGraphPattern pattern,
	                                          final FederationMember fm,
	                                          final boolean useOuterJoinSemantics,
	                                          final boolean collectExceptions ) {
		if ( fm instanceof SPARQLEndpoint )
			return new ExecOpBindJoinSPARQLwithVALUES( pattern, (SPARQLEndpoint) fm, useOuterJoinSemantics, collectExceptions );
		else
			throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {

		return "> VALUESBindJoin" + lop.toString();
	}

}
