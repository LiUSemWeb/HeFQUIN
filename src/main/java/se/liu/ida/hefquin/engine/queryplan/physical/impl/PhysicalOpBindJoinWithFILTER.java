package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithFILTER;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

public class PhysicalOpBindJoinWithFILTER extends BasePhysicalOpSingleInputJoin
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

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinWithFILTER
				&& ((PhysicalOpBindJoinWithFILTER) o).lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final ExpectedVariables... inputVars ) {
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
		else {
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
		}

		if ( fm instanceof SPARQLEndpoint )
			return new ExecOpBindJoinSPARQLwithFILTER( pt, (SPARQLEndpoint) fm, useOuterJoinSemantics );
		else
			throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {

		return "> FILTERBindJoin" +  lop.toString();
	}

}
