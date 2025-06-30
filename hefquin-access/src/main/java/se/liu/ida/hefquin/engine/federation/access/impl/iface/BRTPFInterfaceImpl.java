package se.liu.ida.hefquin.engine.federation.access.impl.iface;

import java.util.Set;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.sparql.serializer.SerializationContext;

import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.access.BRTPFInterface;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;

public class BRTPFInterfaceImpl extends TPFInterfaceImpl implements BRTPFInterface
{
	protected static final SerializationContext scxt = new SerializationContext();

	public final String httpQueryArgumentForBindings;

	public BRTPFInterfaceImpl( final String baseURL,
	                           final String httpQueryArgumentForSubject,
	                           final String httpQueryArgumentForPredicate,
	                           final String httpQueryArgumentForObject,
	                           final String httpQueryArgumentForBindings ) {
		super(baseURL,
		      httpQueryArgumentForSubject,
		      httpQueryArgumentForPredicate,
		      httpQueryArgumentForObject);

		assert httpQueryArgumentForBindings != null;

		this.httpQueryArgumentForBindings = httpQueryArgumentForBindings;
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof BRTPFInterface;
	}

	@Override
	public boolean supportsRequest( final DataRetrievalRequest req ) {
		return req instanceof BindingsRestrictedTriplePatternRequest || super.supportsRequest(req);
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

	@Override
	public String toString() {
		return "BRTPFInterface server at " + baseURL;
	}

}
