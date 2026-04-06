package se.liu.ida.hefquin.mappings.algebra.exprs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.exec.*;
import se.liu.ida.hefquin.mappings.algebra.ops.*;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;

public class MappingExpressionUtils
{
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


	// ---------- helpers ----------

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

			@SuppressWarnings("unchecked")
			final DDS dd = (DDS) d;

			output = new ExtractedMappingRelation<>( new ArrayList<>(op.getSchema()),
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

			output = new ProjectedMappingRelation( new ArrayList<>(op.getSchema()),
			                                       inputs[0] );
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
