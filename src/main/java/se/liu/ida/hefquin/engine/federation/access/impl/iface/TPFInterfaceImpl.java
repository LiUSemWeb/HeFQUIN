package se.liu.ida.hefquin.engine.federation.access.impl.iface;

import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.sparql.engine.http.HttpQuery;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFInterface;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;

public class TPFInterfaceImpl implements TPFInterface
{
	protected static final NodeFormatter nodeFormatter = new NodeFormatterNT();

	public final String baseURL;
	public final String httpQueryArgumentForSubject;
	public final String httpQueryArgumentForPredicate;
	public final String httpQueryArgumentForObject;

	public TPFInterfaceImpl( final String baseURL,
	                         final String httpQueryArgumentForSubject,
	                         final String httpQueryArgumentForPredicate,
	                         final String httpQueryArgumentForObject ) {
		assert baseURL != null;
		assert httpQueryArgumentForSubject    != null;
		assert httpQueryArgumentForPredicate  != null;
		assert httpQueryArgumentForObject     != null;

		this.baseURL = baseURL;
		this.httpQueryArgumentForSubject    = httpQueryArgumentForSubject;
		this.httpQueryArgumentForPredicate  = httpQueryArgumentForPredicate;
		this.httpQueryArgumentForObject     = httpQueryArgumentForObject;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof TPFInterface;
	}

	@Override
	public boolean supportsTriplePatternRequests() {
		return true;
	}

	@Override
	public boolean supportsBGPRequests() {
		return false;
	}

	@Override
	public boolean supportsRequest( final DataRetrievalRequest req ) {
		return req instanceof TriplePatternRequest;
	}

	@Override
	public HttpQuery createHttpRequest( final TPFRequest req ) {
		final HttpQuery httpReq = new HttpQuery(baseURL);

		final Triple tp = req.getQueryPattern().asJenaTriple();

		final Node s = tp.getSubject();
		if ( s != null && s.isConcrete() ) {
			if ( s.isURI() ) {
				httpReq.addParam( httpQueryArgumentForSubject, s.getURI() );
			}
			else {
				throw new IllegalArgumentException("The triple pattern of the given request has an illegal subject (" + s.getClass().getName() + ").");
			}
		}

		final Node p = tp.getPredicate();
		if ( p != null && p.isConcrete() ) {
			if ( p.isURI() ) {
				httpReq.addParam( httpQueryArgumentForPredicate, p.getURI() );
			}
			else {
				throw new IllegalArgumentException("The triple pattern of the given request has an illegal predicate (" + s.getClass().getName() + ").");
			}
		}

		final Node o = tp.getObject();
		if ( o != null && o.isConcrete() ) {
			if ( o.isURI() ) {
				httpReq.addParam( httpQueryArgumentForObject, o.getURI() );
			}
			else if ( o.isLiteral() ) {
				final StringWriterI w = new StringWriterI();
				nodeFormatter.formatLiteral(w, o);
				httpReq.addParam( httpQueryArgumentForObject, w.toString() );
			}
			else {
				throw new IllegalArgumentException("The triple pattern of the given request has an illegal object (" + s.getClass().getName() + ").");
			}
		}

		// TODO
		req.getPageNumber();

		httpReq.setAllowCompression(true);
		httpReq.setAccept(WebContent.defaultRDFAcceptHeader);
		return httpReq;
	}

}
