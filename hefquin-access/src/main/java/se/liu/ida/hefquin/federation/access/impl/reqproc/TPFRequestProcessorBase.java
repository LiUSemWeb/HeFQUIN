package se.liu.ida.hefquin.federation.access.impl.reqproc;

import java.net.http.HttpClient;
import java.time.Duration;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.http.HttpRDF;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseBuilder;

/**
 * Base class for {@link RequestProcessor} implementations that retrieve {@link TPFResponse}.
 * 
 * The main method to be used in subclasses is {@link #performRequest(String, TriplePattern)}.
 */
public abstract class TPFRequestProcessorBase
{
	protected final HttpClient httpClient;

	protected TPFRequestProcessorBase() {
		this(-1L);
	}

	/**
	 * The given timeouts are specified in milliseconds. Any value {@literal <=} 0 means no timeout.
	 */
	protected TPFRequestProcessorBase( final long connectionTimeout ) {
		httpClient = createHttpClient(connectionTimeout);
	}

	protected static HttpClient createHttpClient( final long connectionTimeout ) {
		final HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
				.followRedirects( HttpClient.Redirect.ALWAYS );

		if ( connectionTimeout > 0L )
			httpClientBuilder.connectTimeout( Duration.ofMillis(connectionTimeout) );

		return httpClientBuilder.build();
	}

	protected TPFResponseBuilder performRequest( final String requestURL,
	                                             final TriplePattern tp ) throws HttpRequestException {
		final StreamRDF_TPFResponseBuilder b = new StreamRDF_TPFResponseBuilder(tp);
		b.setRequestStartTimeNow();

		// execute the request
		try {
			HttpRDF.httpGetToStream( httpClient, requestURL, WebContent.defaultRDFAcceptHeader, b );
		}
		catch ( final Exception ex ) {
			throw new HttpRequestException("Executing an HTTP request for a TPF or brTPF server caused an exception.", ex);
		}

		return b;
	}

	protected static class StreamRDF_TPFResponseBuilder extends TPFResponseBuilder implements StreamRDF {
		protected final Triple matchableTP;

		public StreamRDF_TPFResponseBuilder( final TriplePattern tp ) {
			matchableTP = createMatchableTriplePattern( tp.asJenaTriple() );
		}

		@Override
		public void start() {} // nothing to do here

		@Override
		public void triple( final Triple t ) {
			if ( matchableTP.matches(t) )
				addMatchingTriple(t);
			else
				addMetadataTriple(t);
		}

		@Override
		public void quad( final Quad q ) {
			final Node s = q.getSubject();
			final Node p = q.getPredicate();
			final Node o = q.getObject();

			if ( q.isDefaultGraph() && matchableTP.matches(s,p,o) )
				addMatchingTriple(s,p,o);
			else
				addMetadataTriple(s,p,o);
		}

		@Override
		public void base(String base) {} // nothing to do here

		@Override
		public void prefix(String prefix, String iri) {} // nothing to do here

		@Override
		public void finish() {} // nothing to do here
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
