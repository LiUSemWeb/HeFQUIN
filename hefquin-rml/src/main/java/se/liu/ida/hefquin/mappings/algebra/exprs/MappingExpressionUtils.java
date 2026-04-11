package se.liu.ida.hefquin.mappings.algebra.exprs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.utils.PlanPrinter;
import se.liu.ida.hefquin.base.utils.PlanPrinter.PrintablePlan;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.exec.*;
import se.liu.ida.hefquin.mappings.algebra.ops.*;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;

public class MappingExpressionUtils
{
	/**
	 * Prints the given expression using the {@link PlanPrinter}
	 * functionality of HeFQUIN.
	 */
	public static void print( final MappingExpression expr ) {
		final MyPrintablePlanCreator c = new MyPrintablePlanCreator();
		final PrintablePlan pp = c.createPrintablePlan(expr);
		PlanPrinter.print(pp);
	}

	/**
	 * Returns a set of all source references mentioned within
	 * the given mapping expression.
	 */
	public static Set<SourceReference> extractAllSrcRefs(
			final MappingExpression expr ) {
		final SrcRefsExtractor extractor = new SrcRefsExtractor();
		MappingExpressionWalker.walk(expr, extractor, null);
		return extractor.extractedSrcRefs;
	}

	/**
	 * Returns {@code true} if the given source assignment
	 * is valid input for the given mapping expression.
	 * <p>
	 * A source assignment is valid input for a mapping expression
	 * if, for every {@link MappingOpExtract Extract operator} of
	 * the mapping expression, it maps the source reference of that
	 * operator to a data object that fits the source type of the
	 * operator.
	 */
	public static boolean isValidInput(
			final Map<SourceReference,DataObject> srMap,
			final MappingExpression expr ) {
		final ValidInputChecker c = new ValidInputChecker(srMap);
		return c.checkForExpression(expr);
	}

	/**
	 * Returns a mapping relation that represents the result of evaluating
	 * the given mapping expression based on the given source assignment.
	 * Notice that the returned mapping relation may not have been produced
	 * when returned by this function; instead, it may be produced on the
	 * fly when consuming its cursor.
	 */
	public static MappingRelation evaluate(
			final MappingExpression expr,
			final Map<SourceReference,DataObject> srMap ) {
		// If the given expression does not have any
		// sub-expressions, we can evaluate it directly.
		if ( expr.numberOfSubExpressions() == 0 )
			return evalWorker.evaluate(expr, srMap);

		// Otherwise, we evaluate the sub-expressions first.
		final MappingRelation[] inputs = new MappingRelation[ expr.numberOfSubExpressions() ];
		for ( int i = 0; i < expr.numberOfSubExpressions(); i++ ) {
			inputs[i] = evaluate( expr.getSubExpression(i), srMap );
		}

		// .. and now evaluate the given expression, passing
		// the output from the sub-expressions as input.
		return evalWorker.evaluate(expr, srMap, inputs);
	}

/*
TODO: extend the code base such that
i) add: equals, toString, hashCode to the operators and to the expressions
ii) singleton for union operator
iii) test that everything works for actual queries,
*/

	// ---------- helpers ----------

	protected static class MyPrintablePlanCreator implements MappingOperatorVisitor {
		protected List<PrintablePlan> subPlansForVisitedOp;
		protected int idForVisitedOp;
		protected PrintablePlan ppOfVisitedOp;

		public PrintablePlan createPrintablePlan( final MappingExpression expr ) {
			final List<PrintablePlan> pps;
			if ( expr.numberOfSubExpressions() == 0 ) {
				pps = null;
			}
			else {
				pps = new ArrayList<>( expr.numberOfSubExpressions() );
				for ( int i = 0; i < expr.numberOfSubExpressions(); i++ ) {
					final MappingExpression subExpr = expr.getSubExpression(i);
					pps.add( createPrintablePlan(subExpr) );
				}
			}

			subPlansForVisitedOp = pps;
			idForVisitedOp = expr.getID();
			ppOfVisitedOp = null;

			expr.getRootOperator().visit(this);

			return ppOfVisitedOp;
		}

