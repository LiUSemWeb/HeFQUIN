package se.liu.ida.hefquin.federation.access.impl.reqproc;

import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
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
	public TPFResponse performRequest( final TPFRequest req, final TPFServer fm ) {
		return performRequest( fm.createRequestURL(req),
		                       req.getQueryPattern(),
		                       req,
		                       fm );
	}

	@Override
	public TPFResponse performRequest( final TPFRequest req, final BRTPFServer fm ) {
		return performRequest( fm.createRequestURL(req),
		                       req.getQueryPattern(),
		                       req,
		                       fm );
	}
}
