package se.liu.ida.hefquin.federation.access.impl.cache;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.data.impl.TripleImpl;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap.ChronicleMapCacheEntry;
import se.liu.ida.hefquin.federation.access.impl.response.CachedCardinalityResponse;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;

/**
 * Codec for serializing and deserializing {@link ChronicleMapCacheEntry}
 * instances.
 *
 * <p>
 * This class defines the binary format used to persist cache entries
 * independently of the underlying storage technology. Implementations for
 * ChronicleMap, MapDB, or other storage backends can reuse this codec to ensure
 * a consistent on-disk representation.
 * </p>
 *
 * <p>
 * Each serialized entry consists of the entry creation timestamp followed by a
 * serialized form of the cached {@link DataRetrievalResponse}, prefixed with a
 * type discriminator that identifies the concrete response type.
 * </p>
 *
 * <p>
 * Serialization is performed into a temporary in-memory buffer before any bytes
 * are written to the provided output. This ensures that partially serialized
 * entries are never committed if serialization fails.
 * </p>
 */
public final class CacheEntryCodec {
	/**
	 * Type discriminator used in the serialized format to identify the kind of
	 * cached response stored in the entry.
	 * <p>
	 * The ordinal value of this enum is written to the byte stream and must remain
	 * stable for backward compatibility.
	 * </p>
	 */
	protected enum CachedObjectType {
		SOLUTION_MAPPING_RESPONSE,
		TPF_RESPONSE,
		CARDINALITY_RESPONSE
	};

	private CacheEntryCodec() {
	}

	/**
	 * Serializes the given cache entry to the provided output.
	 *
	 * <p>
	 * Serialization is first performed into a temporary buffer and only written to
	 * {@code out} after successful completion. This prevents partially serialized
	 * entries from being persisted if an error occurs during serialization.
	 * </p>
	 *
	 * <p>
	 * This method blocks until the {@link DataRetrievalResponse} contained in the
	 * cache entry becomes available.
	 * </p>
	 *
	 * <pre>
	 * [creationTime: long]
	 * [type: int]
	 *
	 * SOLUTION_MAPPING_RESPONSE:
	 *   [numMappings: int]
	 *   repeat numMappings:
	 *     [bindingSize: int]
	 *     repeat bindingSize:
	 *       [varName: UTF-8]
	 *       [node: UTF-8]
	 *
	 * TPF_RESPONSE:
	 *   [numMatchingTriples: int]
	 *   repeat numMatchingTriples:
	 *     [subject: UTF-8]
	 *     [predicate: UTF-8]
	 *     [object: UTF-8]
	 *   [numMetadataTriples: int]
	 *   repeat numMetadataTriples:
	 *     [subject: UTF-8]
	 *     [predicate: UTF-8]
	 *     [object: UTF-8]
	 *   [nextPageURL: UTF-8 or null]
	 *
	 * CARDINALITY_RESPONSE:
	 *   [cardinality: int]
	 * </pre>
	 *
	 * @param out   destination to which the serialized representation is written
	 * @param entry cache entry to serialize
	 *
	 * @throws IOException           if writing to the output fails
	 * @throws IllegalStateException if the response type is unsupported or the
	 *                               contained future cannot be resolved
	 */
	public static void write( final DataOutput out, final ChronicleMapCacheEntry entry ) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final DataOutputStream tmp = new DataOutputStream(baos);

		tmp.writeLong( entry.createdAt() );

