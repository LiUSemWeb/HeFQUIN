package se.liu.ida.hefquin.mappings.algebra.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.BaseForMappingRelationImpl;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExpression;

/**
 * A {@link MappingRelation} produced by a {@link MappingOpExtend} operator.
 */
public class ExtendedMappingRelation extends BaseForMappingRelationImpl
{
	protected final MappingRelation input;
	protected final ExtendExpression expr;
	protected final String attribute;

	protected final int idxOfNewAttribute;

	protected final String[] schemaMappingAttrs;
	protected final int[] schemaMappingIdxs;

	public ExtendedMappingRelation( final MappingRelation input,
	                                final ExtendExpression expr,
	                                final String attribute ) {
		super( createSchema(input, attribute) );

		assert expr != null;
		assert attribute != null;
		assert ! input.getSchema().contains(attribute);

		this.expr = expr;
		this.attribute = attribute;
		this.input = input;

		this.idxOfNewAttribute = schema.size() - 1;

		// Set up schemaMappingAttrs and schemaMappingIdxs, which specify
		// where in the schema of the input we can find the attributes that
		// are mentioned in the given extend expression.
		final List<String> inputSchema = input.getSchema();
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

	static protected List<String> createSchema( final MappingRelation input,
	                                            final String attribute ) {
		final List<String> schema = new ArrayList<>( input.getSchema() );
		schema.add(attribute);
		return schema;
	}

	@Override
	public MappingRelationCursor getCursor() {
		return new MyCursor(this);
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation myRelation;
		protected final MappingRelationCursor inputCursor;

		protected final Map<String,Node> currentAssignment = new HashMap<>();
		protected Node currentValue = null;

		public MyCursor( final MappingRelation myRelation ) {
			this.myRelation = myRelation;
			this.inputCursor = input.getCursor();
		}

		@Override
		public MappingRelation getMappingRelation() { return myRelation; }

		@Override
		public boolean hasNext() {
			return inputCursor.hasNext();
		}

		@Override
		public void advance() {
			inputCursor.advance();
			currentValue = null;
		}

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			if ( idxOfAttribute != idxOfNewAttribute )
				return inputCursor.getValueOfCurrentTuple(idxOfAttribute);

			if ( currentValue == null ) {
				currentAssignment.clear();
				for ( int i = 0; i < schemaMappingAttrs.length; i++ ) {
					final String attr = schemaMappingAttrs[i];
					final Node value = inputCursor.getValueOfCurrentTuple( schemaMappingIdxs[i] );
					currentAssignment.put(attr, value);
				}

				currentValue = expr.evaluate( currentAssignment );
			}

			return currentValue;
		}
	}

}
