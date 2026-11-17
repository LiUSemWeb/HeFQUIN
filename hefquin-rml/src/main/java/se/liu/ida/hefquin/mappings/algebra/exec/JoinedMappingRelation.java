package se.liu.ida.hefquin.mappings.algebra.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationUtils;
import se.liu.ida.hefquin.mappings.algebra.impl.BaseForMappingRelationImpl;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpJoin;

/**
 * A {@link MappingRelation} produced by a {@link MappingOpJoin} operator.
 */
public class JoinedMappingRelation extends BaseForMappingRelationImpl
{
	protected final MappingRelation input1;
	protected final MappingRelation input2;
	protected final List<Pair<String,String>> joinConditions;

	public JoinedMappingRelation( final MappingRelation input1,
	                              final MappingRelation input2,
	                              final List<Pair<String,String>> joinConditions ) {
		super( combineSchemas(input1, input2) );

		this.input1 = input1;
		this.input2 = input2;
		this.joinConditions = joinConditions;
	}

	protected static List<String> combineSchemas( final MappingRelation input1, final MappingRelation input2 ) {
		final List<String> schema = new ArrayList<>( input1.getSchema() );
		schema.addAll( input2.getSchema() );

		final Set<String> uniqueAttributes = new HashSet<>( schema );

		if ( uniqueAttributes.size() == schema.size() )
			return schema;

		// There is overlap
		throw new IllegalArgumentException( "Cannot join mapping relations with overlapping attributes." );
	}

	@Override
	public MappingRelationCursor getCursor() {
		return new MyCursor(this);
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation myRelation;

		protected final MappingRelationCursor rightCursor;

		protected Iterator<Node[]> currentMatchesIterator;

		protected Node[] currentLeftTuple;
		protected Node[] currentRightTuple;

		protected Node[] nextLeftTuple;
		protected Node[] nextRightTuple;

		protected boolean nextTupleReady = false;

		protected final int[] joinAttributesIndexes1;
		protected final int[] joinAttributesIndexes2;

		// Maps composite join keys to all matching tuples from input1.
		protected Map<List<Node>, List<Node[]>> hashTable = new HashMap<>();

		public MyCursor( final MappingRelation myRelation ) {
			this.myRelation = myRelation;
			this.rightCursor = input2.getCursor();

			joinAttributesIndexes1 = new int[joinConditions.size()];
			joinAttributesIndexes2 = new int[joinConditions.size()];

			for ( int i = 0; i < joinConditions.size(); i++ ) {
				final Pair<String, String> condition = joinConditions.get(i);

				joinAttributesIndexes1[i] = input1.getSchema().indexOf(condition.getLeft());
				joinAttributesIndexes2[i] = input2.getSchema().indexOf(condition.getRight());

				assert joinAttributesIndexes1[i] >= 0;
				assert joinAttributesIndexes2[i] >= 0;
			}
		}

		@Override
		public MappingRelation getMappingRelation() { return myRelation; }

		@Override
		public boolean hasNext() {
			if ( hashTable != null ) {
				hashTable = new HashMap<>();
				buildHashTable();
			}

			if ( ! nextTupleReady )
				nextTupleReady = prepareNextTuple();

			return nextTupleReady;
		}

		protected void buildHashTable() {
			final MappingRelationCursor leftCursor = input1.getCursor();

			while ( leftCursor.hasNext() ) {
				leftCursor.advance();

				final List<Node> key = getJoinKey(leftCursor, joinAttributesIndexes1);

				final Node[] tuple = MappingRelationUtils.getCurrentTuple(leftCursor);

				hashTable.computeIfAbsent(key, k -> new ArrayList<>()).add(tuple);
			}
		}

		/**
		 * Prepares the next joined tuple.
		 *
		 * @return true if another joined tuple is available, false if no more matching tuples can be found.
		 */
		protected boolean prepareNextTuple() {
			// Emit remaining matches for the current input2 tuple, if any.
			if ( currentMatchesIterator != null && currentMatchesIterator.hasNext() ) {
				nextLeftTuple = currentMatchesIterator.next();
				nextTupleReady = true;
				return true;
			}

			currentMatchesIterator = null;

			// Find the next input2 tuple that has at least one match in the hash table.
			while ( rightCursor.hasNext() ) {
				rightCursor.advance();

				final List<Node> key = getJoinKey(rightCursor, joinAttributesIndexes2);

				final List<Node[]> currentMatches = hashTable.get(key);

				if ( currentMatches != null && ! currentMatches.isEmpty() ) {
					currentMatchesIterator = currentMatches.iterator();

					nextLeftTuple = currentMatchesIterator.next();

					nextRightTuple = MappingRelationUtils.getCurrentTuple(rightCursor);

					nextTupleReady = true;
					return true;
				}
			}

			return false;
		}

		/**
		 * Extracts the composite join key from the current tuple using the specified attribute indexes.
		 *
		 * @param cursor  the cursor positioned at the tuple
		 * @param indexes the indexes of the attributes that make up the join key
		 * @return the composite join key
		 */
		protected List<Node> getJoinKey( final MappingRelationCursor cursor,
		                                 final int[] indexes ) {
			final List<Node> key = new ArrayList<>( indexes.length );

			for ( final int idx : indexes )
				key.add(cursor.getValueOfCurrentTuple(idx));

			return key;
		}

		@Override
		public void advance() {
			if ( ! hasNext() )
				throw new UnsupportedOperationException();

			currentLeftTuple = nextLeftTuple;
			currentRightTuple = nextRightTuple;

			nextTupleReady = false;
		}

		@Override
		public Node getValueOfCurrentTuple( final int idxOfAttribute ) {
			if ( idxOfAttribute < input1.getSchema().size() )
				return currentLeftTuple[idxOfAttribute];

			final int idxInRight = idxOfAttribute - input1.getSchema().size();
			return currentRightTuple[idxInRight];
		}
	}

}
