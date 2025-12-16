package se.liu.ida.hefquin.mappings.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.utils.PlanPrinter;
import se.liu.ida.hefquin.base.utils.PlanPrinter.PrintablePlan;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpConstant;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtract;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpJoin;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpProject;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOperatorUtils
{
	public static Set<SourceReference> extractAllSrcRefs( final MappingOperator op ) {
		if ( op instanceof MappingOpExtract e ) {
			return Set.of( e.getSourceReference() );
		}

		final SrcRefsExtractor extractor = new SrcRefsExtractor();
		op.visit(extractor);
		return extractor.extractedSrcRefs;
	}

	public static void print( final MappingOperator op ) {
		final MyPrintablePlanCreator c = new MyPrintablePlanCreator();
		op.visit(c);

		PlanPrinter.print( c.planOfMostRecentlyVisitedOp );
	}

	public static class MyPrintablePlanCreator implements MappingOperatorVisitor {
		public PrintablePlan planOfMostRecentlyVisitedOp = null;

		@Override
		public void visit( final MappingOpConstant op ) {
			final String rootOpAsString = "Constant (" + op.getID() + ")";
			final List<String> props = null;

			planOfMostRecentlyVisitedOp = new PrintablePlan( rootOpAsString,
			                                                 props,
			                                                 null ); // no sub-plans
		}

		@Override
		public void visit( final MappingOpExtract<?,?,?,?,?> op ) {
			final String rootOpAsString = "Extract (" + op.getID() + ")";

			final List<String> props = new ArrayList<>();
			props.add( "sr: " + op.getSourceReference().hashCode() );
			props.add( "type: " + op.getSourceType().getClass().getSimpleName() );
			int i = 0;
			for ( final Query q : op.getQueriesOfP() ) {
				final String attr = op.getIthAttributeOfP( i++ );
				props.add( attr + " -> " + q.toString() );
			}

			planOfMostRecentlyVisitedOp = new PrintablePlan( rootOpAsString,
			                                                 props,
			                                                 null ); // no sub-plans
		}

		@Override
		public void visit( final MappingOpExtend op ) {
			op.getSubOp().visit(this);
			final List<PrintablePlan> subplans = List.of(planOfMostRecentlyVisitedOp);

			final String rootOpAsString = "Extend (" + op.getID() + ")";
			final String prop = op.getAttribute() + " -> " + op.getExtendExpression().toString();

			planOfMostRecentlyVisitedOp = new PrintablePlan( rootOpAsString,
			                                                 List.of(prop),
			                                                 subplans );
		}

		@Override
		public void visit( final MappingOpProject op ) {
			op.getSubOp().visit(this);
			final List<PrintablePlan> subplans = List.of(planOfMostRecentlyVisitedOp);

			final String rootOpAsString = "Project (" + op.getID() + ")";
			final String prop = "P: " + op.getP().toString();

			planOfMostRecentlyVisitedOp = new PrintablePlan( rootOpAsString,
			                                                 List.of(prop),
			                                                 subplans );
		}

		@Override
		public void visit( final MappingOpJoin op ) {
			op.getSubOp1().visit(this);
			final PrintablePlan subplan1 = planOfMostRecentlyVisitedOp;

			op.getSubOp2().visit(this);
			final PrintablePlan subplan2 = planOfMostRecentlyVisitedOp;

			final List<PrintablePlan> subplans = List.of(subplan1, subplan2);

			final String rootOpAsString = "Join (" + op.getID() + ")";
			final List<String> props = null;

			planOfMostRecentlyVisitedOp = new PrintablePlan( rootOpAsString,
			                                                 props,
			                                                 subplans );
		}

		@Override
		public void visit( final MappingOpUnion op ) {
			final int n = op.getNumberOfSubOps();
			final PrintablePlan[] subplans = new PrintablePlan[n];
			int i = 0;
			for ( final MappingOperator subOp : op.getSubOps() ) {
				subOp.visit(this);
				subplans[i++] = planOfMostRecentlyVisitedOp;
			}

			final String rootOpAsString = "Union (" + op.getID() + ")";
			final List<String> props = null;

			planOfMostRecentlyVisitedOp = new PrintablePlan( rootOpAsString,
			                                                 props,
			                                                 Arrays.asList(subplans) );
		}
	}

	public static class SrcRefsExtractor implements MappingOperatorVisitor {
		public final Set<SourceReference> extractedSrcRefs = new HashSet<>();

		@Override
		public void visit( final MappingOpConstant op ) {
			// to nothing
		}

		@Override
		public void visit( final MappingOpExtract<?, ?, ?, ?, ?> op ) {
			extractedSrcRefs.add( op.getSourceReference() );
		}

		@Override
		public void visit( final MappingOpExtend op ) {
			op.getSubOp().visit(this);
		}

		@Override
		public void visit( final MappingOpProject op ) {
			op.getSubOp().visit(this);
		}

		@Override
		public void visit( final MappingOpJoin op ) {
			op.getSubOp1().visit(this);
			op.getSubOp2().visit(this);
		}

		@Override
		public void visit( final MappingOpUnion op ) {
			for ( final MappingOperator subOp : op.getSubOps() ) {
				subOp.visit(this);
			}
		}
	}
}
