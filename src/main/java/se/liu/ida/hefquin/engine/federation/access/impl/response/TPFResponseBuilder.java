package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.VOID;

import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.impl.TripleImpl;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;

public class TPFResponseBuilder
{
	public static final Node countPredicate1 = VOID.triples.asNode();
	public static final Node countPredicate2 = NodeFactory.createURI("http://www.w3.org/ns/hydra/core#totalItems");

	protected final List<Triple> matchingTriples = new ArrayList<>();
	protected final List<Triple> metadataTriples = new ArrayList<>();

	protected FederationMember fm             = null;
	protected DataRetrievalRequest request    = null;
	protected Date requestStartTime           = null;
	protected long tripleCount                = -1L;

	public TPFResponseBuilder addMatchingTriple( final Triple t) {
		matchingTriples.add(t);
		return this;
	}

	public TPFResponseBuilder addMatchingTriple( final org.apache.jena.graph.Triple t ) {
		return addMatchingTriple( new TripleImpl(t) );
	}

	public TPFResponseBuilder addMatchingTriple( final Node s, final Node p, final Node o ) {
		return addMatchingTriple( new TripleImpl(s,p,o) );
	}

	public TPFResponseBuilder addMetadataTriple( final Triple t ) {
		metadataTriples.add(t);
		return this;
	}

	public TPFResponseBuilder addMetadataTriple( final org.apache.jena.graph.Triple t ) {
		return addMetadataTriple( new TripleImpl(t) );
	}

	public TPFResponseBuilder addMetadataTriple( final Node s, final Node p, final Node o ) {
		return addMetadataTriple( new TripleImpl(s,p,o) );
	}

	public TPFResponseBuilder setFederationMember( final FederationMember fm ) {
		this.fm = fm;
		return this;
	}

	public TPFResponseBuilder setRequest( final DataRetrievalRequest request ) {
		this.request = request;
		return this;
	}

	public TPFResponseBuilder setRequestStartTime( final Date requestStartTime ) {
		this.requestStartTime = requestStartTime;
		return this;
	}

	public TPFResponseBuilder setRequestStartTimeNow() {
		return setRequestStartTime( new Date() );
	}

	public TPFResponse build() {
		if ( matchingTriples == null )
			throw new IllegalStateException("matchingTriples not specified");

		if ( metadataTriples == null )
			throw new IllegalStateException("metadataTriples not specified");

		if ( fm == null )
			throw new IllegalStateException("fed.member not specified");

		if ( request == null )
			throw new IllegalStateException("request not specified");

		if ( requestStartTime == null )
			throw new IllegalStateException("requestStartTime not specified");

		return new TPFResponseImpl(matchingTriples, metadataTriples, fm, request, requestStartTime);
	}

	protected boolean tryExtractCountMetadata( final Triple t ) {
		final Node p = t.asJenaTriple().getPredicate();
		if ( countPredicate1.equals(p) || countPredicate2.equals(p) ) {
			final Node o = t.asJenaTriple().getObject();
			if ( o.isLiteral() ) {
				final String oo = o.getLiteral().getLexicalForm();
				final long count;
				try {
					count = Long.parseLong(oo);
				}
				catch ( final NumberFormatException e ) {
					return false;
				}

				if ( count > this.tripleCount ) {
					this.tripleCount = count;
				}

				return true;
			}
		}

		return false;
	}

}
