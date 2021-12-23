package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

public class LogicalPlanUtils
{
	/**
	 * Returns true if the given logical plan is a source assignment.
	 */
	static public boolean isSourceAssignment( final LogicalPlan plan ) {
		final SourceAssignmentChecker v = new SourceAssignmentChecker();
		LogicalPlanWalker.walk(plan, null, v);
		return v.wasSourceAssignment();
	}
	
	static public int countSubplans ( final LogicalPlan plan ) {
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
		public void visit( final LogicalOpRequest<?,?> op )        {
			subplanCount++;
		}

		@Override
		public void visit( final LogicalOpTPAdd op )             {
			subplanCount++;
		}

		@Override
		public void visit( final LogicalOpBGPAdd op )            {
			subplanCount++;
		}

		@Override
		public void visit( final LogicalOpJoin op )              {
			subplanCount++;
		}

		@Override
		public void visit( final LogicalOpUnion op )             {
			subplanCount++;
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op )      {
			subplanCount++;
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op )     {
			subplanCount++;
		}
	}

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
		public void visit( final LogicalOpJoin op ) {
			isSourceAssignment = false;
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			isSourceAssignment = false;
		}
	} // end of class SourceAssignmentChecker

}
