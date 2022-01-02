package se.liu.ida.hefquin.engine.federation.access.impl.iface;

import java.util.Set;

import org.apache.jena.sparql.engine.http.HttpQuery;
import org.apache.jena.sparql.serializer.SerializationContext;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
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
	public HttpQuery createHttpRequest( final BRTPFRequest req ) {
		final HttpQuery httpReq;

		final String pageURL = req.getPageURL();
		if ( pageURL != null ) {
			httpReq = createHttpRequest( pageURL );
		}
		else {
			httpReq = createHttpRequest( req.getTriplePattern().asJenaTriple() );

			final Set<SolutionMapping> solmaps = req.getSolutionMappings();
			if ( solmaps != null && ! solmaps.isEmpty() ) {
				final String values = SolutionMappingUtils.createValuesClause(solmaps, scxt);
				httpReq.addParam( httpQueryArgumentForBindings, values );
			}
		}

		setHeaders(httpReq);
		return httpReq;
	}

}
