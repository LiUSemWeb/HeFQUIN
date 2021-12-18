package se.liu.ida.hefquin.engine.utils;

public interface Stats
{
	Iterable<String> getEntryNames();
	Object getEntry(String entryName);
	boolean isEmpty();
}
