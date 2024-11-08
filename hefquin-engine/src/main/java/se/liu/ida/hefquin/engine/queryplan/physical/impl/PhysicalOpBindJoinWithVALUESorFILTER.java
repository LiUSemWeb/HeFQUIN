package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithVALUESorFILTER;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
* A physical operator that implements (a batching version of) the bind
 * join algorithm. It starts by using a VALUES clause to capture the potential join
 * partners that are sent to the federation member. If this fails, it uses the bind join
 * algorithm with a FILTER clause instead.

 * <p>
 * <b>Algorithm description:</b> For a detailed description of the
 * actual algorithm associated with this physical operator, refer
 * to {@link ExecOpBindJoinSPARQLwithVALUESorFILTER}, which provides the
 * implementation of this algorithm.
 * </p>
 */
public class PhysicalOpBindJoinWithVALUESorFILTER extends BaseForPhysicalOpSingleInputJoin
{
	public PhysicalOpBindJoinWithVALUESorFILTER( final LogicalOpTPAdd lop ) {
		super(lop);
		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}
	public PhysicalOpBindJoinWithVALUESorFILTER( final LogicalOpBGPAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	public PhysicalOpBindJoinWithVALUESorFILTER( final LogicalOpGPAdd lop ) {
		super(lop);

		assert lop.getFederationMember() instanceof SPARQLEndpoint;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof PhysicalOpBindJoinWithVALUESorFILTER
				&& ((PhysicalOpBindJoinWithVALUESorFILTER) o).lop.equals(lop);
	}

	@Override
	public UnaryExecutableOp createExecOp( final boolean collectExceptions,
	                                       final ExpectedVariables... inputVars ) {
		final TriplePattern pt;
		final FederationMember fm;

		if ( lop instanceof LogicalOpTPAdd ) {
			pt = ( (LogicalOpTPAdd) lop ).getTP();
			fm = ( (LogicalOpTPAdd) lop ).getFederationMember();
		}
		else {
			throw new IllegalArgumentException("Unsupported type of operator: " + lop.getClass().getName() );
		}

		return createExecOp(pt, fm, collectExceptions);
	}
	
	
	protected UnaryExecutableOp createExecOp( final TriplePattern pattern,
	                                          final FederationMember fm,
	                                          final boolean collectExceptions ) {
		if ( fm instanceof SPARQLEndpoint )
			return new ExecOpBindJoinSPARQLwithVALUESorFILTER(pattern, (SPARQLEndpoint) fm, collectExceptions );
		else
			throw new IllegalArgumentException("Unsupported type of federation member: " + fm.getClass().getName() );
	}

	@Override
	public void visit(final PhysicalPlanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return "> VALUESorFILTERBindJoin" + lop.toString();
	}

}
