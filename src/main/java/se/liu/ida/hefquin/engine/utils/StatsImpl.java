package se.liu.ida.hefquin.engine.utils;

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

		if ( entry == null )
			throw new IllegalArgumentException();

		return entry;
	}

	public Object put( final String entryName, final Object entry ) {
		return entries.put(entryName, entry);
	}
}
