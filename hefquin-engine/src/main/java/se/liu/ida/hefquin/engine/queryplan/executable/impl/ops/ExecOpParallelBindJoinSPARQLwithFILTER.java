package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Set;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

/**
 * Implementation of the parallel, batch-based bind-join algorithm that
 * uses a FILTERs to capture the potential join partners that are sent
 * to the federation member.
 *
 * For more details about the actual implementation of the algorithm, and its
 * extra capabilities, refer to {@link BaseForExecOpParallelBindJoin}.
 */
public class ExecOpParallelBindJoinSPARQLwithFILTER
		extends BaseForExecOpParallelBindJoinSPARQL
{
	protected final Element pattern;

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
	 *          {@link #minimumRequestBlockSize}
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
	public ExecOpParallelBindJoinSPARQLwithFILTER(
			final SPARQLGraphPattern query,
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
	protected SPARQLRequest createRequest( final Set<Binding> batch ) {
		return ExecOpSequentialBindJoinSPARQLwithFILTER.createRequest(batch, pattern);
	}

}
