package se.liu.ida.hefquin.base.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatsImpl implements Stats
{
	private final Map<String,Object> entries = new LinkedHashMap<>();

	@Override
	public Iterable<String> getEntryNames() {
		return entries.keySet();
	}

	@Override
	public Object getEntry( final String entryName ) {
		final Object entry = entries.get(entryName);

		if ( entry == null && ! entries.containsKey(entryName) )
			throw new IllegalArgumentException();

		return entry;
	}

	@Override
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public Object put( final String entryName, final Object entry ) {
		return entries.put(entryName, entry);
	}
}
