package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.queryplan.ExecutableOperatorCreator;
import se.liu.ida.hefquin.queryplan.PhysicalOperator;

public class ExecOpSPARQLRequest extends ExecOpGenericSolMapsRequest<SPARQLRequest>
{
	private static Creator creator = null;

	public static ExecutableOperatorCreator getCreator() {
		if ( creator == null ) {
			creator = new Creator();
		}
		return creator;
	}

	public ExecOpSPARQLRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		super( req, fm );
	}

	protected SolMapsResponse performRequest( final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest( req, (SPARQLEndpoint) fm );
	}

	protected static class Creator implements ExecutableOperatorCreator
	{
		@Override
		public ExecOpSPARQLRequest createOp( final PhysicalOperator physicalOp ) throws IllegalArgumentException {
			// TODO: implement this once a corresponding physical operator exists (or, perhaps, it is a better idea to move this stuff to that physical op.)
return null;
//			if ( physicalOp instanceof ...??? ) {
//				return new  ExecOpSPARQLRequest( (...???) physicalOp);
//			}
//			else {
//				throw new IllegalArgumentException("Unexpected type of physical operator: " + physicalOp.getClass().getName() + " (expected: ...?TODO)" );
//			}
		}
	}

}
