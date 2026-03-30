package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import java.util.ArrayList;
import java.util.List;

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

/**
 * Serializer/deserializer for {@link ChronicleMapCacheObject}.
 *
 * <p>
 * Writes the {@link ChronicleMapCacheObject}. Reading reconstructs a new object
 * instance.
 * </p>
 */
public class ChronicleMapCacheObjectMarshaller
		implements BytesWriter<ChronicleMapCacheObject>, BytesReader<ChronicleMapCacheObject>
{
	protected static final ChronicleMapCacheObjectMarshaller INSTANCE = new ChronicleMapCacheObjectMarshaller();

	@Override
	public void write( final Bytes<?> out, final ChronicleMapCacheObject object ) {
		// Solution mappings
		final List<SolutionMapping> solutionMappings = object.getSolutionMappings();
		out.writeInt( solutionMappings.size() );
		for ( final SolutionMapping sm : solutionMappings ) {
			final Binding binding = sm.asJenaBinding();
			out.writeInt( binding.size() );
			binding.forEach( (var, node) -> {
				out.writeUtf8( var.getVarName() );
				out.writeUtf8( node == null ? null : serializeNode(node) );
			} );
		}

		// Matching triples
		final List<Triple> matchingTriples = object.getMatchingTriples();
		out.writeInt( matchingTriples.size() );
		for ( final Triple t : matchingTriples ) {
			out.writeUtf8( serializeNode( t.asJenaTriple().getSubject() ) );
			out.writeUtf8( serializeNode( t.asJenaTriple().getPredicate() ) );
			out.writeUtf8( serializeNode( t.asJenaTriple().getObject() ) );
		}

		// Metadata triples
		final List<Triple> metadataTriples = object.getMetadataTriples();
		out.writeInt( metadataTriples.size() );
		for ( final Triple t : metadataTriples ) {
			out.writeUtf8( serializeNode( t.asJenaTriple().getSubject() ) );
			out.writeUtf8( serializeNode( t.asJenaTriple().getPredicate() ) );
			out.writeUtf8( serializeNode( t.asJenaTriple().getObject() ) );
		}

		// Next page URL
		final String nextPageURL = object.getNextPageURL();
		out.writeUtf8( nextPageURL != null ? nextPageURL : "" );

		// Count
		out.writeInt( object.getCount() );
	}

	@Override
	public ChronicleMapCacheObject read( final Bytes<?> in, final ChronicleMapCacheObject using ) {
		// Solution mappings
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

		// Matching triples
		final int matchingTriplesSize = in.readInt();
		final List<Triple> matchingTriples = new ArrayList<>(matchingTriplesSize);
		for ( int i = 0; i < matchingTriplesSize; i++ ) {
			final Node subject = deserializeNode( in.readUtf8() );
			final Node predicate = deserializeNode( in.readUtf8() );
			final Node object = deserializeNode( in.readUtf8() );
			matchingTriples.add( new TripleImpl(subject, predicate, object) );
		}

		// Metadata triples
		final int metadataTriplesSize = in.readInt();
		final List<Triple> metadataTriples = new ArrayList<>(metadataTriplesSize);
		for ( int i = 0; i < metadataTriplesSize; i++ ) {
			final Node subject = deserializeNode( in.readUtf8() );
			final Node predicate = deserializeNode( in.readUtf8() );
			final Node object = deserializeNode( in.readUtf8() );
			metadataTriples.add( new TripleImpl(subject, predicate, object) );
		}

		// Next page URL
		final String url = in.readUtf8();
		final String nextPageURL = url != "" ? url : null;

		// Count
		final int count = in.readInt();

		return new ChronicleMapCacheObject(solutionMappings, matchingTriples, metadataTriples, nextPageURL, count);
	}
	
	protected static String serializeNode( final Node node ) {
		return NodeFmtLib.strNT(node);
	}

	protected static Node deserializeNode( final String s ) {
		return NodeFactoryExtra.parseNode(s);
	}
}
