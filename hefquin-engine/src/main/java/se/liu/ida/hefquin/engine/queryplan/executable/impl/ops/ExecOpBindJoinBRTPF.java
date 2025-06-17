package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Set;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

/**
 * Implementation of (a batching version of) the bind join algorithm
 * for cases in which the federation member accessed by the algorithm
 * supports the brTPF interface.
 *
 * For every batch of solution mappings from the input, the algorithm of
 * this operator sends a brTPF request to the federation member; this request
 * consists of the given triple pattern and the solutions of the current
 * input batch (in fact, the algorithm may also decide to split the input
 * batch into smaller batches for multiple requests).
 * The response to such a request are all triples that i) match the triple
 * pattern and ii) are compatible with at least one of the solutions that
 * were attached to the request.
 * After receiving such a response, the algorithm creates solution mappings
 * from the received triples, joins these solution mappings locally with the
 * solutions in the batch used for making the request, and then outputs the
 * resulting joined solutions (if any).
 * Thereafter, the algorithm moves on to the next batch of solutions from
 * the input.
 *
 * For more details about the actual implementation of the algorithm, and its
 * extra capabilities, refer to {@link BaseForExecOpBindJoinWithRequestOps}.
 */
public class ExecOpBindJoinBRTPF extends BaseForExecOpBindJoinWithRequestOps<TriplePattern,BRTPFServer>
{
	public final static int DEFAULT_BATCH_SIZE = BaseForExecOpBindJoinWithRequestOps.DEFAULT_BATCH_SIZE;

	/**
	 * @param tp - the triple pattern to be evaluated (in a bind-join
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
	 */
	public ExecOpBindJoinBRTPF( final TriplePattern tp,
	                            final BRTPFServer fm,
	                            final ExpectedVariables inputVars,
	                            final boolean useOuterJoinSemantics,
	                            final int batchSize,
	                            final boolean collectExceptions ) {
		super( tp, tp.getAllMentionedVariables(), fm, inputVars, useOuterJoinSemantics, batchSize, collectExceptions );
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOp( final Set<Binding> solMaps ) {
		// If there is only a single solution mapping, we
		// do a TPF request instead of a brTPF request.
		if ( solMaps.size() == 1 ) {
			final Binding sm = solMaps.iterator().next();
			final TriplePattern restrictedTP;
			try {
				restrictedTP = TriplePatternImpl.applySolMapToTriplePattern(sm, query);
			}
			catch ( final VariableByBlankNodeSubstitutionException e ) {
				// This exception should not happen because the set of solution
				// mappings given to this function should not have blank nodes
				// for the join variables.
				throw new IllegalStateException();
			}

			final TriplePatternRequest req = new TriplePatternRequestImpl(restrictedTP);
			return new ExecOpRequestTPFatBRTPFServer(req, fm, false);
		}

		final BindingsRestrictedTriplePatternRequest req = new BindingsRestrictedTriplePatternRequestImpl(query, solMaps);
		return new ExecOpRequestBRTPF(req, fm, false);
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOpForAll() {
		final TriplePatternRequest req = new TriplePatternRequestImpl(query);
		return new ExecOpRequestTPFatBRTPFServer(req, fm, false);
	}

}
