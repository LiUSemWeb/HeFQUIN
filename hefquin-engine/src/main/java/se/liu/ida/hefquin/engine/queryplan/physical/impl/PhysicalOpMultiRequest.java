package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpMultiRequest;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOpForLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that performs requests with the same graph
 * pattern at multiple federation members in parallel and, then,
 * outputs the solution mappings obtained via all these requests,
 * where each solution mapping is extended with a binding that
 * captures the service variable with the service URI of the
 * federation member from which it was obtained.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpMultiRequest} class.
 */
public class PhysicalOpMultiRequest implements NullaryPhysicalOpForLogicalOp
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	protected final LogicalOpMultiRequest lop;

	protected PhysicalOpMultiRequest( final LogicalOpMultiRequest lop ) {
		assert lop != null;
		this.lop = lop;
	}

	@Override
	public LogicalOpMultiRequest getLogicalOperator() {
		return lop;
	}

	@Override
	public NullaryExecutableOp createExecOp( final boolean collectExceptions,
	                                         final QueryPlanningInfo qpInfo,
	                                         final ExpectedVariables ... inputVars ) {
		return new ExecOpMultiRequest( lop.getRequest(),
		                               lop.getServiceVariable(),
		                               lop.getFederationMembers(),
		                               collectExceptions,
		                               qpInfo );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return    o instanceof PhysicalOpMultiRequest oo
		       && oo.lop.equals(lop);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ lop.hashCode();
	}

	@Override
	public String toString() {
		return lop.toString();
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			return ( lop instanceof LogicalOpMultiRequest );
		}

		@Override
		public PhysicalOpMultiRequest create( final NullaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpMultiRequest op ) {
				return new PhysicalOpMultiRequest(op);
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

}
