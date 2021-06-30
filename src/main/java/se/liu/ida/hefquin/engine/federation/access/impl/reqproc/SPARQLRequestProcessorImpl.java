package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;

public class SPARQLRequestProcessorImpl implements SPARQLRequestProcessor
{
	@Override
	public SolMapsResponse performRequest( final SPARQLRequest req,
	                                       final SPARQLEndpoint fm )
			throws FederationAccessException
	{
		final Query query = req.getQuery().asJenaQuery();
		final MySolutionConsumer sink = new MySolutionConsumer();

		final Date requestStartTime = new Date();

		final RDFConnection conn;
		try {
			conn = RDFConnectionFactory.connect( fm.getInterface().getURL(),
                    null,   // updateServiceEndpoint
                    null ); // graphStoreProtocolEndpoint
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Connecting to the SPARQL endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
		}

		try {
			conn.querySelect(query, sink);
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Issuing the given query to the SPARQL endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
		}

		try {
			conn.close();
		}
		catch ( final Exception ex ) {
			throw new FederationAccessException("Closing the connection to the SPARQL endpoint at '" + fm.getInterface().getURL() + "' caused an exception.", ex, req, fm);
		}

		return new SolMapsResponseImpl(sink.solMaps, fm, req, requestStartTime);
	}


	protected static class MySolutionConsumer implements Consumer<QuerySolution>
	{
		public final List<SolutionMapping> solMaps = new ArrayList<>();

		@Override
		public void accept( final QuerySolution s ) {
			solMaps.add( SolutionMappingUtils.createSolutionMapping(s) );
		}
		
	} // end of MySolutionConsumer

}
