package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSymmetricHashJoin;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements the symmetric hash join algorithm
 * to perform an inner join of two sequences of input solution mappings
 * (produced by the two sub-plans under this operator).
 *
 * For a more detailed description of the algorithm, refer to {@link
 * ExecOpSymmetricHashJoin}, which provides the implementation of this
 * algorithm.
 */
public class PhysicalOpSymmetricHashJoin extends BaseForPhysicalOpBinaryJoin
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	private static PhysicalOpSymmetricHashJoin singleton = null;

	protected PhysicalOpSymmetricHashJoin() { }

	@Override
	public BinaryExecutableOp createExecOp( final boolean collectExceptions,
	                                        final QueryPlanningInfo qpInfo,
	                                        final ExpectedVariables ... inputVars ) {
		assert inputVars.length == 2;

		return new ExecOpSymmetricHashJoin( inputVars[0], inputVars[1], collectExceptions, qpInfo );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof PhysicalOpSymmetricHashJoin;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ getLogicalOperator().hashCode();
	}

	@Override
	public String toString() {
		return "SHJ";
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			// Generally, binary joins and multiway joins are okay, ...
			if (    ! (lop instanceof LogicalOpJoin)
			     && ! (lop instanceof LogicalOpMultiwayJoin) )
				return false;

			// ... but in the case of a multiway join, it must still be binary.
			if ( inputVars.length != 2 )
				return false;

			// Now we also need to check that there is at least one join
			// variable, for which we first need to ensure that the given
			// inputVars contain no null value.
			for ( final ExpectedVariables vars : inputVars ) {
				if ( vars == null ) return false;
			}

			// Determine the set of join variables ...
			final Set<Var> joinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars);
			// ... and check that it is not empty. 
			return ( ! joinVars.isEmpty() );
		}

		@Override
		public PhysicalOpSymmetricHashJoin create( final BinaryLogicalOp lop ) {
			if (    lop instanceof LogicalOpJoin
			     || lop instanceof LogicalOpMultiwayJoin ) {
				return getInstance();
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

	public static PhysicalOpSymmetricHashJoin getInstance() {
		if ( singleton == null ) singleton = new PhysicalOpSymmetricHashJoin();

		return singleton;
	}
}
