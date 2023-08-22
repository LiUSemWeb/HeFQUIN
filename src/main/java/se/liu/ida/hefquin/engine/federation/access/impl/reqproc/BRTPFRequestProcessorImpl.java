package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import org.apache.jena.sparql.engine.http.HttpQuery;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseBuilder;
import se.liu.ida.hefquin.engine.query.TriplePattern;

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
		final HttpQuery q = fm.getInterface().createHttpRequest(req);
		final TriplePattern tp = req.getTriplePattern();

		final TPFResponseBuilder b;
		try {
			b = performRequest(q, tp);
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Performing a brTPF request caused an exception.", ex, req, fm);
		}

		return b.setRequest(req)
		        .setFederationMember(fm)
		        .build();
	}
}
