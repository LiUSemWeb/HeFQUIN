package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;
import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.data.impl.TripleImpl;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.response.CachedCardinalityResponse;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;

/**
 * Serializer/deserializer for {@link ChronicleMapCacheEntry}.
 *
 * <p>
 * Writes the entry's creation timestamp followed by a serialized form of the
 * cached {@link DataRetrievalResponse}, including a type discriminator. Reading
 * reconstructs a new entry instance.
 * </p>
 */
public class ChronicleMapCacheEntryMarshaller
		implements BytesWriter<ChronicleMapCacheEntry>, BytesReader<ChronicleMapCacheEntry>
{
	protected final static ChronicleMapCacheEntryMarshaller INSTANCE = new ChronicleMapCacheEntryMarshaller();

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

	/**
	 * Serializes a cache entry into the given {@link Bytes} buffer.
	 *
	 * <p>
	 * Serialization is performed into a temporary buffer first and only written to
	 * {@code out} upon success, ensuring that partially written entries do not
	 * corrupt the cache.
	 * </p>
	 *
	 * <p>
	 * This method blocks until the {@link DataRetrievalResponse} contained in the
	 * entry is available.
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
	 * @param out   the output buffer to which the serialized entry is written
	 * @param entry the cache entry to serialize
	 *
	 * @throws IllegalStateException if the response type is unsupported or if
	 *                               serialization fails (e.g., due to retrieval
	 *                               errors or interrupted execution)
	 */
	@Override
	public void write( final Bytes<?> out, final ChronicleMapCacheEntry entry ) {
		final Bytes<?> tmp = Bytes.allocateElasticOnHeap();
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
					binding.forEach( (var, node) -> {
						tmp.writeUtf8( var.getVarName() );
						tmp.writeUtf8( serializeNode(node) );
					} );
				}
			}
			else if ( object instanceof TPFResponse tpfResp ) {
				tmp.writeInt( CachedObjectType.TPF_RESPONSE.ordinal() );
				final List<Triple> matchingTriples = new ArrayList<>();
				tpfResp.getPayload().forEach(matchingTriples::add);
				tmp.writeInt( matchingTriples.size() );
				for ( final Triple t : matchingTriples ) {
					tmp.writeUtf8( serializeNode( t.asJenaTriple().getSubject() ) );
					tmp.writeUtf8( serializeNode( t.asJenaTriple().getPredicate() ) );
					tmp.writeUtf8( serializeNode( t.asJenaTriple().getMatchObject() ) );
				}

				final List<Triple> metadataTriples = new ArrayList<>();
				tpfResp.getMetadata().forEach( metadataTriples::add );;
				tmp.writeInt( metadataTriples.size() );
				for ( final Triple t : metadataTriples ) {
					tmp.writeUtf8( serializeNode( t.asJenaTriple().getSubject() ) );
					tmp.writeUtf8( serializeNode( t.asJenaTriple().getPredicate() ) );
					tmp.writeUtf8( serializeNode( t.asJenaTriple().getObject() ) );
				}

				final String nextPageURL = tpfResp.getNextPageURL();
				tmp.writeUtf8( nextPageURL );
			}
			else if ( object instanceof CardinalityResponse cardResp ) {
				tmp.writeInt( CachedObjectType.CARDINALITY_RESPONSE.ordinal() );
				tmp.writeInt( cardResp.getCardinality() );
			}
			else {
				throw new IllegalStateException( "Unsupported type: " + object.getClass() );
			}
			out.write(tmp);
		} catch ( final UnsupportedOperationDueToRetrievalError e ) {
			throw new IllegalStateException(e);
		} catch ( final InterruptedException e ) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		} catch ( final ExecutionException e ) {
			throw new IllegalStateException(e);
		} finally {
			tmp.releaseLast();
		}
	}

	/**
	 * Deserializes a {@link ChronicleMapCacheEntry} from the given {@link Bytes}
	 * buffer.
	 *
	 * <p>
	 * This method reconstructs a cache entry by reading its creation timestamp and
	 * a serialized {@link DataRetrievalResponse}, using a type discriminator to
	 * determine the concrete response type.
	 * </p>
	 *
	 * <p>
	 * The binary format must match exactly the format defined in
	 * {@link #write(Bytes, ChronicleMapCacheEntry)}.
	 * </p>
	 *
	 * @param in    the input buffer containing the serialized cache entry
	 * @param using an optional reusable instance (ignored in this implementation)
	 * @return a reconstructed {@link ChronicleMapCacheEntry} containing the
	 *         deserialized response and original creation timestamp
	 *
	 * @throws IllegalStateException if the type discriminator is not supported
	 */
	@Override
	public ChronicleMapCacheEntry read( final Bytes<?> in, final ChronicleMapCacheEntry using ) {
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
					final Var var = Var.alloc( in.readUtf8() );
					final Node node = deserializeNode( in.readUtf8() );
					builder.add(var, node);

				}
				solutionMappings.add( new SolutionMappingImpl( builder.build() ) );
			}
			object = new SolMapsResponseImpl( solutionMappings, null, null, new Date() );
		}
		else if ( type.equals( CachedObjectType.TPF_RESPONSE ) ) {
			// Matching triples
			final int matchingTriplesSize = in.readInt();
			final List<Triple> matchingTriples = new ArrayList<>(matchingTriplesSize);
			for ( int i = 0; i < matchingTriplesSize; i++ ) {
				final Node s = deserializeNode( in.readUtf8() );
				final Node p = deserializeNode( in.readUtf8() );
				final Node o = deserializeNode( in.readUtf8() );
				matchingTriples.add( new TripleImpl(s, p, o) );
			}

			// Metadata triples
			final int metadataTriplesSize = in.readInt();
			final List<Triple> metadataTriples = new ArrayList<>(metadataTriplesSize);
			for ( int i = 0; i < metadataTriplesSize; i++ ) {
				final Node s = deserializeNode( in.readUtf8() );
				final Node p = deserializeNode( in.readUtf8() );
				final Node o = deserializeNode( in.readUtf8() );
				metadataTriples.add( new TripleImpl(s, p, o) );
			}

			// Next page URL
			final String nextPageURL = in.readUtf8();

			object = new TPFResponseImpl( matchingTriples, metadataTriples, nextPageURL, null, null, new Date() );
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
}
