package se.liu.ida.hefquin.federation.access.impl.iface;

import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.sparql.exec.http.Params;

import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TPFInterface;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.DataRetrievalInterfaceBase;

public class TPFInterfaceImpl extends DataRetrievalInterfaceBase implements TPFInterface
{
	protected static final NodeFormatter nodeFormatter = new NodeFormatterNT();

	public final String baseURL;
	public final String baseURLWithFinalSeparator;
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

		if ( baseURL.endsWith("?") || baseURL.endsWith("&") )
			baseURLWithFinalSeparator = baseURL;
		else if ( baseURL.contains("?") )
			baseURLWithFinalSeparator = baseURL + "&";
		else
			baseURLWithFinalSeparator = baseURL + "?";
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
	public boolean supportsSPARQLPatternRequests() {
		return false;
	}

	@Override
	public boolean supportsRequest( final DataRetrievalRequest req ) {
		return req instanceof TriplePatternRequest;
	}

	@Override
	public String createRequestURL( final TPFRequest req ) {
		final String pageURL = req.getPageURL();
		if ( pageURL != null ) {
			return pageURL;
		}

		final Params params = createParams( req.getQueryPattern().asJenaTriple() );

		return baseURLWithFinalSeparator + params.httpString();
	}

	protected Params createParams( final Triple tp ) {
		final Params params = Params.create();

		final Node s = tp.getSubject();
		if ( s != null && s.isConcrete() ) {
			if ( s.isURI() ) {
				params.add( httpQueryArgumentForSubject, s.getURI() );
			}
			else {
				throw new IllegalArgumentException("The triple pattern of the given request has an illegal subject (" + s.getClass().getName() + ").");
			}
		}
		else if ( s != null && s.isVariable() ) {
			// variables need to be included in the request;
			// otherwise brTPF servers do not know what to do
			// with the variables in the 'values' parameter
			params.add( httpQueryArgumentForSubject, "?" + s.getName() );
		}

		final Node p = tp.getPredicate();
		if ( p != null && p.isConcrete() ) {
			if ( p.isURI() ) {
				params.add( httpQueryArgumentForPredicate, p.getURI() );
			}
			else {
				throw new IllegalArgumentException("The triple pattern of the given request has an illegal predicate (" + s.getClass().getName() + ").");
			}
		}
		else if ( p != null && p.isVariable() ) {
			params.add( httpQueryArgumentForPredicate, "?" + p.getName() );
		}

		final Node o = tp.getObject();
		if ( o != null && o.isConcrete() ) {
			if ( o.isURI() ) {
				params.add( httpQueryArgumentForObject, o.getURI() );
			}
			else if ( o.isLiteral() ) {
				final StringWriterI w = new StringWriterI();
				nodeFormatter.formatLiteral(w, o);
				params.add( httpQueryArgumentForObject, w.toString() );
			}
			else {
				throw new IllegalArgumentException("The triple pattern of the given request has an illegal object (" + s.getClass().getName() + ").");
			}
		}
		else if ( o != null && o.isVariable() ) {
			params.add( httpQueryArgumentForObject, "?" + o.getName() );
		}

		return params;
	}

	@Override
	public String toString() {
		return "TPFInterface server at " + baseURL;
	}

}