		try {
			final DataRetrievalResponse<?> object = entry.getObject().get();
			if ( object instanceof SolMapsResponse solMapsResp ) {
				tmp.writeInt( CachedObjectType.SOLUTION_MAPPING_RESPONSE.ordinal() );
				final List<SolutionMapping> solutionMappings = new ArrayList<>();
				solMapsResp.getResponseData().forEach( solutionMappings::add );
				tmp.writeInt( solutionMappings.size() );
				for ( final SolutionMapping sm : solutionMappings ) {
					final Binding binding = sm.asJenaBinding();
					tmp.writeInt( binding.size() );

					final Iterator<Var> vars = binding.vars();
					while ( vars.hasNext() ) {
						final Var var = vars.next();
						final Node node = binding.get(var);

						tmp.writeUTF( var.getVarName() );
						tmp.writeUTF( serializeNode(node) );
					}
				}
			}
			else if ( object instanceof TPFResponse tpfResp ) {
				tmp.writeInt( CachedObjectType.TPF_RESPONSE.ordinal() );
				final List<Triple> matchingTriples = new ArrayList<>();
				tpfResp.getPayload().forEach(matchingTriples::add);
				tmp.writeInt( matchingTriples.size() );
				for ( final Triple t : matchingTriples ) {
					tmp.writeUTF( serializeNode( t.asJenaTriple().getSubject() ) );
					tmp.writeUTF( serializeNode( t.asJenaTriple().getPredicate() ) );
					tmp.writeUTF( serializeNode( t.asJenaTriple().getMatchObject() ) );
				}

				final List<Triple> metadataTriples = new ArrayList<>();
				tpfResp.getMetadata().forEach( metadataTriples::add );;
				tmp.writeInt( metadataTriples.size() );
				for ( final Triple t : metadataTriples ) {
					tmp.writeUTF( serializeNode( t.asJenaTriple().getSubject() ) );
					tmp.writeUTF( serializeNode( t.asJenaTriple().getPredicate() ) );
					tmp.writeUTF( serializeNode( t.asJenaTriple().getObject() ) );
				}

				final String nextPageURL = tpfResp.getNextPageURL();
				writeNullableUTF(tmp, nextPageURL);
			}
			else if ( object instanceof CardinalityResponse cardResp ) {
				tmp.writeInt( CachedObjectType.CARDINALITY_RESPONSE.ordinal() );
				tmp.writeInt( cardResp.getCardinality() );
			}
			else {
				throw new IllegalStateException( "Unsupported type: " + object.getClass() );
			}
			// only commit to the real output if successful
			tmp.flush();
    		out.write(baos.toByteArray());
		} catch ( final UnsupportedOperationDueToRetrievalError e ) {
			throw new IllegalStateException(e);
		} catch ( final InterruptedException e ) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		} catch ( final ExecutionException e ) {
			throw new IllegalStateException(e);
		} finally {
			tmp.close();
		}
	}

	/**
	 * Deserializes a cache entry from the provided input.
	 *
	 * <p>
	 * The input must contain data encoded according to the binary format defined by
	 * {@link #write(DataOutput, ChronicleMapCacheEntry)}.
	 * </p>
	 *
	 * <p>
	 * The deserialized response is wrapped in a completed {@link CompletableFuture}
	 * preserving the original creation timestamp.
	 * </p>
	 *
	 * @param in source containing a serialized cache entry
	 *
	 * @return reconstructed cache entry
	 *
	 * @throws IOException           if the serialized representation cannot be read
	 * @throws IllegalStateException if an unsupported response type identifier is
	 *                               encountered
	 */
	public static ChronicleMapCacheEntry read( final DataInput in ) throws IOException {
		final long creationTime = in.readLong();
		final CachedObjectType type = CachedObjectType.values()[in.readInt()];

		final DataRetrievalResponse<?> object;
		if ( type.equals( CachedObjectType.SOLUTION_MAPPING_RESPONSE ) ) {
			final int solutionMappingsSize = in.readInt();
			final List<SolutionMapping> solutionMappings = new ArrayList<>(solutionMappingsSize);
			for ( int i = 0; i < solutionMappingsSize; i++ ) {
				final int bindingSize = in.readInt();
				final BindingBuilder builder = BindingBuilder.create();
				for ( int j = 0; j < bindingSize; j++ ) {
					final Var var = Var.alloc( in.readUTF() );
					final Node node = deserializeNode( in.readUTF() );
					builder.add(var, node);

				}
				solutionMappings.add( new SolutionMappingImpl( builder.build() ) );
			}
			object = new SolMapsResponseImpl( solutionMappings, new Date() );
		}
		else if ( type.equals( CachedObjectType.TPF_RESPONSE ) ) {
			// Matching triples
			final int matchingTriplesSize = in.readInt();
			final List<Triple> matchingTriples = new ArrayList<>(matchingTriplesSize);
			for ( int i = 0; i < matchingTriplesSize; i++ ) {
				final Node s = deserializeNode( in.readUTF() );
				final Node p = deserializeNode( in.readUTF() );
				final Node o = deserializeNode( in.readUTF() );
				matchingTriples.add( new TripleImpl(s, p, o) );
			}

			// Metadata triples
			final int metadataTriplesSize = in.readInt();
			final List<Triple> metadataTriples = new ArrayList<>(metadataTriplesSize);
			for ( int i = 0; i < metadataTriplesSize; i++ ) {
				final Node s = deserializeNode( in.readUTF() );
				final Node p = deserializeNode( in.readUTF() );
				final Node o = deserializeNode( in.readUTF() );
				metadataTriples.add( new TripleImpl(s, p, o) );
			}

			// Next page URL
			final String nextPageURL = readNullableUTF(in);

			object = new TPFResponseImpl( matchingTriples, metadataTriples, nextPageURL, new Date() );
		}
		else if ( type.equals( CachedObjectType.CARDINALITY_RESPONSE ) ) {
			final int count = in.readInt();
			object = new CachedCardinalityResponse(count);
		}
		else {
			throw new IllegalStateException( "Unsupported CachedObjectType: " + type );
		}

		return new ChronicleMapCacheEntry(CompletableFuture.completedFuture(object), creationTime);
	}

	/**
	 * Serializes a Jena {@link Node} into its N-Triples string representation.
	 *
	 * @param node the node to serialize
	 */
	protected static String serializeNode( final Node node ) {
		return NodeFmtLib.strNT(node);
	}

	/**
	 * Parses a Jena {@link Node} from its N-Triples string representation.
	 *
	 * @param s the serialized node
	 */
	protected static Node deserializeNode( final String s ) {
		return NodeFactoryExtra.parseNode(s);
	}

	private static void writeNullableUTF( final DataOutput out, final String s ) throws IOException {
		out.writeBoolean( s != null );
		if ( s != null ) {
			out.writeUTF( s );
		}
	}

	private static String readNullableUTF( final DataInput in ) throws IOException {
		return in.readBoolean() ? in.readUTF() : null;
	}
}