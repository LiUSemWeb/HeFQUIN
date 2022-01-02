package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.TPFInterface;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseBuilder;

public class TPFRequestProcessorImpl extends TPFRequestProcessorBase implements TPFRequestProcessor
{
	public TPFRequestProcessorImpl( final int connectionTimeout, final int readTimeout ) {
		super(connectionTimeout, readTimeout);
	}

	public TPFRequestProcessorImpl() {
		super();
	}

	@Override
	public TPFResponse performRequest( final TPFRequest req, final TPFServer fm ) throws FederationAccessException {
		return performRequest(req, fm.getInterface(), fm);
	}

	@Override
	public TPFResponse performRequest( final TPFRequest req, final BRTPFServer fm ) throws FederationAccessException {
		return performRequest(req, fm.getInterface(), fm);
	}

	protected TPFResponse performRequest( final TPFRequest req, final TPFInterface iface, final FederationMember fm ) throws FederationAccessException {
		final TPFResponseBuilder b;
		try {
			b = performRequest( iface.createHttpRequest(req), req.getQueryPattern() );
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Performing a TPF request caused an exception.", ex, req, fm);
		}

		return b.setRequest(req)
		        .setFederationMember(fm)
		        .build();
	}
}