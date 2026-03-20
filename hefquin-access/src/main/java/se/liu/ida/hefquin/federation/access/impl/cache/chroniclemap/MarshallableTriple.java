package se.liu.ida.hefquin.federation.access.impl.cache.chroniclemap;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesMarshallable;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.hash.impl.util.Objects;
import se.liu.ida.hefquin.base.data.Triple;

/**
 * ChronicleMap-native ({@link BytesMarshallable}) {@link Triple}
 * implementation.
 */
public class MarshallableTriple implements Triple, BytesMarshallable
{
	private String subject;
	private String predicate;
	private String object;
	
	public MarshallableTriple(){}

	public MarshallableTriple( final Triple triple ){
		assert triple != null;

		subject = serializeNode( triple.asJenaTriple().getSubject() );
		predicate = serializeNode( triple.asJenaTriple().getPredicate() );
		object = serializeNode( triple.asJenaTriple().getObject() );
	}

	protected static String serializeNode( final Node node ) {
		return NodeFmtLib.strNT(node);
	}

	protected static Node deserializeNode( final String s ) {
		return NodeFactoryExtra.parseNode(s);
	}

    @Override
	public void writeMarshallable( final BytesOut<?> out ) {
		out.writeUtf8(subject);
		out.writeUtf8(predicate);
		out.writeUtf8(object);
	}

	@Override
	public void readMarshallable( final BytesIn<?> in ) {
		subject = in.readUtf8();
		predicate = in.readUtf8();
		object = in.readUtf8();
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof Triple && asJenaTriple().equals( ((Triple) o).asJenaTriple() );
	}

	@Override
	public int hashCode() {
		return Objects.hash(subject, predicate, object);
	}

	@Override
	public String toString() {
		return "MarshallableTriple{subject=" + subject + ", predicate=" + predicate + ", object=" + object + "}";
	}

	@Override
	public org.apache.jena.graph.Triple asJenaTriple() {
		return org.apache.jena.graph.Triple.create( deserializeNode(subject),
		                                            deserializeNode(predicate),
		                                            deserializeNode(object) );
	}
}
