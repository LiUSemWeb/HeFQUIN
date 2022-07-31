package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpIndexNestedLoopsJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpIndexNestedLoopsJoinSPARQL;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpIndexNestedLoopsJoinTPF;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

public class PhysicalOpIndexNestedLoopsJoin extends BasePhysicalOpSingleInputJoin
{
	protected static final boolean useOuterJoinSemantics = false;

	public PhysicalOpIndexNestedLoopsJoin( final LogicalOpTPAdd lop ) {
		super(lop);
	}

	public PhysicalOpIndexNestedLoopsJoin( final LogicalOpBGPAdd lop ) {
		super(lop);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpIndexNestedLoopsJoin
				&& ((PhysicalOpIndexNestedLoopsJoin) o).lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final ExpectedVariables ... inputVars )
	{
		if ( lop instanceof LogicalOpTPAdd ) {
			final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
			final FederationMember fm = tpAdd.getFederationMember();

			if ( fm instanceof TPFServer )
				return new ExecOpIndexNestedLoopsJoinTPF( tpAdd.getTP(), (TPFServer) fm, useOuterJoinSemantics );
			else if ( fm instanceof BRTPFServer )
				return new ExecOpIndexNestedLoopsJoinBRTPF( tpAdd.getTP(), (BRTPFServer) fm, useOuterJoinSemantics );
			else if ( fm instanceof SPARQLEndpoint )
				return new ExecOpIndexNestedLoopsJoinSPARQL( tpAdd.getTP(), (SPARQLEndpoint) fm );
			else
				throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			final LogicalOpBGPAdd bgpAdd = (LogicalOpBGPAdd) lop;
			final FederationMember fm = bgpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint )
				return new ExecOpIndexNestedLoopsJoinSPARQL( bgpAdd.getBGP(), (SPARQLEndpoint) fm );
			else
				throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
		}
		else
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
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
