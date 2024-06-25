package se.liu.ida.hefquin.engine.utils;

import static org.junit.Assert.*;
import org.junit.Test;

public class StatsImplTest
{
	@Test
	public void testGetEntryNamesEmpty() {
		final StatsImpl stats = new StatsImpl();
		assertFalse(stats.getEntryNames().iterator().hasNext());
	}

	@Test
	public void testGetEntryNamesNotEmpty() {
		final StatsImpl stats = new StatsImpl();
		stats.put("key", "value");
		assertTrue(stats.getEntryNames().iterator().hasNext());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetEntryNonExistent() {
		final StatsImpl stats = new StatsImpl();
		stats.getEntry("key");
	}

	@Test
	public void testGetEntryExisting() {
		final StatsImpl stats = new StatsImpl();
		stats.put("key", "value");
		assertEquals("value", stats.getEntry("key"));
	}

	@Test
	public void testIsEmptyEmpty() {
		final StatsImpl stats = new StatsImpl();
		assertTrue(stats.isEmpty());
	}

	@Test
	public void testIsEmptyNotEmpty() {
		final StatsImpl stats = new StatsImpl();
		stats.put("key", "value");
		assertFalse(stats.isEmpty());
	}

	@Test
	public void testPut() {
		final StatsImpl stats = new StatsImpl();
		assertNull(stats.put("key", "value"));
		assertEquals("value", stats.getEntry("key"));
	}

}
