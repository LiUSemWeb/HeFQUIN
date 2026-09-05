package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.members.BRTPFServer;

public class BRTPFRequestProcessorImpl extends TPFRequestProcessorBase implements BRTPFRequestProcessor
{
	public BRTPFRequestProcessorImpl( final int connectionTimeout ) {
		super(connectionTimeout);
	}

	public BRTPFRequestProcessorImpl() {
		super();
	}

	@Override
	public TPFResponse performRequest( final BRTPFRequest req, final BRTPFServer fm ) {
		return performRequest( fm.createRequestURL(req),
		                       req.getTriplePattern(),
		                       req,
		                       fm );
	}
}
