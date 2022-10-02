package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.mappings.VocabularyMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;

public class ExecOpRequestSPARQLWithTranslation extends BaseForExecOpSolMapsRequest<SPARQLRequest, SPARQLEndpoint>
{
	public ExecOpRequestSPARQLWithTranslation( final SPARQLRequest req,
	                                           final SPARQLEndpoint fm,
	                                           final boolean collectExceptions ) {
		super( req, fm, collectExceptions );
	}

	@Override
	protected void process( final SolMapsResponse response, final IntermediateResultElementSink sink )
	{
		for ( final SolutionMapping sm : response.getSolutionMappings() ) {
			for ( final SolutionMapping smTranslated : fm.getVocabularyMapping().translateSolutionMapping(sm) ) {
				sink.send( smTranslated );
			}
		}
	}
	
	@Override
	protected SolMapsResponse performRequest( final FederationAccessManager fedAccessMgr ) throws FederationAccessException
	{
		final SPARQLGraphPattern translatedPattern = VocabularyMappingUtils.translateGraphPattern( req.getQueryPattern(),
		                                                                                           fm.getVocabularyMapping() );

		final SPARQLRequest newReq;
		if ( translatedPattern == req.getQueryPattern() ) {
			newReq = req;
		}
		else {
			newReq = new SPARQLRequestImpl(translatedPattern);
		}

		return FederationAccessUtils.performRequest(fedAccessMgr, newReq, fm);
	}

}
