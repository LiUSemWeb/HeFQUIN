package se.liu.ida.hefquin.queryplan.physical.impl;

import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.ExecOpBindJoinSPARQL;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.queryplan.physical.UnaryPhysicalOpForLogicalOp;

public class PhysicalOpBindJoin implements UnaryPhysicalOpForLogicalOp
{
	protected final UnaryLogicalOp lop;

	/**
	 * The given logical operator is expected to be of one of the following
	 * two types: {@link LogicalOpTPAdd} or {@link LogicalOpBGPAdd}.
	 */
	public PhysicalOpBindJoin(final UnaryLogicalOp lop ) {
		assert lop != null;
		assert (lop instanceof LogicalOpBGPAdd) || (lop instanceof LogicalOpTPAdd);
		this.lop = lop;
	}

	@Override
	public UnaryLogicalOp getLogicalOperator() { return lop; }

	@Override
	public UnaryExecutableOp createExecOp()
	{
		if ( lop instanceof LogicalOpTPAdd ) {
			final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
			final FederationMember fm = tpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint  )
				// tpAdd.getTP()?
				return new ExecOpBindJoinSPARQL( tpAdd.getTP(), (SPARQLEndpoint) fm );
			else
				throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );

		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			final LogicalOpBGPAdd bgpAdd = (LogicalOpBGPAdd) lop;
			final FederationMember fm = bgpAdd.getFederationMember();

			if ( fm instanceof SPARQLEndpoint )
				return new ExecOpBindJoinSPARQL( bgpAdd.getBGP(), (SPARQLEndpoint) fm );
			else
				throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
		}
		else
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
	}

}
