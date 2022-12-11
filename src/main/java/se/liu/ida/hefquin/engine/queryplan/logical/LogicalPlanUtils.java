package se.liu.ida.hefquin.engine.queryplan.logical;

import java.util.List;

import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithNullaryRootImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithUnaryRootImpl;

public class LogicalPlanUtils
{
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
		public void visit( final LogicalOpTPAdd op )         { subplanCount++; }

		@Override
		public void visit( final LogicalOpBGPAdd op )        { subplanCount++; }

		@Override
		public void visit( final LogicalOpTPOptAdd op )      { subplanCount++; }

		@Override
		public void visit( final LogicalOpBGPOptAdd op )     { subplanCount++; }

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
		public void visit( final LogicalOpTPAdd op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpBGPAdd op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpTPOptAdd op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpBGPOptAdd op ) {
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
