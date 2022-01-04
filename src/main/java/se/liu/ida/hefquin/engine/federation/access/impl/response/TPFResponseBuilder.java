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
	public static final Node countPredicate1    = VOID.triples.asNode();
	public static final Node countPredicate2    = NodeFactory.createURI("http://www.w3.org/ns/hydra/core#totalItems");
	public static final Node nextPagePredicate  = NodeFactory.createURI("http://www.w3.org/ns/hydra/core#next");

	protected final List<Triple> matchingTriples = new ArrayList<>();
	protected final List<Triple> metadataTriples = new ArrayList<>();

	protected FederationMember fm             = null;
	protected DataRetrievalRequest request    = null;
	protected Date requestStartTime           = null;
	protected int tripleCount                 = -1;  // TODO: should better be long, but changing affects a lot of other things
	protected String nextPageURL              = null;

	public TPFResponseBuilder addMatchingTriple( final Triple t) {
		matchingTriples.add(t);
		return this;
	}

	public final TPFResponseBuilder addMatchingTriple( final org.apache.jena.graph.Triple t ) {
		return addMatchingTriple( new TripleImpl(t) );
	}

	public final TPFResponseBuilder addMatchingTriple( final Node s, final Node p, final Node o ) {
		return addMatchingTriple( new TripleImpl(s,p,o) );
	}

	public TPFResponseBuilder addMetadataTriple( final Triple t ) {
		metadataTriples.add(t);
		tryExtractCountMetadataOrNextPageURL(t);
		return this;
	}

	public final TPFResponseBuilder addMetadataTriple( final org.apache.jena.graph.Triple t ) {
		return addMetadataTriple( new TripleImpl(t) );
	}

	public final TPFResponseBuilder addMetadataTriple( final Node s, final Node p, final Node o ) {
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

		if ( tripleCount < 0 )
			return new TPFResponseImpl(matchingTriples, metadataTriples, nextPageURL, fm, request, requestStartTime);
		else
			return new TPFResponseImpl(matchingTriples, metadataTriples, nextPageURL, tripleCount, fm, request, requestStartTime);
	}

	protected boolean tryExtractCountMetadataOrNextPageURL( final Triple t ) {
		final Node p = t.asJenaTriple().getPredicate();
		if ( p.equals(countPredicate1) || p.equals(countPredicate2) ) {
			final Node o = t.asJenaTriple().getObject();
			if ( o.isLiteral() ) {
				final String oo = o.getLiteral().getLexicalForm();
				final int count;
				try {
					count = Integer.parseInt(oo);
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
		else if ( p.equals(nextPagePredicate) ) {
			final Node o = t.asJenaTriple().getObject();
			if ( o.isURI() ) { // TODO: perhaps we should check the subject first
				this.nextPageURL = o.getURI(); // TODO: should we simply trust the server here?
				return true;
			}
		}

		return false;
	}

}
