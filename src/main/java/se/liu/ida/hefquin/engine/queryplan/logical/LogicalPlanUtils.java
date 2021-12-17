package se.liu.ida.hefquin.engine.queryplan.logical;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpUnion;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
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

	public static void printStringOfFm( final StringBuilder builder, final FederationMember fm ) {

		final DataRetrievalInterface intFace = fm.getInterface();
		if ( intFace instanceof SPARQLEndpointInterface){
			builder.append( ((SPARQLEndpointInterface) intFace).getURL() );
		}
		else {
			builder.append( " Print the Federation Member in the type of interface: " + intFace.getClass().getName() + "is an open TODO" );
		}

	}

	public static void printTriplesOfGraphPattern(final Op op, final StringBuilder builder ) {
		if ( op instanceof OpBGP) {
			builder.append( ((OpBGP) op).getPattern().getList() );
		}
		else if ( op instanceof OpJoin) {
			printTriplesOfGraphPattern( (OpJoin) op, builder );
		}
		else if ( op instanceof OpUnion) {
			printTriplesOfGraphPattern( (OpUnion) op, builder );
		}
		else {
			throw new UnsupportedOperationException("Print triples of an arbitrary SPARQL patterns is an open TODO (type of Jena Op in the current case: " + op.getClass().getName() + ").");
		}
	}

	public static void printTriplesOfGraphPattern( final OpJoin op, final StringBuilder builder ) {
		printTriplesOfGraphPattern(op.getLeft(), builder );
		builder.append(" AND ");
		printTriplesOfGraphPattern(op.getRight(), builder );
	}

	public static void printTriplesOfGraphPattern( final OpUnion op, final StringBuilder builder ) {
		printTriplesOfGraphPattern(op.getLeft(), builder );
		builder.append(" UNION ");
		printTriplesOfGraphPattern(op.getRight(), builder );
	}

}
