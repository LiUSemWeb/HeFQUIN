package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithFILTER;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements (a batching version of) the bind
 * join algorithm using FILTERs to capture the potential join partners
 * that are sent to the federation member.
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
 * to {@link ExecOpBindJoinSPARQLwithFILTER}, which provides the
 * implementation of this algorithm.
 * </p>
 */
public class PhysicalOpBindJoinWithFILTER extends BaseForPhysicalOpSingleInputJoin
{
	public PhysicalOpBindJoinWithFILTER( final LogicalOpTPAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithFILTER( final LogicalOpTPOptAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithFILTER( final LogicalOpBGPAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithFILTER( final LogicalOpBGPOptAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithFILTER( final LogicalOpGPAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithFILTER( final LogicalOpGPOptAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinWithFILTER
				&& ((PhysicalOpBindJoinWithFILTER) o).lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final ExpectedVariables... inputVars ) {
		final SPARQLGraphPattern pt;
		final FederationMember fm;
		final boolean useOuterJoinSemantics;

		if ( lop instanceof LogicalOpTPAdd ) {
			pt = ( (LogicalOpTPAdd) lop ).getTP();
			fm = ( (LogicalOpTPAdd) lop ).getFederationMember();
			useOuterJoinSemantics = false;
		}
		else if ( lop instanceof LogicalOpTPOptAdd ) {
			pt = ( (LogicalOpTPOptAdd) lop ).getTP();
			fm = ( (LogicalOpTPOptAdd) lop ).getFederationMember();
			useOuterJoinSemantics = true;
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			pt = ( (LogicalOpBGPAdd) lop ).getBGP();
			fm = ( (LogicalOpBGPAdd) lop ).getFederationMember();
			useOuterJoinSemantics = false;
		}
		else if ( lop instanceof LogicalOpBGPOptAdd ) {
			pt = ( (LogicalOpBGPOptAdd) lop ).getBGP();
			fm = ( (LogicalOpBGPOptAdd) lop ).getFederationMember();
			useOuterJoinSemantics = true;
		}
		else if ( lop instanceof LogicalOpGPAdd ) {
			pt = ( (LogicalOpGPAdd) lop ).getPattern();
			fm = ( (LogicalOpGPAdd) lop ).getFederationMember();
			useOuterJoinSemantics = false;
		}
		else if ( lop instanceof LogicalOpGPOptAdd ) {
			pt = ( (LogicalOpGPOptAdd) lop ).getPattern();
			fm = ( (LogicalOpGPOptAdd) lop ).getFederationMember();
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
			return new ExecOpBindJoinSPARQLwithFILTER( pattern, (SPARQLEndpoint) fm, useOuterJoinSemantics, collectExceptions );
		else
			throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {

		return "> FILTERBindJoin " + "(" + getID() + ") " +  lop.toString();
	}

}
