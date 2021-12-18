package se.liu.ida.hefquin.engine.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsImpl implements Stats
{
	private final List<String> entryNames = new ArrayList<>();
	private final Map<String,Object> entries = new HashMap<>();

	@Override
	public Iterable<String> getEntryNames() {
		return entryNames;
	}

	@Override
	public Object getEntry( final String entryName ) {
		final Object entry = entries.get(entryName);

		if ( entry == null )
			throw new IllegalArgumentException();

		return entry;
	}

	public Object put( final String entryName, final Object entry ) {
		final Object old = entries.put(entryName, entry);
		if ( old == null ) {
			entryNames.add(entryName);
		}
		return old;
	}
}
