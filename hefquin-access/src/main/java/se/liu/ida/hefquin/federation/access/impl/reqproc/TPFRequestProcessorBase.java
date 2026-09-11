package se.liu.ida.hefquin.federation.access.impl.reqproc;

import java.net.http.HttpClient;
import java.util.Map;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.http.HttpRDF;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.utils.BuildInfo;
import se.liu.ida.hefquin.base.net.http.HttpClientProvider;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.impl.RequestProcessor;
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
		httpClient = HttpClientProvider.client(connectionTimeout);
	}

	protected TPFResponse performRequest( final String requestURL,
	                                      final TriplePattern tp,
	                                      final DataRetrievalRequest req,
	                                      final FederationMember fm ) {
		final Map<String, String> headers = Map.of(
			"Accept", WebContent.defaultRDFAcceptHeader,
			"User-Agent", BuildInfo.getUserAgent()
		);

		if ( fm.getAuthenticationInformation() != null )
			fm.getAuthenticationInformation().applyTo(headers);

		final StreamRDF_TPFResponseBuilder b = new StreamRDF_TPFResponseBuilder(tp);
		b.setRequestStartTimeNow();

		// execute the request
		try {
			HttpRDF.httpGetToStream(httpClient, requestURL, headers, b);
		}
		catch ( final HttpException ex ) {
			final String type = (req instanceof BRTPFRequest) ? "an brTPF" : "a TPF";
			final String msg = "Performing " + type + " request for the server " +
					"with service URI " + fm.getServiceURI() + " caused " +
					"an HTTP-related exception with the following message: " +
					ex.getMessage();
			b.setException( new FederationAccessException(msg,ex,req,fm) );
		}
		catch ( final RiotException ex ) {
			final String type = (req instanceof BRTPFRequest) ? "an brTPF" : "a TPF";
			final String msg = "Performing " + type + " request for the server " +
					"with service URI " + fm.getServiceURI() + " caused " +
					"a parsing exception with the following message: " +
					ex.getMessage();
			b.setException( new FederationAccessException(msg,ex,req,fm) );
		}
		catch ( final Exception ex ) {
			final String type = (req instanceof BRTPFRequest) ? "an brTPF" : "a TPF";
			final String msg = "Performing " + type + " request for the server " +
					"with service URI " + fm.getServiceURI() + " caused " +
					"an unexpected exception (type: " + ex.getClass().getName() +
					") with the following message: " + ex.getMessage();
			b.setException( new FederationAccessException(msg,ex,req,fm) );
		}

		return b.build();
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
