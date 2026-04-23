package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpHashBasedMinus;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMinus;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOpFactory;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;

public class PhysicalOpMinus extends BaseForPhysicalOpBinaryJoin
{
	protected static final Factory factory = new Factory();
	public static PhysicalOpFactory getFactory() { return factory; }

	private static PhysicalOpMinus singletonWithoutReduction = null;
	private static PhysicalOpMinus singletonThatMayReduce = null;

	protected PhysicalOpMinus( final boolean mayReduce ) {
		super(true, mayReduce);
	}

	@Override
	public BinaryExecutableOp createExecOp( final boolean collectExceptions,
	                                        final QueryPlanningInfo qpInfo,
	                                        final ExpectedVariables ... inputVars ) {
		assert inputVars.length == 2;

		return new ExecOpHashBasedMinus( mayReduce,
		                                 inputVars[0], inputVars[1],
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

		return o instanceof PhysicalOpMinus oo
		    && oo.mayReduce == mayReduce;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ (mayReduce ? 1 : 0);
	}

	@Override
	public String toString() {
		return "minus";
	}

	public static class Factory implements PhysicalOpFactory
	{
		@Override
		public boolean supports( final LogicalOperator lop, final ExpectedVariables... inputVars ) {
			if ( ! (lop instanceof LogicalOpMinus) )
				return false;

			// inputVars contains null value?
			for ( final ExpectedVariables vars : inputVars ) {
				if ( vars == null ) return false;
			}

			final Set<Var> joinVars = ExpectedVariablesUtils.intersectionOfCertainVariables(inputVars);
			return ( joinVars != null && ! joinVars.isEmpty() );
		}

		@Override
		public PhysicalOpMinus create( final BinaryLogicalOp lop ) {
			if ( lop instanceof LogicalOpMinus ) {
				return getInstance(lop.mayReduce());
			}

			throw new UnsupportedOperationException( "Unsupported type of logical operator: " + lop.getClass().getName() + "." );
		}
	}

	public static PhysicalOpMinus getInstance(final boolean mayReduce) {
		if ( mayReduce ) {
			if ( singletonThatMayReduce == null )
				singletonThatMayReduce = new PhysicalOpMinus(true);
			return singletonThatMayReduce;
		}
		else {
			if ( singletonWithoutReduction == null )
				singletonWithoutReduction = new PhysicalOpMinus(false);
			return singletonWithoutReduction;
		}
	}
}
