package se.liu.ida.hefquin.federation.access.impl.cache;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.junit.After;
import se.liu.ida.hefquin.base.datastructures.Cache;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCache;

public class ChronicleMapCacheTest extends CacheTestBase
{
	protected String filename = "cache/test/chronicle-map.dat";

	@After
	public void cleanup() {
		final File file = new File( filename );
		if ( file.exists() && ! file.delete() ) {
			throw new RuntimeException( "Failed to delete test cache file: " + filename );
		}
	}

	@Override
	protected Cache<PersistentCacheKey, CompletableFuture<? extends DataRetrievalResponse<?>>> createCache(
			int capacity, CachePoliciesForTest cachePolicies ) throws IOException {
		return new ChronicleMapCache( capacity, filename, cachePolicies );
	}
}
