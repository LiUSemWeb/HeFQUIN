package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Set;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;

/**
 * Implementation of (a batching version of) the bind join algorithm that starts by
 * using a VALUES clause exactly as done by {@link ExecOpBindJoinSPARQLwithVALUES}.
 * If this fails for the first request, the implementation repeats the request by
 * using FILTERs as done by {@link ExecOpBindJoinSPARQLwithFILTER} and, then,
 * continues using the FILTER-based approach for the rest of the requests. If the
 * first VALUES-based request succeeds, however, then the implementation continues
 * using the VALUES-based approach for the rest of the requests. 
 */
public class ExecOpBindJoinSPARQLwithVALUESorFILTER extends BaseForExecOpBindJoinSPARQL
{
	public final static int DEFAULT_BATCH_SIZE = BaseForExecOpBindJoinWithRequestOps.DEFAULT_BATCH_SIZE;

	protected final Element pattern;

	/**
	 * <code>true</code> means that the FILTER-based bind-join requests will
	 * be used, whereas <code>false</code> is for VALUES-based bind-join
	 * requests. While this flag is initially set to <code>false</code>,
	 * it will be flipped to <code>true</code> if the first (VALUES-based)
	 * request fails.
	 */
	protected boolean useFilterBasedApproach = false;

	/**
	 * To be flipped to <code>false</code> after the first request has been
	 * done and we know which approach to use for the rest of the requests.
	 */
	protected boolean trialPhase = true;

	/**
	 * @param query - the graph pattern to be evaluated (in a bind-join
	 *          manner) at the federation member given as 'fm'
	 *
	 * @param fm - the federation member targeted by this operator
	 *
	 * @param inputVars - the variables to be expected in the solution
	 *          mappings that will be pushed as input to this operator
	 *
	 * @param useOuterJoinSemantics - <code>true</code> if the 'query' is to
	 *          be evaluated under outer-join semantics; <code>false</code>
	 *          for inner-join semantics
	 *
	 * @param batchSize - the number of solution mappings to be included in
	 *          each bind-join request; this value must not be smaller than
	 *          {@link #minimumRequestBlockSize}; as a default value for this
	 *          parameter, use {@link #DEFAULT_BATCH_SIZE}
	 *
	 * @param collectExceptions - <code>true</code> if this operator has to
	 *          collect exceptions (which is handled entirely by one of the
	 *          super classes); <code>false</code> if the operator should
	 *          immediately throw every {@link ExecOpExecutionException}
	 *
	 * @param qpInfo - the {@link QueryPlanningInfo} object that was
	 *          populated for a physical plan whose root operator was
	 *          the physical operator for which this executable operator
	 *          was created
	 */
	public ExecOpBindJoinSPARQLwithVALUESorFILTER( final SPARQLGraphPattern query,
	                                               final SPARQLEndpoint fm,
	                                               final ExpectedVariables inputVars,
	                                               final boolean useOuterJoinSemantics,
	                                               final int batchSize,
	                                               final boolean collectExceptions,
	                                               final QueryPlanningInfo qpInfo ) {
		super(query, fm, inputVars, useOuterJoinSemantics, batchSize, collectExceptions, qpInfo);

		pattern = QueryPatternUtils.convertToJenaElement(query);
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOp( final Set<Binding> solMaps ) {
		if ( useFilterBasedApproach ) {
			return ExecOpBindJoinSPARQLwithFILTER.createExecutableReqOp(solMaps, pattern, fm);
		}
		else {
			return ExecOpBindJoinSPARQLwithVALUES.createExecutableReqOp(solMaps, pattern, fm);
		}
	}

	protected void performRequestAndHandleResponse( final IntermediateResultElementSink sink,
	                                                final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableReqOp(currentSolMapsForRequest);
		final MyIntermediateResultElementSink mySink = createMySink();

		numberOfRequestOpsUsed++;

		try {
			reqOp.execute(mySink, execCxt);
		}
		catch ( final ExecOpExecutionException e ) {
			if ( ! trialPhase ) {
				throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
			}

			trialPhase = false;
			useFilterBasedApproach = true;

			statsOfLastReqOp = reqOp.getStats();
			if ( statsOfFirstReqOp == null ) statsOfFirstReqOp = statsOfLastReqOp;

			performRequestAndHandleResponse(sink, execCxt);

			return;
		}

		trialPhase = false;

		consumeMySink(mySink, sink);

		statsOfLastReqOp = reqOp.getStats();
		if ( statsOfFirstReqOp == null ) statsOfFirstReqOp = statsOfLastReqOp;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "switchedToFilter", useFilterBasedApproach );
		return s;
	}

}
