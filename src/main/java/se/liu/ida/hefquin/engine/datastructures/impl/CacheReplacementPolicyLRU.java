package se.liu.ida.hefquin.engine.datastructures.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheReplacementPolicyLRU<IdType,
                                       ObjectType,
                                       EntryType extends CacheEntry<ObjectType>>
      implements CacheReplacementPolicy<IdType,ObjectType,EntryType>
{
	protected final Map<IdType, IdNode> nodes = new HashMap<>();
	protected IdNode head = null;
	protected IdNode tail = null;

	@Override
	public Iterable<IdType> getEvictionCandidates( final int numberOfCandidates ) {
		final List<IdType> result = new ArrayList<>();
		int i = 0;
		while ( i < numberOfCandidates && head != null ) {
			final IdNode oldHead = head;
			result.add(oldHead.id);

			head = oldHead.next;
			oldHead.next = null;

			if ( head != null )
				head.prev = null;
		}
		return result;
	}

	@Override
	public void entryWasAdded( final IdType id, final EntryType e ) {
		final IdNode oldTail = tail;
		tail = new IdNode(id, oldTail, null);
		nodes.put(id, tail);

		if ( oldTail != null )
			oldTail.next = tail;

		if ( head == null )
			head = tail;
	}

	@Override
	public void entryWasRequested( final IdType id, final EntryType e ) {
		final IdNode n = nodes.get(id);

		if ( n == null )
			throw new IllegalArgumentException();

		// Move the entry to the end of the queue unless it is already there.
		if ( tail != n ) {
			removeFromList(n);

			if ( tail != null ) {
				tail.next = n;
			}

			n.prev = tail;
			tail = n;

			if ( head == null )
				head = tail;
		} 
	}

	@Override
	public void entryWasEvicted( final IdType id ) {
		final IdNode n = nodes.remove(id);

		if ( n == null )
			throw new IllegalArgumentException();

		removeFromList(n);
	}

	@Override
	public void clear() {
		head = null;
		tail = null;

		for ( final IdNode n : nodes.values() ) {
			n.prev = null;
			n.next = null;
		}

		nodes.clear();
	}


	/** Attention, this method does not remove n from {@link #nodes}. */
	protected void removeFromList( final IdNode n ) {
		final IdNode prev = n.prev;
		final IdNode next = n.next;

		if ( prev != null ) {
			prev.next = next;
		}
		else {
			head = next;
		}

		if ( next != null ) {
			next.prev = prev;
		}
		else {
			tail = prev;
		}

		n.prev = null;
		n.next = null;
	}


	protected class IdNode {
		public final IdType id;
		public IdNode prev, next;

		public IdNode( final IdType id, final IdNode prev, final IdNode next ) {
			this.id = id;
			this.prev = prev;
			this.next = next;
		}

		public IdNode( final IdType id ) {
			this(id, null, null);
		}
	}

}
