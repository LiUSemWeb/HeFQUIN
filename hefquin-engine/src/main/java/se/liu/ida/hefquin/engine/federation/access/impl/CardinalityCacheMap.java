package se.liu.ida.hefquin.engine.federation.access.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;

/**
 * A thread-safe cache implementation for storing cardinality responses. This
 * cache uses a ConcurrentHashMap and supports serialization to persist data.
 *
 * @param <K> The key type for caching cardinality responses.
 */
public class CardinalityCacheMap<K> implements Map<K, CompletableFuture<CardinalityResponse>> {

	private final Map<K, CompletableFuture<CardinalityResponse>> map = new ConcurrentHashMap<>();
	private String filename = "cache/cache.dat";

	/**
	 * Constructs a new CardinalityCacheMap with the default cache file.
	 */
	public CardinalityCacheMap() {
		ensureFileExists();
		load();
	}

	/**
	 * Constructs a new CardinalityCacheMap with a custom file path.
	 *
	 * @param filename Path to the cache file.
	 */
	public CardinalityCacheMap( String filename ) {
		this.filename = filename;
		ensureFileExists();
		load();
	}

	/**
	 * Ensures that the cache file exists before reading or writing.
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
	 * Saves the current cache state to a file asynchronously.
	 */
	public void save() {
		Map<K, Integer> snapshot = new HashMap<>();
		for ( Map.Entry<K, CompletableFuture<CardinalityResponse>> entry : map.entrySet() ) {
			final CompletableFuture<CardinalityResponse> future = entry.getValue();
			if ( future.isDone() && ! future.isCompletedExceptionally() ) {
				final CardinalityResponse value = future.getNow( null );
				if ( value != null ) {
					snapshot.put( entry.getKey(), value.getCardinality() );
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
	 * Loads the cache state from a file.
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

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey( Object key ) {
		return map.containsKey( key );
	}

	@Override
	public boolean containsValue( Object value ) {
		return map.containsKey( value );
	}

	@Override
	public CompletableFuture<CardinalityResponse> get( Object key ) {
		return map.get( key );
	}

	@Override
	public CompletableFuture<CardinalityResponse> put( K key, CompletableFuture<CardinalityResponse> value ) {
		return map.put( key, value );
	}

	@Override
	public CompletableFuture<CardinalityResponse> remove( Object key ) {
		return map.remove( key );
	}

	@Override
	public void putAll( Map<? extends K, ? extends CompletableFuture<CardinalityResponse>> m ) {
		map.putAll( m );
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<CompletableFuture<CardinalityResponse>> values() {
		return map.values();
	}

	@Override
	public Set<Entry<K, CompletableFuture<CardinalityResponse>>> entrySet() {
		return map.entrySet();
	}

	/**
	 * A simple implementation of CardinalityResponse.
	 */
	private static class CachedCardinalityResponse implements CardinalityResponse {
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
		public int getCardinality() {
			return cardinality;
		}
	}
}
