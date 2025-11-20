package se.liu.ida.hefquin.mappings.algebra.ops;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithColumnLayout;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOpProject extends BaseForMappingOperator
{
	protected final MappingOperator subOp;
	protected final Set<String> P;

	protected final Set<String> schema;
	protected final boolean valid;

	public MappingOpProject( final MappingOperator subOp, final Set<String> P ) {
		assert subOp != null;
		assert P != null;
		assert ! P.isEmpty();

		this.subOp = subOp;
		this.P = P;

		final Set<String> schemaOfSubOp = subOp.getSchema();
		if ( schemaOfSubOp.containsAll(P) ) {
			schema = P;
			valid = subOp.isValid();
		}
		else {
			valid = false;
			schema = new HashSet<>();
			for ( final String a : P ) {
				if ( schemaOfSubOp.contains(a) )
					schema.add(a);
			}
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
		final MappingRelation input =  subOp.evaluate(srMap);
		if ( input instanceof MappingRelationImplWithColumnLayout impl )
			return createOutputRelation(impl);
		else
			return new MyMappingRelation(input);
	}

	/**
	 * Creates the output relation by simply re-using the relevant columns
	 * of the given input relation.
	 */
	protected MappingRelation createOutputRelation( final MappingRelationImplWithColumnLayout input ) {
		final List<String> inputSchema = input.getSchema();
		final Node[][] inputColumns = input.getColumns();
		final int numberOfTuples = inputColumns[0].length;

		final String[] outputSchema = new String[ P.size() ];
		final Node[][] outputColumns = new Node[ P.size() ][ numberOfTuples ];
		int idxOut = 0;
		for ( int idxIn = 0; idxIn < inputSchema.size(); idxIn++ ) {
			final String attr = inputSchema.get(idxIn);
			if ( P.contains(attr) ) {
				outputSchema[idxOut] = attr;
				outputColumns[idxOut] = inputColumns[idxIn];
				idxOut++;
			}
		}

		assert idxOut == P.size();

		return MappingRelationImplWithColumnLayout.createBasedOnColumns(outputSchema, outputColumns);
	}


	protected class MyMappingRelation implements MappingRelation {
		protected final MappingRelation input;
		protected final String[] schema;
		protected final int[] schemaMapping;

		public MyMappingRelation( final MappingRelation input ) {
			this.input = input;

			schema = new String[ P.size() ];
			schemaMapping = new int[ P.size() ];

			// populate 'schema' and 'schemaMapping'
			final List<String> inputSchema = input.getSchema();
			int idxOut = 0;
			for ( int idxIn = 0; idxIn < inputSchema.size(); idxIn++ ) {
				final String attr = inputSchema.get(idxIn);
				if ( P.contains(attr) ) {
					schema[idxOut] = attr;
					schemaMapping[idxOut] = idxIn;
					idxOut++;
				}
			}

			assert idxOut == P.size();
		}

		@Override
		public List<String> getSchema() { return Arrays.asList(schema); }

		@Override
		public MappingRelationCursor getCursor() {
			final MappingRelationCursor inputCursor = input.getCursor();
			return new MyCursor(this, inputCursor, schemaMapping);
		}
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation myRelation;
		protected final MappingRelationCursor input;
		protected final int[] schemaMapping;

		public MyCursor( final MappingRelation myRelation,
		                 final MappingRelationCursor input,
		                 final int[] schemaMapping ) {
			this.myRelation = myRelation;
			this.input = input;
			this.schemaMapping = schemaMapping;
		}

		@Override
		public MappingRelation getMappingRelation() { return myRelation; }

		@Override
		public boolean hasNext() { return input.hasNext(); }

		@Override
		public void advance() { input.advance(); }

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			final int idxInInput = schemaMapping[idxOfAttribute];
			return input.getValueOfCurrentTuple(idxInInput);
		}
	}

}
