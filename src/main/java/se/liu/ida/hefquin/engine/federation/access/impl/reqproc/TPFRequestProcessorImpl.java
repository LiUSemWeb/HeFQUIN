package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.http.HttpQuery;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.TPFInterface;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseBuilder;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class TPFRequestProcessorImpl implements TPFRequestProcessor
{
	protected final int connectionTimeout;
	protected final int readTimeout;

	/**
	 * The given timeouts are specified in milliseconds. Any value {@literal <=} 0 means no timeout.
	 */
	public TPFRequestProcessorImpl( final int connectionTimeout, final int readTimeout ) {
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	public TPFRequestProcessorImpl() {
		this(-1, -1);
	}

	@Override
	public TPFResponse performRequest( final TPFRequest req, final TPFServer fm ) throws FederationAccessException {
		return performRequest(req, fm.getInterface(), fm);
	}

	@Override
	public TPFResponse performRequest( final TPFRequest req, final BRTPFServer fm ) throws FederationAccessException {
		return performRequest(req, fm.getInterface(), fm);
	}

	protected TPFResponse performRequest( final TPFRequest req, final TPFInterface iface, final FederationMember fm ) throws FederationAccessException {
		final TPFResponseBuilder b;
		try {
			b = performRequest(req, iface);
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Performing a TPF request caused an exception.", ex, req, fm);
		}

		b.setRequest(req);
		b.setFederationMember(fm);

		return b.build();
	}

	protected TPFResponseBuilder performRequest( final TPFRequest req,
	                                             final TPFInterface iface ) throws HttpRequestException {
		return performRequest( iface.createHttpRequest(req), req.getQueryPattern() );
	}

	protected TPFResponseBuilder performRequest( final HttpQuery req,
	                                             final TriplePattern tp ) throws HttpRequestException {
		req.setConnectTimeout(connectionTimeout);
		req.setReadTimeout(readTimeout);

		final String requestedContentType = req.getContentType();

		final TPFResponseBuilder b = new TPFResponseBuilder();
		b.setRequestStartTimeNow();

		// execute the request
		final InputStream inStream;
		try {
			inStream = req.exec();
		}
		catch ( final Exception ex ) {
			throw new HttpRequestException("Executing an HTTP request for a TPF or brTPF server caused an exception.", ex);
		}

		final String contentType = determineRetrievedContentType(req, requestedContentType);

		parseRetrievedData(inStream, contentType, tp, b);

		return b;
	}

	protected String determineRetrievedContentType( final HttpQuery req,
	                                                final String requestedContentType ) {
		final String returnedContentType = req.getContentType();

		// If the server did not return a content type, then we assume
		// that the server used the content type that was requested.
		if ( returnedContentType == null || returnedContentType.isEmpty() ) {
			return requestedContentType;
		}

		return returnedContentType;
	}

	protected void parseRetrievedData( final InputStream inStream,
	                                   final String contentType,
	                                   final TriplePattern tp,
	                                   final TPFResponseBuilder b ) throws HttpRequestException {
		final String baseIRI = null;

		final Lang lang = RDFLanguages.contentTypeToLang(contentType);
		if ( RDFLanguages.isQuads(lang) ) {
			final Iterator<Quad> it = RDFDataMgr.createIteratorQuads(inStream, lang, baseIRI);
			parseRetrievedQuads(it, tp, b);
		}
		else if ( RDFLanguages.isTriples(lang) ) {
			final Iterator<Triple> it = RDFDataMgr.createIteratorTriples(inStream, lang, baseIRI);
			parseRetrievedTriples(it, tp, b);
		}
		else {
			throw new HttpRequestException("The given content type is not a valid RDF syntax (" + contentType + ").");
		}
	}

	/**
	 * Iterates over the given quads and adds them to the given
	 * {@link TPFResponseBuilder} either as matching triples or
	 * as metadata triples, depending on whether they match the
	 * given triple pattern.
	 */
	protected void parseRetrievedQuads( final Iterator<Quad> it,
	                                    final TriplePattern tp,
	                                    final TPFResponseBuilder b ) {
		final Triple matchableTP = createMatchableTriplePattern( tp.asJenaTriple() );
		while ( it.hasNext() ) {
			final Quad q = it.next();
			final Node s = q.getSubject();
			final Node p = q.getPredicate();
			final Node o = q.getObject();

			if ( q.isDefaultGraph() && matchableTP.matches(s,p,o) ) {
				b.addMatchingTriple(s,p,o);
			}
			else {
				b.addMetadataTriple(s,p,o);
			}
		}
	}

	/**
	 * Iterates over the given triples and adds them to the given
	 * {@link TPFResponseBuilder} either as matching triples or
	 * as metadata triples, depending on whether they match the
	 * given triple pattern.
	 */
	protected void parseRetrievedTriples( final Iterator<Triple> it,
	                                      final TriplePattern tp,
	                                      final TPFResponseBuilder b ) {
		final Triple matchableTP = createMatchableTriplePattern( tp.asJenaTriple() );
		while ( it.hasNext() ) {
			final Triple t = it.next();

			if ( matchableTP.matches(t) ) {
				b.addMatchingTriple(t);
			}
			else {
				b.addMetadataTriple(t);
			}
		}
	}

	public static Triple createMatchableTriplePattern( final Triple tp ) {
		Node s = tp.getSubject();
		Node p = tp.getPredicate();
		Node o = tp.getObject();

		if ( s != null && ! s.isConcrete() ) { s = null; }
		if ( p != null && ! p.isConcrete() ) { p = null; }
		if ( o != null && ! o.isConcrete() ) { o = null; }

		return Triple.createMatch(s, p, o);
	}


	public static class HttpRequestException extends Exception {
		private static final long serialVersionUID = 4355659915417656390L;
		public HttpRequestException( final String msg ) { super(msg); }
		public HttpRequestException( final String msg, final Throwable cause ) { super(msg, cause); }
	}

}
