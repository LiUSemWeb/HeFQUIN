package se.liu.ida.hefquin.engine.queryproc.impl.optimizer;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.NaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class LogicalToPhysicalPlanConverterImpl implements LogicalToPhysicalPlanConverter
{
	@Override
	public PhysicalPlan convert( final LogicalPlan lp, final boolean keepMultiwayJoins ) {
		final List<PhysicalPlan> children = convertChildren(lp, keepMultiwayJoins);
		return createPhysicalPlan( lp.getRootOperator(), children, keepMultiwayJoins );
	}

	/**
	 * Converts the sub-plans of the given logical plan (if any) and returns
	 * a list of the resulting physical plans. The order of the physical plans
	 * in the returned list is such that the i-th physical plan in the list
	 * is the physical plan that has been created for the i-th sub-plan of
	 * the given logical plan. For logical plans that do not contain any
	 * sub-plans, an empty list is returned.
	 */
	protected List<PhysicalPlan> convertChildren( final LogicalPlan lp, final boolean keepMultiwayJoins ) {
		final List<PhysicalPlan> children = new ArrayList<PhysicalPlan>();
		final int numChildren = lp.numberOfSubPlans();
		if ( numChildren > 0 ) {
			for ( int i = 0; i < numChildren; ++i ) {
				children.add( convert(lp.getSubPlan(i), keepMultiwayJoins) );
			}
		}
		return children;
	}

	protected PhysicalPlan createPhysicalPlan( final LogicalOperator lop,
	                                           final List<PhysicalPlan> children,
	                                           final boolean keepMultiwayJoins )
	{
		if ( lop instanceof NullaryLogicalOp ) {
			if ( children.size() != 0 )
				throw new IllegalArgumentException( "unexpected number of sub-plans: " + children.size() );

			return createPhysicalPlanWithNullaryRoot( (NullaryLogicalOp) lop );
		}
		else if ( lop instanceof UnaryLogicalOp ) {
			if ( children.size() != 1 )
				throw new IllegalArgumentException( "unexpected number of sub-plans: " + children.size() );

			return createPhysicalPlanWithUnaryRoot( (UnaryLogicalOp) lop, children.get(0) );
		}
		else if ( lop instanceof BinaryLogicalOp ) {
			if ( children.size() != 2 )
				throw new IllegalArgumentException( "unexpected number of sub-plans: " + children.size() );

			return createPhysicalPlanWithBinaryRoot( (BinaryLogicalOp) lop, children.get(0), children.get(1) );
		}
		else if ( lop instanceof NaryLogicalOp ) {
			if ( children.size() < 1 )
				throw new IllegalArgumentException( "unexpected number of sub-plans: " + children.size() );

			return createPhysicalPlanWithNaryRoot( (NaryLogicalOp) lop, children, keepMultiwayJoins );
		}
		else {
			throw new IllegalArgumentException( "unknown logical operator: " + lop.getClass().getName() );
		}
	}

	protected PhysicalPlan createPhysicalPlanWithNullaryRoot( final NullaryLogicalOp lop ) {
		final NullaryPhysicalOp pop;
		if ( lop instanceof LogicalOpRequest<?,?> ) {
			pop = convertRequest( (LogicalOpRequest<?,?>) lop );
		}
		else {
			throw new IllegalArgumentException( "unknown logical operator: " + lop.getClass().getName() );
		}

		return new PhysicalPlanWithNullaryRootImpl(pop);
	}

	protected PhysicalPlan createPhysicalPlanWithUnaryRoot( final UnaryLogicalOp lop, final PhysicalPlan child ) {
		final UnaryPhysicalOp pop;
		if ( lop instanceof LogicalOpTPAdd ) {
			pop = convertTPAdd( (LogicalOpTPAdd) lop );
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			pop = convertBGPAdd( (LogicalOpBGPAdd) lop );
		}
		else {
			throw new IllegalArgumentException( "unknown logical operator: " + lop.getClass().getName() );
		}

		return new PhysicalPlanWithUnaryRootImpl(pop, child);
	}

	protected PhysicalPlan createPhysicalPlanWithBinaryRoot( final BinaryLogicalOp lop, final PhysicalPlan child1, final PhysicalPlan child2 ) {
		final BinaryPhysicalOp pop;
		if ( lop instanceof LogicalOpJoin ) {
			pop = convertJoin( (LogicalOpJoin) lop );
		}
		else if ( lop instanceof LogicalOpUnion ) {
			pop = convertUnion( (LogicalOpUnion) lop );
		}
		else {
			throw new IllegalArgumentException( "unknown logical operator: " + lop.getClass().getName() );
		}

		return new PhysicalPlanWithBinaryRootImpl(pop, child1, child2);
	}

	protected PhysicalPlan createPhysicalPlanWithNaryRoot( final NaryLogicalOp lop,
	                                                       final List<PhysicalPlan> children,
	                                                       final boolean keepMultiwayJoins )
	{
		if ( lop instanceof LogicalOpMultiwayJoin ) {
			return createPhysicalPlanForMultiwayJoin( (LogicalOpMultiwayJoin) lop, children, keepMultiwayJoins );
		}
		else if ( lop instanceof LogicalOpMultiwayUnion ) {
			return createPhysicalPlanForMultiwayUnion( (LogicalOpMultiwayUnion) lop, children );
		}
		else {
			throw new IllegalArgumentException( "unknown logical operator: " + lop.getClass().getName() );
		}
	}

	protected PhysicalPlan createPhysicalPlanForMultiwayJoin( final LogicalOpMultiwayJoin lop,
	                                                          final List<PhysicalPlan> children,
	                                                          final boolean keepMultiwayJoins )
	{
		if ( children.size() == 1 ) {
			return children.get(0);
		}

		if ( keepMultiwayJoins ) {
			final NaryPhysicalOp pop = new BasePhysicalOpMultiwayJoin(lop) {
				@Override public void visit(PhysicalPlanVisitor visitor) { throw new UnsupportedOperationException(); }
				@Override public NaryExecutableOp createExecOp(ExpectedVariables... inputVars) { throw new UnsupportedOperationException(); }
			};
			return new PhysicalPlanWithNaryRootImpl(pop, children);
		}

		// As long as we do not have an actual algorithm for multiway joins,
		// we simply convert this to a left-deep plan of binary joins.
		PhysicalPlan currentSubPlan = children.get(0);
		for ( int i = 1; i < children.size(); ++i ) {
			final LogicalOpJoin newRootOfLogicalPlan = new LogicalOpJoin();
			currentSubPlan = createPhysicalPlanWithBinaryRoot( newRootOfLogicalPlan, currentSubPlan, children.get(i) );
		}

		return currentSubPlan;
	}

	protected PhysicalPlan createPhysicalPlanForMultiwayUnion( final LogicalOpMultiwayUnion lop, final List<PhysicalPlan> children ) {
		if ( children.size() == 1 ) {
			return children.get(0);
		}

		// As long as we do not have an actual algorithm for multiway unions,
		// we simply convert this to a left-deep plan of binary unions.
		PhysicalPlan currentSubPlan = children.get(0);
		for ( int i = 1; i < children.size(); ++i ) {
			final LogicalOpUnion newRootOfLogicalPlan = new LogicalOpUnion();
			currentSubPlan = createPhysicalPlanWithBinaryRoot( newRootOfLogicalPlan, currentSubPlan, children.get(i) );
		}

		return currentSubPlan;
	}

	protected NullaryPhysicalOp convertRequest( final LogicalOpRequest<?,?> lop ) {
		return new PhysicalOpRequest<>(lop);
	}

	protected UnaryPhysicalOp convertTPAdd( final LogicalOpTPAdd lop ) {
		return new PhysicalOpBindJoin(lop);
	}

	protected UnaryPhysicalOp convertBGPAdd( final LogicalOpBGPAdd lop ) {
		return new PhysicalOpBindJoin(lop);
	}

	protected BinaryPhysicalOp convertJoin( final LogicalOpJoin lop ) {
		return new PhysicalOpSymmetricHashJoin(lop);
	}

	protected BinaryPhysicalOp convertUnion( final LogicalOpUnion lop ) {
		return new PhysicalOpBinaryUnion(lop);
	}

}
