package se.liu.ida.hefquin.engine.queryplan.info;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty.Type;

/**
 * Every object of this class captures a collection of information about
 * a particular query plan. These objects are meant to be populated and
 * used during query planning.
 */
public class QueryPlanningInfo implements Stats
{
	protected Map<QueryPlanProperty.Type, QueryPlanProperty> properties = new HashMap<>();

	/**
	 * Can be used to ask whether this collection of information is still empty.
	 *
	 * @return <code>true</code> if this collection of information
	 *         is still empty, <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	/**
	 * Returns the query plan property of the given type. If the collected
	 * information does not include such a property, then <code>null</code>
	 * is returned.
	 *
	 * @param type - the type for which the property is requested
	 * @return the requested property or <code>null</code>
	 */
	public QueryPlanProperty getProperty( final QueryPlanProperty.Type type ) {
		return properties.get(type);
	}

	/**
	 * Returns all collected query plan properties.
	 *
	 * @return an iterable of all collected properties
	 */
	public Iterable<QueryPlanProperty> getProperties() {
		return properties.values();
	}

	/**
	 * Adds the given property to the collected information, unless
	 * there already is a property of the same type. If this object
	 * already contains a property of the same type as the given
	 * property, then an {@link IllegalArgumentException} is thrown.
	 *
	 * @param p - the property to be added
	 */
	public void addProperty( final QueryPlanProperty p ) {
		final QueryPlanProperty.Type type = p.getType();

		if ( properties.containsKey(type) )
			throw new IllegalArgumentException("This QueryPlanningInfo object already contains a propety of type '" + type.name + "'.");

		properties.put(type, p);
	}

	@Override
	public Iterable<String> getEntryNames() {
		return new Iterable<>() {
			@Override public Iterator<String> iterator() {
				return new EntryNamesIterator();
			}
		};
	}

	protected class EntryNamesIterator implements Iterator<String> {
		protected final Iterator<Type> it = properties.keySet().iterator();
		@Override public boolean hasNext() { return it.hasNext(); }
		@Override public String next() { return it.next().name; }
	}

	@Override
	public Object getEntry( final String entryName ) {
		for ( final Map.Entry<Type, QueryPlanProperty> e : properties.entrySet() ) {
			if ( e.getKey().name.equals(entryName) ) {
				return e.getValue().getValue();
			}
		}

		return null;
	}

}
