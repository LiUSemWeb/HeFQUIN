package se.liu.ida.hefquin.mappings.algebra.exec;

import java.util.List;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.BaseForMappingRelationImpl;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpProject;

/**
 * A {@link MappingRelation} produced by a {@link MappingOpProject} operator.
 */
public class ProjectedMappingRelation extends BaseForMappingRelationImpl
{
	protected final MappingRelation input;

	/**
	 * For the i-th attribute of the output schema, the integer at the i-th
	 * index in this array is the index at which the attribute can be found
	 * in the schema of the input relation.
	 */
	protected final int[] schemaMapping;

	/**
	 * The given schema is assumed to consist of the projection variables
	 * that are in the schema of the given input relation.
	 */
	public ProjectedMappingRelation( final List<String> schema,
	                                 final MappingRelation input ) {
		super(schema);

		this.input = input;

		// Set up schemaMapping.
		final List<String> inputSchema = input.getSchema();
		schemaMapping = new int[ schema.size() ];
		for ( int i = 0; i < schema.size(); i++ ) {
			final String attr = schema.get(i);
			final int j = inputSchema.indexOf(attr);

			if ( j < 0 )
				throw new IllegalArgumentException("Projection attribute '" + attr + "' not contained in the inout schema (" + inputSchema.toString() + ").");

			schemaMapping[i] = j;
		}
	}

	@Override
	public MappingRelationCursor getCursor() {
		return new MyCursor(this);
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation myRelation;
		protected final MappingRelationCursor inputCursor;

		public MyCursor( final MappingRelation myRelation ) {
			this.myRelation = myRelation;
			this.inputCursor = input.getCursor();
		}

		@Override
		public MappingRelation getMappingRelation() { return myRelation; }

		@Override
		public boolean hasNext() { return inputCursor.hasNext(); }

		@Override
		public void advance() { inputCursor.advance(); }

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			final int idxInInput = schemaMapping[idxOfAttribute];
			return inputCursor.getValueOfCurrentTuple(idxInInput);
		}
	}
}
