package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseBuilder;

public class BRTPFRequestProcessorImpl extends TPFRequestProcessorBase implements BRTPFRequestProcessor
{
	public BRTPFRequestProcessorImpl( final int connectionTimeout, final int readTimeout ) {
		super(connectionTimeout, readTimeout);
	}

	public BRTPFRequestProcessorImpl() {
		super();
	}

	@Override
	public TPFResponse performRequest( final BRTPFRequest req, final BRTPFServer fm ) throws FederationAccessException {
		final TPFResponseBuilder b;
		try {
			b = performRequest( fm.getInterface().createHttpRequest(req), req.getTriplePattern() );
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Performing a brTPF request caused an exception.", ex, req, fm);
		}

		return b.setRequest(req)
		        .setFederationMember(fm)
		        .build();
	}
}
