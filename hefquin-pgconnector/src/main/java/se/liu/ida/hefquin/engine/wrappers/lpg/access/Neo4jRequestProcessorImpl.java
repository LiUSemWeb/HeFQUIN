package se.liu.ida.hefquin.engine.wrappers.lpg.access;

import se.liu.ida.hefquin.engine.wrappers.lpg.Neo4jException;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.federation.members.Neo4jServer;

import java.util.Date;
import java.util.List;

public class Neo4jRequestProcessorImpl implements Neo4jRequestProcessor
{
	@Override
	public RecordsResponse performRequest( final Neo4jRequest req,
	                                       final Neo4jServer fm ) {
		final Date requestStartTime = new Date();
		final Neo4jConnectionFactory.Neo4jConnection conn = Neo4jConnectionFactory.connect( fm.getURL() );
		final List<TableRecord> result;
		try {
			result = conn.execute( req.getCypherQuery() );
		}
		catch ( final Neo4jException e ) {
			final String msg = "Performing the remote execution of a query at " +
					"the Neo4j server with service URI " + fm.getServiceURI() +
					" caused an exception with the following message: "
					+ e.getMessage();
			return new RecordsResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
		}

		return new RecordsResponseImpl(result, requestStartTime);
	}
}