		@Override
		public <DDS extends DataObject,
		        DC1 extends DataObject,
		        DC2 extends DataObject,
		        QL1 extends Query,
		        QL2 extends Query>
		void visit( final MappingOpExtract<DDS, DC1, DC2, QL1, QL2> op ) {
			assert subPlansForVisitedOp == null;

			final String rootOpAsString = "Extract (" + idForVisitedOp + ")";

			final List<String> props = new ArrayList<>();
			props.add( "sr: " + op.getSourceReference().hashCode() );
			props.add( "type: " + op.getSourceType().getClass().getSimpleName() );

			for ( final Map.Entry<String, ?> e : op.getEntriesOfP() ) {
				final String attr = e.getKey();
				final Object q = e.getValue();
				props.add( attr + " -> " + q.toString() );
			}

			ppOfVisitedOp = new PrintablePlan( rootOpAsString,
			                                   props,
			                                   null ); // no sub-plans
		}

		@Override
		public void visit( final MappingOpConstant op ) {
			assert subPlansForVisitedOp == null;

			final String rootOpAsString = "Constant (" + idForVisitedOp + ")";

			ppOfVisitedOp = new PrintablePlan( rootOpAsString,
			                                   null, // no properties
			                                   null ); // no sub-plans
		}

		@Override
		public void visit( final MappingOpExtend op ) {
			assert subPlansForVisitedOp != null;
			assert subPlansForVisitedOp.size() == 1;

			final String rootOpAsString = "Extend (" + idForVisitedOp + ")";
			final String prop = op.getAttribute() + " -> " + op.getExtendExpression().toString();

			ppOfVisitedOp = new PrintablePlan( rootOpAsString,
			                                   List.of(prop),
			                                   subPlansForVisitedOp );
		}

		@Override
		public void visit( final MappingOpProject op ) {
			assert subPlansForVisitedOp != null;
			assert subPlansForVisitedOp.size() == 1;

			final String rootOpAsString = "Project (" + idForVisitedOp + ")";
			final String prop = "P: " + op.getP().toString();

			ppOfVisitedOp = new PrintablePlan( rootOpAsString,
			                                   List.of(prop),
			                                   subPlansForVisitedOp );
		}

		@Override
		public void visit( final MappingOpJoin op ) {
			assert subPlansForVisitedOp != null;
			assert subPlansForVisitedOp.size() == 2;

			final String rootOpAsString = "Join (" + idForVisitedOp + ")";

			ppOfVisitedOp = new PrintablePlan( rootOpAsString,
			                                   null, // no properties
			                                   subPlansForVisitedOp );
		}

		@Override
		public void visit( final MappingOpUnion op ) {
			assert subPlansForVisitedOp != null;
			assert subPlansForVisitedOp.size() > 0;

			final String rootOpAsString = "Union (" + idForVisitedOp + ")";

			ppOfVisitedOp = new PrintablePlan( rootOpAsString,
			                                   null, // no properties
			                                   subPlansForVisitedOp );
		}
	}


	protected static class SrcRefsExtractor implements MappingOperatorVisitor {
		public final Set<SourceReference> extractedSrcRefs = new HashSet<>();

		@Override
		public <DDS extends DataObject,
		        DC1 extends DataObject,
		        DC2 extends DataObject,
		        QL1 extends Query,
		        QL2 extends Query>
		void visit( final MappingOpExtract<DDS, DC1, DC2, QL1, QL2> op ) {
			extractedSrcRefs.add( op.getSourceReference() );
		}

		@Override
		public void visit( final MappingOpConstant op ) {}  // do nothing

		@Override
		public void visit( final MappingOpExtend op )   {}  // do nothing

		@Override
		public void visit( final MappingOpProject op )  {}  // do nothing

		@Override
		public void visit( final MappingOpJoin op )     {}  // do nothing

