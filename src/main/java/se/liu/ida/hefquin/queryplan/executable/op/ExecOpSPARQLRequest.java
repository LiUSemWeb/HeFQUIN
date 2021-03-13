package se.liu.ida.hefquin.queryplan.executable.op;

import java.util.Iterator;

import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.query.SolutionMapping;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ExecOpSPARQLRequest implements NullaryExecutableOp<SolutionMapping>
{
	protected final SPARQLRequest req;
	protected final SPARQLEndpoint fm;

	public ExecOpSPARQLRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		assert req != null;
		assert fm != null;

		this.req = req;
		this.fm = fm;
	}

	public void execute( final IntermediateResultElementSink<SolutionMapping> sink,
	                     final ExecutionContext execCxt )
	{
		final SolMapsResponse response = execCxt.getFederationAccessMgr().performRequest(req, fm);
		final Iterator<SolutionMapping> it = response.getIterator();
		while ( it.hasNext() ) {
			sink.send( it.next() );
		}
	}

}
