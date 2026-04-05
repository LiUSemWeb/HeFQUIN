package se.liu.ida.hefquin.mappings.algebra.exec;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.BaseForMappingRelationImpl;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;

/**
 * A {@link MappingRelation} produced by a {@link MappingOpUnion} operator.
 */
public class UnionedMappingRelation extends BaseForMappingRelationImpl
{
	protected final List<MappingRelation> inputs;

	public UnionedMappingRelation( final MappingRelation ... inputs ) {
		this( Arrays.asList(inputs) );
	}

	public UnionedMappingRelation( final List<MappingRelation> inputs ) {
		super( inputs.get(0).getSchema() );

		this.inputs = inputs;
	}

	@Override
	public MappingRelationCursor getCursor() {
		return new MyCursor(this);
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation myRelation;

		protected final Iterator<MappingRelation> inputIt;
		protected MappingRelationCursor currentInput = null;
		/**
		 * This is the schema of {@link #currentInput}, which we need to
		 * remember to be able to check whether {@link #currentSchemaMapping}
		 * needs to be updated when we move to the next non-empty
		 * {@link #currentInput}.
		 */
		protected List<String> currentInputSchema = null;
		/**
		 * The i-th position in this array corresponds to the i-th
		 * attribute in the schema of {@link #myRelation} and the
		 * integer at that position in the array is the position of
		 * the same attribute in {@link #currentInputSchema}. As an
		 * optimization, the array is null if the two schemas coincide.
		 */
		protected int[] currentSchemaMapping = null;

		public MyCursor( final MappingRelation myRelation ) {
			this.myRelation = myRelation;
			inputIt = inputs.iterator();
		}

		@Override
		public MappingRelation getMappingRelation() { return myRelation; }

		@Override
		public boolean hasNext() {
			while ( currentInput == null || ! currentInput.hasNext() ) {
				if ( ! inputIt.hasNext() ) {
					return false;
				}

				final MappingRelation r = inputIt.next();
				currentInput = r.getCursor();

				if ( currentInput.hasNext() ) {
					final List<String> prevInputSchema = currentInputSchema;
					currentInputSchema = r.getSchema();

					// If the schema of the previous (non-empty) input
					// relation is different from the schema of the
					// next/current (non-empty) input relation (where
					// the difference should be only in the order of
					// the attributes, not in terms of having different
					// attributes), then we need to update the schema
					// mapping (currentSchemaMapping) that we will use
					// while consuming that next input relation. Yet,
					// we do this only if the next input is nonempty
					// (i.e., currentInput.hasNext() == true).
					if ( ! Objects.equals(prevInputSchema, currentInputSchema) ) {
						updateSchemaMapping(currentInputSchema);
					}
				}
			}

			return true;
		}

		protected void updateSchemaMapping( final List<String> inputSchema ) {
			final List<String> mySchema = myRelation.getSchema();
			if ( mySchema.equals(inputSchema) ) {
				currentSchemaMapping = null;
			}
			else {
				if ( currentSchemaMapping == null ) {
					currentSchemaMapping = new int[ mySchema.size() ];
				}

				int i = 0;
				for ( final String attr : mySchema ) {
					final int attrIdxInInputSchema =inputSchema.indexOf(attr);
					assert attrIdxInInputSchema != -1;
					currentSchemaMapping[i++] = attrIdxInInputSchema;
				}
			}
		}

		@Override
		public void advance() {
			if ( ! hasNext() )
				throw new UnsupportedOperationException();

			currentInput.advance();
		}

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			if ( currentSchemaMapping == null ) {
				return currentInput.getValueOfCurrentTuple(idxOfAttribute);
			}

			final int idxInInput = currentSchemaMapping[idxOfAttribute];
			return currentInput.getValueOfCurrentTuple(idxInInput);
		}
	}

}