		@Override
		public void visit( final MappingOpUnion op )    {}  // do nothing
	}


	protected static class ValidInputChecker implements MappingOperatorVisitor {
		protected final Map<SourceReference,DataObject> srMap;
		protected boolean valid;

		public ValidInputChecker( final Map<SourceReference,DataObject> srMap ) {
			this.srMap = srMap;
		}

		public boolean checkForExpression( final MappingExpression expr ) {
			// Recursively check for all sub-expressions first.
			for ( int i = 0; i < expr.numberOfSubExpressions(); i++ ) {
				final MappingExpression subExpr = expr.getSubExpression(i);
				if ( ! checkForExpression(subExpr) )
					return false;
			}

			// Now check for the root operator of the given expression.
			valid = true;
			expr.getRootOperator().visit(this);

			return valid;
		}

		@Override
		public <DDS extends DataObject,
		        DC1 extends DataObject,
		        DC2 extends DataObject,
		        QL1 extends Query,
		        QL2 extends Query>
		void visit( final MappingOpExtract<DDS, DC1, DC2, QL1, QL2> op ) {
			final DataObject d = srMap.get( op.getSourceReference() );
			if ( d == null )
				valid = false;
			else
				valid = op.getSourceType().isRelevantDataObject(d);
		}

		@Override
		public void visit( final MappingOpConstant op ) { valid = true; }

		@Override
		public void visit( final MappingOpExtend op )   { valid = true; }

		@Override
		public void visit( final MappingOpProject op )  { valid = true; }

		@Override
		public void visit( final MappingOpJoin op )     { valid = true; }

		@Override
		public void visit( final MappingOpUnion op )    { valid = true; }
	}


	protected static EvaluateWorker evalWorker = new EvaluateWorker();

	protected static class EvaluateWorker implements MappingOperatorVisitor
	{
		protected Map<SourceReference,DataObject> srMap = null;
		protected MappingRelation[] inputs = null; 
		protected MappingRelation output = null;

		protected MappingRelation evaluate(
				final MappingExpression expr,
				final Map<SourceReference,DataObject> srMap,
				final MappingRelation ... inputs ) {
			this.srMap = srMap;
			this.inputs = inputs;
			this.output = null;

			expr.getRootOperator().visit(this);

			assert output != null;
			return output;
		}

		@Override
		public <DDS extends DataObject,
		        DC1 extends DataObject,
		        DC2 extends DataObject,
		        QL1 extends Query,
		        QL2 extends Query>
		void visit( final MappingOpExtract<DDS, DC1, DC2, QL1, QL2> op ) {
			assert inputs.length == 0;

			final DataObject d = srMap.get( op.getSourceReference() );

			if ( d == null )
				throw new IllegalArgumentException();

			@SuppressWarnings("unchecked")
			final DDS dd = (DDS) d;

			output = new ExtractedMappingRelation<>( new ArrayList<>(op.getAttributesOfP()),
			                                         op.getSourceType(),
			                                         op.getQuery(),
			                                         op.getEntriesOfP(),
			                                         dd );
		}

		@Override
		public void visit( final MappingOpConstant op ) {
			assert inputs.length == 0;

			output = op.getMappingRelation();
		}

		@Override
		public void visit( final MappingOpExtend op ) {
			assert inputs.length == 1;

			output = new ExtendedMappingRelation( inputs[0],
			                                      op.getExtendExpression(),
			                                      op.getAttribute() );
		}

		@Override
		public void visit( final MappingOpProject op ) {
			assert inputs.length == 1;

			output = new ProjectedMappingRelation( op.getP(), inputs[0] );
		}

		@Override
		public void visit( final MappingOpJoin op ) {
			assert inputs.length == 2;

			// TODO
			//output = new JoinedMappingRelation( ...
			throw new UnsupportedOperationException();
		}

		@Override
		public void visit( final MappingOpUnion op ) {
			assert inputs.length > 0;

			output = new UnionedMappingRelation(inputs);
		}
	}

}
