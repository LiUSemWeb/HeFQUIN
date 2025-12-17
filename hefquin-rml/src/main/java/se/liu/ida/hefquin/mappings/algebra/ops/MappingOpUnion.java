package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOpUnion extends BaseForNaryMappingOperator
{
	protected final Set<String> schema;
	protected final boolean valid;

	public MappingOpUnion( final MappingOperator ... subOps ) {
		this( Arrays.asList(subOps) );
	}

	public MappingOpUnion( final List<MappingOperator> subOps ) {
		super(subOps);

		schema = new HashSet<>();
		boolean _valid = true;
		for ( final MappingOperator subOp : subOps ) {
			final Set<String> schemaOfSubOp = subOp.getSchema();
			schema.addAll(schemaOfSubOp);

			if ( _valid && ! subOp.isValid() )
				_valid = false;

			if ( _valid && ! schema.equals(schemaOfSubOp) )
				_valid = false;
		}

		valid = _valid;
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
	public void visit( final MappingOperatorVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean isValidInput( final Map<SourceReference, DataObject> srMap ) {
		for ( final MappingOperator subOp : subOps ) {
			if ( ! subOp.isValidInput(srMap) )
				return false;
		}

		return true;
	}

	@Override
	public MappingRelation evaluate( final Map<SourceReference, DataObject> srMap ) {
		return new MyMappingRelation(srMap);
	}

	protected class MyMappingRelation implements MappingRelation {
		protected final Map<SourceReference, DataObject> srMap;
		protected final List<String> schemaL;

		public MyMappingRelation( final Map<SourceReference, DataObject> srMap ) {
			this.srMap = srMap;
			this.schemaL = new ArrayList<>(schema);
		}

		@Override
		public List<String> getSchema() { return schemaL; }

		@Override
		public MappingRelationCursor getCursor() {
			return new MyCursor(this, srMap);
		}
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation myRelation;
		protected final Map<SourceReference, DataObject> srMap;

		protected final Iterator<MappingOperator> subOpIt;
		protected MappingRelationCursor currentInput = null;
		/**
		 * The i-th position in this array corresponds to the i-th
		 * attribute in the schema of myRelation and the integer at
		 * that position in the array is the position in of the same
		 * attribute in the schema of currentInput. As an optimization,
		 * the array is null if the two schemas coincide.
		 */
		protected int[] currentSchemaMapping = null;

		public MyCursor( final MappingRelation myRelation,
		                 final Map<SourceReference, DataObject> srMap ) {
			this.myRelation = myRelation;
			this.srMap = srMap;
			subOpIt = subOps.iterator();
		}

		@Override
		public MappingRelation getMappingRelation() { return myRelation; }

		@Override
		public boolean hasNext() {
			while ( currentInput == null || ! currentInput.hasNext() ) {
				if ( ! subOpIt.hasNext() ) {
					return false;
				}

				final List<String> prevInputSchema;
				if ( currentInput == null )
					prevInputSchema = null;
				else
					prevInputSchema = currentInput.getMappingRelation().getSchema();

				final MappingRelation r = subOpIt.next().evaluate(srMap);
				currentInput = r.getCursor();

				final List<String> nextInputSchema = r.getSchema();
				if (    currentInput.hasNext()
				     && ! Objects.equals(prevInputSchema, nextInputSchema) ) {
					// If the next input schema is different from the
					// previous one (where the difference should be only
					// in the order of the attributes, not in terms of
					// having different attributes), then we need to
					// update the schema mapping (currentSchemaMapping)
					// that we will use while consuming the next input
					// relation. Yet, we do this only if the next input
					// is nonempty (i.e., currentInput.hasNext() == true).
					updateSchemaMapping(nextInputSchema);
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
