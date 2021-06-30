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
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;

public class SPARQLRequestProcessorImpl implements SPARQLRequestProcessor
{
	@Override
	public SolMapsResponse performRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		final Query query = req.getQuery().asJenaQuery();
		final MySolutionConsumer sink = new MySolutionConsumer();

		final Date requestStartTime = new Date();
		final RDFConnection conn = RDFConnectionFactory.connect( fm.getInterface().getURL(),
		                                                         null,   // updateServiceEndpoint
		                                                         null ); // graphStoreProtocolEndpoint
		conn.querySelect(query, sink);
		conn.close();

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
