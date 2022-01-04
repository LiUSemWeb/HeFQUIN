package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

public class PhysicalOpBindJoin extends BasePhysicalOpSingleInputJoin
{
    public PhysicalOpBindJoin( final LogicalOpTPAdd lop ) {
        super(lop);
    }

    // not supported at the moment
    //public PhysicalOpBindJoin( final LogicalOpBGPAdd lop ) {
    //    super(lop);
    //}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoin && ((PhysicalOpBindJoin) o).lop.equals(lop);
	}

    @Override
    public UnaryExecutableOp createExecOp( final ExpectedVariables ... inputVars )
    {
        if ( lop instanceof LogicalOpTPAdd ) {
            final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
            final FederationMember fm = tpAdd.getFederationMember();

            if ( fm instanceof BRTPFServer )
                return new ExecOpBindJoinBRTPF( tpAdd.getTP(), (BRTPFServer) fm );
                //else if ( fm instanceof SPARQLEndpoint)
                //	return new ExecOpBindJoinSPARQL( tpAdd.getTP(), (SPARQLEndpoint) fm );
            else
                throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );

        }
        else if ( lop instanceof LogicalOpBGPAdd ) {
            final LogicalOpBGPAdd bgpAdd = (LogicalOpBGPAdd) lop;
            final FederationMember fm = bgpAdd.getFederationMember();

            //if ( fm instanceof SPARQLEndpoint )
            //	return new ExecOpBindJoinSPARQL( bgpAdd.getBGP(), (SPARQLEndpoint) fm );
            //else
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

        return "> bindJoin" + lop.toString();
    }

}