package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpHashJoin;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

/**
 * A physical operator that implements the hash join algorithm to perform
 * a join of two sequences of input solution mappings (produced by the two
 * sub-plans under this operator). The algorithm builds a hash table with
 * the solution mappings of the second input sequence (using the values
 * that they have for the join variables to decide where to place them in
 * the hash table) and, thereafter, probes the hash table to find join
 * partners for each of the solution mappings of the first input sequence.
 *
 * The actual algorithm of this operator is implemented in the
 * {@link ExecOpHashJoin2} class.
 */
public class PhysicalOpHashJoin2 extends BaseForPhysicalOpBinaryJoin
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	private static PhysicalOpHashJoin2 singletonForInnerJoin = null;
	private static PhysicalOpHashJoin2 singletonForOuterJoin = null;

	protected PhysicalOpHashJoin2( final boolean useOuterJoinSemantics ) {
		super(useOuterJoinSemantics);
	}

	@Override
	public BinaryExecutableOp createExecOp( final boolean collectExceptions,
	                                        final QueryPlanningInfo qpInfo,
	                                        final ExpectedVariables ... inputVars ) {
		assert inputVars.length == 2;

		return new ExecOpHashJoin( inputVars[0], inputVars[1], collectExceptions, qpInfo );
	}

	@Override
	public void visit( final PhysicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return o instanceof PhysicalOpHashJoin;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ getLogicalOperator().hashCode();
	}

	@Override
	public String toString() {
		return "hash join 2";
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			if (    ! (lop instanceof LogicalOpJoin)
			     && ! (lop instanceof LogicalOpLeftJoin) )
				return false;

			// inputVars contains null value?
			for ( final ExpectedVariables vars : inputVars ) {
				if ( vars == null ) return false;
			}

			final Set<Var> joinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars);
			return ( joinVars != null && ! joinVars.isEmpty() );
		}

		@Override
		public PhysicalOpHashJoin2 create( final BinaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpJoin ) {
				return getInstanceForInnerJoin();
			}

			if ( lop instanceof LogicalOpLeftJoin ) {
				return getInstanceForOuterJoin();
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

	public static PhysicalOpHashJoin2 getInstanceForInnerJoin() {
		if ( singletonForInnerJoin == null )
			singletonForInnerJoin = new PhysicalOpHashJoin2(false);

		return singletonForInnerJoin;
	}

	public static PhysicalOpHashJoin2 getInstanceForOuterJoin() {
		if ( singletonForOuterJoin == null )
			singletonForOuterJoin = new PhysicalOpHashJoin2(true);

		return singletonForOuterJoin;
	}
}
