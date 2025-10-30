package se.liu.ida.hefquin.federation.members.impl;

import java.util.Objects;

import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;
import org.apache.jena.sparql.exec.http.Params;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.members.TPFServer;

public class TPFServerImpl extends BaseForFederationMember
                           implements TPFServer
{
	// TODO: these string should not be hard-coded but extracted from the
	//       Hydra description returned in each response to a TPF request
	//       see: https://github.com/LiUSemWeb/HeFQUIN/issues/232
	public final static String DfltHttpQueryArgumentForSubject   = "subject";
	public final static String DfltHttpQueryArgumentForPredicate = "predicate";
	public final static String DfltHttpQueryArgumentForObject    = "object";

	protected static final NodeFormatter nodeFormatter = new NodeFormatterNT();

	protected final VocabularyMapping vm;

	public final String baseURL;
	public final String baseURLWithFinalSeparator;
	public final String httpQueryArgumentForSubject;
	public final String httpQueryArgumentForPredicate;
	public final String httpQueryArgumentForObject;

	public TPFServerImpl( final String baseURL,
	                      final VocabularyMapping vm ) {
		this( baseURL,
		      DfltHttpQueryArgumentForSubject,
		      DfltHttpQueryArgumentForPredicate,
		      DfltHttpQueryArgumentForObject,
		      vm );
	}

	public TPFServerImpl( final String baseURL,
	                      final String httpQueryArgumentForSubject,
	                      final String httpQueryArgumentForPredicate,
	                      final String httpQueryArgumentForObject,
	                      final VocabularyMapping vm ) {
		assert baseURL != null;
		assert httpQueryArgumentForSubject    != null;
		assert httpQueryArgumentForPredicate  != null;
		assert httpQueryArgumentForObject     != null;

		this.vm = vm;

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
	public VocabularyMapping getVocabularyMapping() { return vm; }

	@Override
	public String getBaseURL() { return baseURL; }

	@Override
	public String toString() { return "TPF server at " + baseURL; }

	@Override
	public boolean equals( final Object o ) {
		if ( o == this )
			return true;

		return    o instanceof TPFServer tpf
		       && tpf.getBaseURL().equals(baseURL)
		       && Objects.equals( tpf.getVocabularyMapping(), vm );
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
}
