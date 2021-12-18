package se.liu.ida.hefquin.engine.utils;

/**
 * An interface for statistics collected during some process or by a data
 * structure or component. Such statistics consists of multiple entries,
 * each of which is a name-value pair. Note that a value may be another
 * {@link Stats} object.
 */
public interface Stats
{
	/**
	 * Returns the names of all entries.
	 */
	Iterable<String> getEntryNames();

	/**
	 * Returns the value of the entry with the given name.
	 */
	Object getEntry(String entryName);

	/**
	 * Returns <code>true</code> if there are no entries in this object.
	 */
	boolean isEmpty();
}
