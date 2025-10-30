package se.liu.ida.hefquin.federation.members.impl;

import java.util.Set;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.sparql.serializer.SerializationContext;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.members.BRTPFServer;

public class BRTPFServerImpl extends TPFServerImpl implements BRTPFServer
{
	// TODO: these strings should not be hard-coded but extracted from the
	//       Hydra description returned in each response to a brTPF request
	//       see: https://github.com/LiUSemWeb/HeFQUIN/issues/233
	public final static String DfltHttpQueryArgumentForBindings  = "values";

	protected static final SerializationContext scxt = new SerializationContext();

	public final String httpQueryArgumentForBindings;

	public BRTPFServerImpl( final String baseURL,
	                        final VocabularyMapping vm ) {
		this( baseURL,
		      DfltHttpQueryArgumentForSubject,
		      DfltHttpQueryArgumentForPredicate,
		      DfltHttpQueryArgumentForObject,
		      DfltHttpQueryArgumentForBindings,
		      vm );
	}

	public BRTPFServerImpl( final String baseURL,
	                        final String httpQueryArgumentForSubject,
	                        final String httpQueryArgumentForPredicate,
	                        final String httpQueryArgumentForObject,
	                        final String httpQueryArgumentForBindings,
	                        final VocabularyMapping vm ) {
		super( baseURL,
		       httpQueryArgumentForSubject,
		       httpQueryArgumentForPredicate,
		       httpQueryArgumentForObject,
		       vm );

		assert httpQueryArgumentForBindings != null;

		this.httpQueryArgumentForBindings = httpQueryArgumentForBindings;
	}

	@Override
	public String toString() { return "brTPF server at " + baseURL; }

	@Override
	public boolean equals( final Object o ) {
		if ( super.equals(o) == false )
			return false;

		return    o instanceof BRTPFServer brtpf
		       && brtpf.getBaseURL().equals(baseURL);
	}

	@Override
	public String createRequestURL( final BRTPFRequest req ) {
		final String pageURL = req.getPageURL();
		if ( pageURL != null ) {
			return pageURL;
		}

		final Params params = createParams( req.getTriplePattern().asJenaTriple() );

		final Set<Binding> solmaps = req.getSolutionMappings();
		if ( solmaps != null && ! solmaps.isEmpty() ) {
			final String values = SolutionMappingUtils.createValuesClause(solmaps, scxt);
			params.add( httpQueryArgumentForBindings, values );
		}

		return baseURLWithFinalSeparator + params.httpString();
	}

}
