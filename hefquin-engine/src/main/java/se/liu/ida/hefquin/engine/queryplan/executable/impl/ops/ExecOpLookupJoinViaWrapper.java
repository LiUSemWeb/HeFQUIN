package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.exec.http.Params;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.RESTRequest;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.RESTRequestImpl;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint.DataConversionException;

public class ExecOpLookupJoinViaWrapper
		extends BaseForExecOpIndexNestedLoopsJoinWithRequests<SPARQLGraphPattern,
		                                                      WrappedRESTEndpoint,
		                                                      RESTRequest,
		                                                      StringResponse>
{
	protected final List<Var> paramVars;

	// statistics
	private long numberOfOutputMappingsProduced = 0L;
	private int numberOfDataConversionExceptions = 0;

	public ExecOpLookupJoinViaWrapper( final SPARQLGraphPattern pattern,
	                                   final List<Var> paramVars,
	                                   final WrappedRESTEndpoint fm,
	                                   final boolean collectExceptions,
	                                   final QueryPlanningInfo qpInfo ) {
		super(pattern, fm, collectExceptions, qpInfo);

		assert paramVars != null;
		assert paramVars.size() > 0;
		assert paramVars.size() == fm.getNumberOfParameters();

		this.paramVars = paramVars;
	}

	@Override
	protected RESTRequest createRequest( final SolutionMapping sm )
	{
// TODO: Use a cache for cases in which otherwise the same request would be created and issued multiple times.
		if ( paramVars == null ) {
			return new RESTRequestImpl( fm.getURL() );
		}

		final Params params = Params.create();

		final Iterator<RESTEndpoint.Parameter> itParamDecl = fm.getParameters().iterator();
		final Iterator<Var>                    itParamVar  = paramVars.iterator();

		while ( itParamDecl.hasNext() ) {
			final RESTEndpoint.Parameter paramDecl = itParamDecl.next();
			final Var paramVar = itParamVar.next();

			final Node paramValueAsNode = sm.asJenaBinding().get(paramVar);
			if ( paramValueAsNode == null ) return null;
			if ( ! paramValueAsNode.isLiteral() ) return null;
			if ( ! paramValueAsNode.getLiteralDatatype().equals(paramDecl.getType()) ) return null;

			final String paramValue = paramValueAsNode.getLiteralValue().toString();
			params.add( paramDecl.getName(), paramValue );
		}

		return new RESTRequestImpl( fm.getURL(), params );
	}

	@Override
	protected CompletableFuture<StringResponse> issueRequest( final RESTRequest req,
	                                                          final FederationAccessManager fedAccessMgr)
			throws FederationAccessException {
		return fedAccessMgr.issueRequest(req, fm);
	}

	@Override
	protected MyResponseProcessor createResponseProcessor( final SolutionMapping sm,
	                                                       final IntermediateResultElementSink sink,
	                                                       final ExecutableOperator op ) {
		return new MyResponseProcessor( sm, sink, op ) {
			@Override
			protected Iterable<SolutionMapping> extractSolMaps( final StringResponse response )
					throws UnsupportedOperationDueToRetrievalError {
				final String data = response.getResponseData();
				try {
					return fm.evaluatePatternOverRDFView(query, data);
				}
				catch ( final DataConversionException e ) {
					numberOfDataConversionExceptions++;
					throw new UnsupportedOperationDueToRetrievalError("Converting the reponse of a REST request into RDF failed.", e, null, fm);
				}
			}

			@Override
			protected void processExtractedSolMaps( final Iterable<SolutionMapping> solmaps ) {
				for ( final SolutionMapping fetchedSM : solmaps ) {
					if ( SolutionMappingUtils.compatible(fetchedSM, sm) ) {
						final SolutionMapping out = SolutionMappingUtils.merge(sm, fetchedSM);
						sink.send(out);
						numberOfOutputMappingsProduced++;
					}
				}
			}
		};
	}

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced = 0L;
		numberOfDataConversionExceptions = 0;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfDataConversionExceptions",  numberOfDataConversionExceptions );
		s.put( "numberOfOutputMappingsProduced",    numberOfOutputMappingsProduced );
		return s;
	}

}
