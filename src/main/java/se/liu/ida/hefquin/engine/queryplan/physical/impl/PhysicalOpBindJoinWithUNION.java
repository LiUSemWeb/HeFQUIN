package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithUNION;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

public class PhysicalOpBindJoinWithUNION extends BasePhysicalOpSingleInputJoin
{
	public PhysicalOpBindJoinWithUNION( final LogicalOpTPAdd lop) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithUNION( final LogicalOpBGPAdd lop) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithUNION( final LogicalOpGPAdd lop) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinWithUNION
				&& ((PhysicalOpBindJoinWithUNION) o).lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final ExpectedVariables... inputVars ) {
		assert  inputVars.length == 1;
		if (! inputVars[0].getPossibleVariables().isEmpty()){
			// The executable operator for this physical operator (i.e., ExecOpBindJoinSPARQLwithUNION)
			// can work correctly only in cases in which all input solution mappings are for the exact
			// same set of variables. This can be guaranteed only if the set of possible variables from
			// the child operator is empty.
			throw new IllegalArgumentException("Nonempty set of possible variables.");
		}
		if ( lop instanceof LogicalOpTPAdd ) {
			final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
			final FederationMember fm = tpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint )
				return new ExecOpBindJoinSPARQLwithUNION( tpAdd.getTP(), (SPARQLEndpoint) fm, collectExceptions );
			else
				throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			final LogicalOpBGPAdd bgpAdd = (LogicalOpBGPAdd) lop;
			final FederationMember fm = bgpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint )
				return new ExecOpBindJoinSPARQLwithUNION( bgpAdd.getBGP(), (SPARQLEndpoint) fm, collectExceptions );
			else
				throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
		}
		else if ( lop instanceof LogicalOpGPAdd ) {
			final LogicalOpGPAdd gpAdd = (LogicalOpGPAdd) lop;
			final FederationMember fm = gpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint )
				return new ExecOpBindJoinSPARQLwithUNION( gpAdd.getPattern(), (SPARQLEndpoint) fm, collectExceptions );
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
	public String toString() {

		return "> UNIONBindJoin" + lop.toString();
	}

}
