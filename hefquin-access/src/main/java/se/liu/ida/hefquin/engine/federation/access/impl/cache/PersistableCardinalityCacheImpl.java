package se.liu.ida.hefquin.engine.federation.access.impl.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import se.liu.ida.hefquin.base.datastructures.PersistableCache;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.UnsupportedOperationDueToRetrievalError;

/**
 * A thread-safe cache implementation for storing cardinality responses. This
 * cache uses a {@link ConcurrentHashMap} to store values and supports
 * serialization for persistence.
 *
 * @param <K> The key type for caching cardinality responses.
 */
public class PersistableCardinalityCacheImpl<K> implements PersistableCache<K, CompletableFuture<CardinalityResponse>>
{
	protected final Map<K, CompletableFuture<CardinalityResponse>> map = new ConcurrentHashMap<>();
	protected final String filename;
	protected final static String defaultFilename = "cache/cache.dat";

	/**
	 * Constructs a new {@link PersistableCardinalityCacheImpl} with the default
	 * cache file.
	 */
	public PersistableCardinalityCacheImpl() {
		this( defaultFilename );
	}

	/**
	 * Constructs a new {@link PersistableCardinalityCacheImpl} with a custom file
	 * path.
	 *
	 * @param filename Path to the cache file.
	 */
	public PersistableCardinalityCacheImpl( final String filename ) {
		this.filename = filename;
		ensureFileExists();
		load();
	}

	/**
	 * Ensures that the cache file exists before reading or writing. If the file
	 * does not exist, it is created along with necessary directories.
	 */
	private void ensureFileExists() {
		final File file = new File( filename );
		try {
			if ( ! file.exists() ) {
				file.getParentFile().mkdirs();
				file.createNewFile();
				save();
			}
		} catch ( IOException e ) {
			throw new RuntimeException( "Failed to create file: " + file.getAbsolutePath(), e );
		}
	}

	/**
	 * Saves the current cache state to a file. Only completed
	 * {@link CardinalityResponse} objects are persisted.
	 */
	public void save() {
		Map<K, Integer> snapshot = new HashMap<>();
		for ( Map.Entry<K, CompletableFuture<CardinalityResponse>> entry : map.entrySet() ) {
			final CompletableFuture<CardinalityResponse> future = entry.getValue();
			if ( future.isDone() && ! future.isCompletedExceptionally() ) {
				final CardinalityResponse value = future.getNow( null );
				if ( value != null ) {
					try {
						snapshot.put( entry.getKey(), value.getCardinality() );
					} catch(UnsupportedOperationDueToRetrievalError e){
						// intentionally ignored
					}
				}
			}
		}

		try {
			final FileOutputStream file = new FileOutputStream( filename );
			final ObjectOutputStream out = new ObjectOutputStream( file );
			out.writeObject( snapshot );
			out.close();
			file.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the cache state from a file. The data is deserialized and stored in
	 * memory.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void load() {
		try {
			final FileInputStream file = new FileInputStream( filename );
			final ObjectInputStream in = new ObjectInputStream( file );
			final Map<K, Integer> m = (HashMap<K, Integer>) in.readObject();
			for ( final Entry<K, Integer> e : m.entrySet() ) {
				final CardinalityResponse cr = new CachedCardinalityResponse( e.getValue() );
				map.put( e.getKey(), CompletableFuture.completedFuture( cr ) );
			}
			in.close();
			file.close();
		} catch ( IOException | ClassNotFoundException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new cardinality response to the cache, associated with the given key.
	 * If an entry already exists for this key, it is replaced.
	 *
	 * @param key   The key identifying the response.
	 * @param value The cardinality response to store.
	 */
	@Override
	public void put( K key, CompletableFuture<CardinalityResponse> value ) {
		map.put( key, value );
	}

	/**
	 * Retrieves the cardinality response associated with the given key.
	 *
	 * @param key The key to look up.
	 * @return The cached response, or {@link null} if not found.
	 */
	@Override
	public CompletableFuture<CardinalityResponse> get( K key ) {
		return map.get( key );
	}

	/**
	 * Removes the entry associated with the given key from the cache.
	 *
	 * @param key The key to remove.
	 * @return {@link true} if an entry was removed, {@link false} otherwise.
	 */
	@Override
	public boolean evict( K key ) {
		return map.remove( key ) == null;
	}

	/**
	 * Removes the specified entry from the cache only if the current value matches.
	 *
	 * @param key   The key to remove.
	 * @param value The expected value to match before removal.
	 * @return {@link true} if the entry was removed, {@link false} otherwise.
	 */
	@Override
	public boolean evict( K key, CompletableFuture<CardinalityResponse> value ) {
		return map.remove( key, value );
	}

	/**
	 * Checks whether the cache is currently empty.
	 *
	 * @return {@link true} if the cache is empty, {@link false} otherwise.
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Clears all entries from the cache.
	 */
	@Override
	public void clear() {
		map.clear();
	}

	/**
	 * A simple implementation of {@link CardinalityResponse}.
	 */
	private class CachedCardinalityResponse implements CardinalityResponse
	{
		private final int cardinality;

		/**
		 * Constructs a CachedCardinalityResponse.
		 *
		 * @param cardinality The cached cardinality value.
		 */
		CachedCardinalityResponse( int cardinality ) {
			this.cardinality = cardinality;
		}

		@Override
		public FederationMember getFederationMember() {
			throw new UnsupportedOperationException( "Method 'getFederationMember' is not implemented." );
		}

		@Override
		public DataRetrievalRequest getRequest() {
			throw new UnsupportedOperationException( "Method 'getRequest' is not implemented." );
		}

		@Override
		public Date getRequestStartTime() {
			throw new UnsupportedOperationException( "Method 'getRequestStartTime' is not implemented." );
		}

		@Override
		public Date getRetrievalEndTime() {
			throw new UnsupportedOperationException( "Method 'getRetrievalEndTime' is not implemented." );
		}

		@Override
		public Integer getResponseData() throws UnsupportedOperationDueToRetrievalError {
			if( isError() ){
				throw new UnsupportedOperationDueToRetrievalError(
					getErrorStatusCode(),
					getErrorDescription(),
					getRequest(),
					getFederationMember()
				);
			}
			return cardinality;
		}
	}
}
