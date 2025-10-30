package se.liu.ida.hefquin.federation.access.impl.reqproc;

import java.util.Map;

import org.apache.jena.riot.WebContent;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.utils.BuildInfo;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseBuilder;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.TPFServer;

public class TPFRequestProcessorImpl extends TPFRequestProcessorBase implements TPFRequestProcessor
{
	public TPFRequestProcessorImpl( final long connectionTimeout ) {
		super(connectionTimeout);
	}

	public TPFRequestProcessorImpl() {
		super();
	}

	@Override
	public TPFResponse performRequest( final TPFRequest req, final TPFServer fm ) throws FederationAccessException {
		return _performRequest( req, fm );
	}

	@Override
	public TPFResponse performRequest( final TPFRequest req, final BRTPFServer fm ) throws FederationAccessException {
		return _performRequest( req, fm );
	}

	protected TPFResponse _performRequest( final TPFRequest req,
	                                       final TPFServer fm ) throws FederationAccessException {
		final String requestURL = fm.createRequestURL(req);
		final TriplePattern tp = req.getQueryPattern();
		final Map<String, String> headers = Map.of(
			"Accept", WebContent.defaultRDFAcceptHeader,
			"User-Agent", BuildInfo.getUserAgent()
		);

		final TPFResponseBuilder b;
		try {
			b = performRequest(requestURL, tp, headers);
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Performing a TPF request caused an exception.", ex, req, fm);
		}

		return b.setRequest(req)
		        .setFederationMember(fm)
		        .build();
	}
}
