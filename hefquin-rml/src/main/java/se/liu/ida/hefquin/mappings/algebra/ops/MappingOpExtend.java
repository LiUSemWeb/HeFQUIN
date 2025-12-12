package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExpression;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOpExtend extends BaseForUnaryMappingOperator
{
	protected final ExtendExpression expr;
	protected final String attribute;

	protected final Set<String> schema;
	protected final boolean valid;

	public MappingOpExtend( final MappingOperator subOp,
	                        final ExtendExpression expr,
	                        final String attribute ) {
		super(subOp);
		assert expr != null;
		assert attribute != null;

		this.expr = expr;
		this.attribute = attribute;

		final Set<String> schemaOfSubOp = subOp.getSchema();
		schema = new HashSet<>();
		schema.addAll(schemaOfSubOp);
		schema.add(attribute);

		if ( ! subOp.isValid() ) {
			valid = false;
		}
		else if ( schemaOfSubOp.size() == schema.size() ) {
			valid = false;
		}
		else {
			final Set<String> varsInExpr = expr.getAllMentionedAttributes();
			valid = schemaOfSubOp.containsAll(varsInExpr);
		}
	}

	@Override
	public Set<String> getSchema() {
		return schema;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		return subOp.isValidInput(srMap);
	}

	@Override
	public MappingRelation evaluate( final Map<SourceReference, DataObject> srMap ) {
		final MappingRelation input = subOp.evaluate(srMap);
		return new MyMappingRelation(input);
	}

	protected class MyMappingRelation implements MappingRelation {
		protected final MappingRelation input;
		protected final List<String> schema;

		protected final String[] schemaMappingAttrs;
		protected final int[] schemaMappingIdxs;

		public MyMappingRelation( final MappingRelation input ) {
			this.input = input;

			final List<String> inputSchema = input.getSchema();
			this.schema = new ArrayList<>( inputSchema.size() + 1 );
			this.schema.addAll(inputSchema);
			this.schema.add(attribute);

			final Set<String> attrsInExpr = expr.getAllMentionedAttributes();
			schemaMappingAttrs = new String[ attrsInExpr.size() ];
			schemaMappingIdxs = new int[ attrsInExpr.size() ];
			int idxOut = 0;
			for ( int idxIn = 0; idxIn < inputSchema.size(); idxIn++ ) {
				final String attr = inputSchema.get(idxIn);
				if ( attrsInExpr.contains(attr) ) {
					schemaMappingAttrs[idxOut] = attr;
					schemaMappingIdxs[idxOut] = idxIn;
					idxOut++;
				}
			}
		}

		@Override
		public List<String> getSchema() { return schema; }

		@Override
		public MappingRelationCursor getCursor() {
			return new MyCursor( this, input.getCursor(),
			                     schema.size() - 1,
			                     schemaMappingAttrs, schemaMappingIdxs );
		}
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation myRelation;
		protected final MappingRelationCursor input;
		protected final int idxOfNewAttribute;
		protected final String[] schemaMappingAttrs;
		protected final int[] schemaMappingIdxs;

		protected final Map<String,Node> currentAssignment = new HashMap<>();
		protected Node currentValue = null;

		public MyCursor( final MappingRelation myRelation,
		                 final MappingRelationCursor input,
		                 final int idxOfNewAttribute,
		                 final String[] schemaMappingAttrs,
		                 final int[] schemaMappingIdxs ) {
			this.myRelation = myRelation;
			this.input = input;
			this.idxOfNewAttribute = idxOfNewAttribute;
			this.schemaMappingAttrs = schemaMappingAttrs;
			this.schemaMappingIdxs = schemaMappingIdxs;
		}

		@Override
		public MappingRelation getMappingRelation() { return myRelation; }

		@Override
		public boolean hasNext() {
			return input.hasNext();
		}

		@Override
		public void advance() {
			input.advance();
			currentValue = null;
		}

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			if ( idxOfAttribute != idxOfNewAttribute )
				return input.getValueOfCurrentTuple(idxOfAttribute);

			if ( currentValue == null ) {
				for ( int i = 0; i < schemaMappingAttrs.length; i++ ) {
					final String attr = schemaMappingAttrs[i];
					final Node value = input.getValueOfCurrentTuple( schemaMappingIdxs[i] );
					currentAssignment.put(attr, value);
				}

				currentValue = expr.evaluate( currentAssignment );
			}

			return currentValue;
		}
	}

}
