package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements (a batching version of) the bind join
 * algorithm for cases in which the federation member accessed by the algorithm
 * supports the brTPF interface.
 *
 * For a given graph pattern, a federation member, and a (single) input
 * sequence of solution mappings (produced by the sub-plan under this
 * operator), the operator produces the solution mappings resulting from
 * the join between the solutions of evaluating the graph pattern over
 * the data of the federation member and the input solutions. To this end,
 * for every batch of solutions from the input, the algorithm sends a request
 * to the federation member, where this request consists of the graph pattern
 * and the solutions from the current input batch. The response to such a
 * request is the subset of the solutions for the graph pattern that are
 * compatible with at least one of the solutions that were attached to the
 * request. After receiving such a response, the algorithm joins (locally)
 * the solutions from the response with the solutions in the batch used for
 * making the request, and then outputs the resulting joined solutions (if
 * any). Thereafter, the algorithm moves on to the next batch of solutions
 * from the input.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpBindJoinBRTPF} class.
 */
public class PhysicalOpBindJoin extends BasePhysicalOpSingleInputJoin
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