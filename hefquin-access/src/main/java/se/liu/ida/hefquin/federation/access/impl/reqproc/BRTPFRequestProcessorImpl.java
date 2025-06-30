package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseBuilder;

public class BRTPFRequestProcessorImpl extends TPFRequestProcessorBase implements BRTPFRequestProcessor
{
	public BRTPFRequestProcessorImpl( final int connectionTimeout ) {
		super(connectionTimeout);
	}

	public BRTPFRequestProcessorImpl() {
		super();
	}

	@Override
	public TPFResponse performRequest( final BRTPFRequest req, final BRTPFServer fm ) throws FederationAccessException {
		final String requestURL = fm.getInterface().createRequestURL(req);
		final TriplePattern tp = req.getTriplePattern();

		final TPFResponseBuilder b;
		try {
			b = performRequest(requestURL, tp);
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Performing a brTPF request caused an exception.", ex, req, fm);
		}

		return b.setRequest(req)
		        .setFederationMember(fm)
		        .build();
	}
}
