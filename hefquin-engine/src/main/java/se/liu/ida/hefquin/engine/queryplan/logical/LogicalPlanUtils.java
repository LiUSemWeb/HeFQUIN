package se.liu.ida.hefquin.engine.queryplan.logical;

import java.util.Arrays;
import java.util.List;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.federation.access.BGPRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;

public class LogicalPlanUtils
{
	/**
	 * Creates a {@link LogicalPlan} with a {@link LogicalOpJoin} as root
	 * operator and the given plans as its two subplans.
	 */
	public static LogicalPlan createPlanWithBinaryJoin( final LogicalPlan subPlan1,
	                                                    final LogicalPlan subPlan2 ) {
		return createPlanWithSubPlans( LogicalOpJoin.getInstance(), subPlan1, subPlan2 );
	}

	/**
	 * Creates a {@link LogicalPlan} with a {@link LogicalOpMultiwayJoin} as
	 * root operator and the given plans as its subplans.
	 */
	public static LogicalPlan createPlanWithMultiwayJoin( final LogicalPlan ... subPlans ) {
		return createPlanWithSubPlans( LogicalOpMultiwayJoin.getInstance(), subPlans );
	}

	/**
	 * Creates a {@link LogicalPlan} with a {@link LogicalOpMultiwayJoin} as
	 * root operator and the given plans as its subplans.
	 */
	public static LogicalPlan createPlanWithMultiwayJoin( final List<LogicalPlan> subPlans ) {
		return createPlanWithSubPlans( LogicalOpMultiwayJoin.getInstance(), subPlans );
	}

	/**
	 * Creates a {@link LogicalPlan} with a {@link LogicalOpUnion} as root
	 * operator and the given plans as its two subplans.
	 */
	public static LogicalPlan createPlanWithBinaryUnion( final LogicalPlan subPlan1,
	                                                     final LogicalPlan subPlan2 ) {
		return createPlanWithSubPlans( LogicalOpUnion.getInstance(), subPlan1, subPlan2 );
	}

	/**
	 * Creates a {@link LogicalPlan} with a {@link LogicalOpMultiwayUnion} as
	 * root operator and the given plans as its subplans.
	 */
	public static LogicalPlan createPlanWithMultiwayUnion( final LogicalPlan ... subPlans ) {
		return createPlanWithSubPlans( LogicalOpMultiwayUnion.getInstance(), subPlans );
	}

	/**
	 * Creates a {@link LogicalPlan} with a {@link LogicalOpMultiwayUnion} as
	 * root operator and the given plans as its subplans.
	 */
	public static LogicalPlan createPlanWithMultiwayUnion( final List<LogicalPlan> subPlans ) {
		return createPlanWithSubPlans( LogicalOpMultiwayUnion.getInstance(), subPlans );
	}

	/**
	 * Creates a {@link LogicalPlan} with the given operator as root operator
	 * and the given plans as subplans.
	 */
	public static LogicalPlan createPlanWithSubPlans( final LogicalOperator rootOp,
	                                                  final LogicalPlan ... subPlans ) {
		return createPlanWithSubPlans( rootOp, Arrays.asList(subPlans) );
	}

	/**
	 * Creates a {@link LogicalPlan} with the given operator as root operator
	 * and the plans given in the list as subplans. If the given operator is
	 * a {@link NullaryLogicalOp}, then the given list may be null.
	 */
	public static LogicalPlan createPlanWithSubPlans( final LogicalOperator rootOp,
	                                                  final List<LogicalPlan> subPlans ) {
		if ( rootOp instanceof NullaryLogicalOp ) {
			assert subPlans == null || subPlans.isEmpty();
			return new LogicalPlanWithNullaryRootImpl( (NullaryLogicalOp) rootOp );
		}
		else if ( rootOp instanceof UnaryLogicalOp ) {
			assert subPlans.size() == 1;
			return new LogicalPlanWithUnaryRootImpl( (UnaryLogicalOp) rootOp, subPlans.get(0) );
		}
		else if ( rootOp instanceof BinaryLogicalOp ) {
			assert subPlans.size() == 2;
			return new LogicalPlanWithBinaryRootImpl( (BinaryLogicalOp) rootOp, subPlans.get(0), subPlans.get(1) );
		}
		else if ( rootOp instanceof NaryLogicalOp ) {
			return new LogicalPlanWithNaryRootImpl( (NaryLogicalOp) rootOp, subPlans );
		}
		else {
			throw new IllegalArgumentException( "unexpected type of logical operator: " + rootOp.getClass().getName() );
		}
	}

	/**
	 * Returns true if the given logical plan is a source assignment.
	 */
	static public boolean isSourceAssignment( final LogicalPlan plan ) {
		final SourceAssignmentChecker v = new SourceAssignmentChecker();
		LogicalPlanWalker.walk(plan, null, v);
		return v.wasSourceAssignment();
	}

	static public int countSubplans( final LogicalPlan plan ) {
		final LogicalPlanCounter c = new LogicalPlanCounter();
		LogicalPlanWalker.walk(plan, null, c);
		return c.getSubplanCount();
	}

	static public class LogicalPlanCounter implements LogicalPlanVisitor {
		protected int subplanCount = 0;

		public int getSubplanCount() {
			return subplanCount;
		}

		@Override
		public void visit( final LogicalOpRequest<?,?> op )  { subplanCount++; }

		@Override
		public void visit( final LogicalOpGPAdd op )         { subplanCount++; }

		@Override
		public void visit( final LogicalOpGPOptAdd op )      { subplanCount++; }

		@Override
		public void visit( final LogicalOpJoin op )          { subplanCount++; }

		@Override
		public void visit( final LogicalOpRightJoin op )     { subplanCount++; }

		@Override
		public void visit( final LogicalOpUnion op )         { subplanCount++; }

		@Override
		public void visit( final LogicalOpMultiwayJoin op )  { subplanCount++; }

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) { subplanCount++; }

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) { subplanCount++; }

		@Override
		public void visit( final LogicalOpFilter op )        { subplanCount++; }

		@Override
		public void visit( final LogicalOpBind op )          { subplanCount++; }

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) { subplanCount++; }

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) { subplanCount++; }
	} // end of class LogicalPlanCounter

	static public class SourceAssignmentChecker extends LogicalPlanVisitorBase {
		protected boolean isSourceAssignment = true;

		public boolean wasSourceAssignment() { return isSourceAssignment; }

		@Override
		public void visit( final LogicalOpRequest<?,?> op ) {
			final DataRetrievalRequest req = op.getRequest();
			if (   !(req instanceof TriplePatternRequest)
				&& !(req instanceof BGPRequest) )
				isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpRightJoin op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			isSourceAssignment = false;
		}
	} // end of class SourceAssignmentChecker

}
