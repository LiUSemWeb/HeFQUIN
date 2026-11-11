package se.liu.ida.hefquin.mappings.algebra.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
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
		final List<String> schema = new ArrayList<>(input1.getSchema());

		for ( final String attr : input2.getSchema() ) {
			if ( schema.contains(attr) ) {
				throw new IllegalArgumentException(
					"Cannot join relations with overlapping attributes: " + attr );
			}

			schema.add(attr);
		}

		return schema;
	}

	@Override
	public MappingRelationCursor getCursor() {
		return new MyCursor(this);
	}

	protected class MyCursor implements MappingRelationCursor {
		protected final MappingRelation myRelation;

		protected MappingRelationCursor currentLeftCursor = null;
		protected MappingRelationCursor currentRightCursor = null;

		protected List<Node[]> currentMatches;
		protected Iterator<Node[]> currentMatchesIterator;

		protected Node[] currentLeftTuple;
		protected Node[] currentRightTuple;

		protected Node[] nextLeftTuple;
		protected Node[] nextRightTuple;

		protected boolean nextTupleReady = false;

		// Maps composite join keys to all matching tuples from input1.
		protected final Map<List<Node>, List<Node[]>> hashTable = new HashMap<>();

		public MyCursor( final MappingRelation myRelation ) {
			this.myRelation = myRelation;
			this.currentRightCursor = input2.getCursor();

			buildHashTable();
		}

		protected void buildHashTable() {
			currentLeftCursor = input1.getCursor();

			while ( currentLeftCursor.hasNext() ) {
				currentLeftCursor.advance();

				final List<Node> key = getJoinKey(currentLeftCursor, input1.getSchema(), true);

				Node[] tuple = new Node[input1.getSchema().size()];
				for ( int i = 0; i < tuple.length; i++ )
					tuple[i] = currentLeftCursor.getValueOfCurrentTuple(i);

				hashTable.computeIfAbsent(key, k -> new ArrayList<>()).add(tuple);
			}
		}

		protected boolean prepareNextTuple() {
			// Emit remaining matches for the current input2 tuple, if any.
			if ( currentMatchesIterator != null && currentMatchesIterator.hasNext() ) {
				nextLeftTuple = currentMatchesIterator.next();
				nextTupleReady = true;
				return true;
			}

			currentMatchesIterator = null;

			// Find the next input2 tuple that has at least one match in the hash table.
			while ( currentRightCursor.hasNext() ) {
				currentRightCursor.advance();

				final List<Node> key = getJoinKey(currentRightCursor, input2.getSchema(), false);

				currentMatches = hashTable.get(key);

				if ( currentMatches != null && ! currentMatches.isEmpty() ) {
					currentMatchesIterator = currentMatches.iterator();

					nextLeftTuple = currentMatchesIterator.next();

					nextRightTuple = new Node[input2.getSchema().size()];

					for (int i = 0; i < nextRightTuple.length; i++)
						nextRightTuple[i] = currentRightCursor.getValueOfCurrentTuple(i);

					nextTupleReady = true;
					return true;
				}
			}

			return false;
		}

		/**
		 * Extracts the composite join key from the current tuple.
		 * The key consists of the attribute values specified by the join conditions,
		 * using the left-side attributes for input1 and the right-side attributes for input2.
		 */
		protected List<Node> getJoinKey( final MappingRelationCursor cursor,
		                                 final List<String> schema,
		                                 final boolean isLeftInput ) {
			final List<Node> key = new ArrayList<>();

			for ( final Pair<String, String> condition : joinConditions ) {
				final String attribute = isLeftInput
						? condition.getLeft()
						: condition.getRight();

				final int idx = schema.indexOf(attribute);

				key.add(cursor.getValueOfCurrentTuple(idx));
			}

			return key;
		}

		@Override
		public MappingRelation getMappingRelation() { return myRelation; }

		@Override
		public boolean hasNext() {
			if ( ! nextTupleReady )
				nextTupleReady = prepareNextTuple();

			return nextTupleReady;
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
