package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithUNION;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;

public class PhysicalOpBindJoinWithUNION extends BasePhysicalOpSingleInputJoin {

	public PhysicalOpBindJoinWithUNION( final UnaryLogicalOp lop) {
		super(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp(final ExpectedVariables... inputVars) {
		for (final ExpectedVariables ev : inputVars) {
			System.out.println(ev.getCertainVariables());
			if ( ! ev.getPossibleVariables().isEmpty() ) {
				throw new IllegalArgumentException("Illegal child operator for tpAdd. Possible variables should be empty.");
			}
		}
		if ( lop instanceof LogicalOpTPAdd ) {
			final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
			final FederationMember fm = tpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint )
				return new ExecOpBindJoinSPARQLwithUNION( tpAdd.getTP(), (SPARQLEndpoint) fm );
			else
				throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
		}
		else
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
	}

}
