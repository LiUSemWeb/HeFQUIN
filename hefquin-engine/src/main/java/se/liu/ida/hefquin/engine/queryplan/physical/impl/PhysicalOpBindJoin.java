package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements (a batching version of) the bind
 * join algorithm for cases in which the federation member accessed by
 * the algorithm supports the brTPF interface.
 *
 * <p>
 * <b>Semantics:</b> This operator implements the logical operators tpAdd
 * (see {@link LogicalOpTPAdd}) and tpOptAdd (see {@link LogicalOpTPOptAdd}).
 * That is, for a given triple pattern, a federation  member, and an input
 * sequence of solution mappings (produced by the sub-plan under this
 * operator), the operator produces the solutions resulting from the join
 * (inner or left outer) between the input solutions and the solutions of
 * evaluating the given triple pattern over the data of the federation
 * member.
 * </p>
 *
 * <p>
 * <b>Algorithm description:</b> For a detailed description of the
 * actual algorithm associated with this physical operator, refer
 * to {@link ExecOpBindJoinBRTPF}, which provides the
 * implementation of this algorithm.
 * </p>
 */
public class PhysicalOpBindJoin extends BaseForPhysicalOpSingleInputJoin
{
    public PhysicalOpBindJoin( final LogicalOpTPAdd lop ) {
        super(lop);
    }

    public PhysicalOpBindJoin( final LogicalOpTPOptAdd lop ) {
        super(lop);
    }

    // not supported at the moment
    //public PhysicalOpBindJoin( final LogicalOpBGPAdd lop ) {
    //    super(lop);
    //}
    //
    //public PhysicalOpBindJoin( final LogicalOpBGPOptAdd lop ) {
    //    super(lop);
    //}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoin && ((PhysicalOpBindJoin) o).lop.equals(lop);
	}

    @Override
    public UnaryExecutableOp createExecOp( final boolean collectExceptions,
                                           final ExpectedVariables ... inputVars )
    {
        if ( lop instanceof LogicalOpTPAdd ) {
            final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) lop;
            final FederationMember fm = tpAdd.getFederationMember();
            final boolean useOuterJoinSemantics = false;

            if ( fm instanceof BRTPFServer )
                return new ExecOpBindJoinBRTPF( tpAdd.getTP(), (BRTPFServer) fm, useOuterJoinSemantics, collectExceptions );
            else
                throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );

        }
        else if ( lop instanceof LogicalOpTPOptAdd ) {
            final LogicalOpTPOptAdd tpAdd = (LogicalOpTPOptAdd) lop;
            final FederationMember fm = tpAdd.getFederationMember();
            final boolean useOuterJoinSemantics = true;

            if ( fm instanceof BRTPFServer )
                return new ExecOpBindJoinBRTPF( tpAdd.getTP(), (BRTPFServer) fm, useOuterJoinSemantics, collectExceptions );
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