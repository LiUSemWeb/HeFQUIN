package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

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
 */
public class ExecOpBindJoinBRTPF extends BaseForExecOpBindJoinWithRequestOps<TriplePattern,BRTPFServer>
{
	public ExecOpBindJoinBRTPF( final TriplePattern tp,
	                            final BRTPFServer fm,
	                            final boolean useOuterJoinSemantics,
	                            final boolean collectExceptions ) {
		super(tp, fm, useOuterJoinSemantics, tp.getAllMentionedVariables(), collectExceptions );
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOp( final Iterable<SolutionMapping> inputSolMaps ) {
		final Set<SolutionMapping> restrictedSMs = restrictSolMaps(inputSolMaps, varsInPatternForFM);

		if ( restrictedSMs == null ) {
			final TriplePatternRequest req = new TriplePatternRequestImpl(query);
			return new ExecOpRequestTPFatBRTPFServer(req, fm, false);
		}

		if ( restrictedSMs.isEmpty() ) {
			return null;
		}

		// If there is only a single solution mapping, we
		// do a TPF request instead of a brTPF request.
		if ( restrictedSMs.size() == 1 ) {
			final SolutionMapping sm = restrictedSMs.iterator().next();
			final TriplePattern restrictedTP;
			try {
				restrictedTP = query.applySolMapToGraphPattern(sm);
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

		final BindingsRestrictedTriplePatternRequest req = new BindingsRestrictedTriplePatternRequestImpl( (TriplePattern) query, restrictedSMs );
		return new ExecOpRequestBRTPF(req, fm, false);
	}


	// ---- helper functions ---------

	/**
	 * Returns null if at least one of the solution mappings that would
	 * otherwise be added to the returned set of solution mappings is
	 * the empty solution mapping (in which case this operator better
	 * uses a TPF request rather than a brTPF request).
	 */
	public static Set<SolutionMapping> restrictSolMaps( final Iterable<SolutionMapping> inputSolMaps,
	                                                    final Set<Var> joinVars ) {
		final Set<SolutionMapping> restrictedSolMaps = new HashSet<>();
		for ( final SolutionMapping sm : inputSolMaps ) {
			final SolutionMapping sm2 = SolutionMappingUtils.restrict(sm, joinVars);

			if ( sm2.asJenaBinding().isEmpty() ) {
				return null;
			}

			assert ! SolutionMappingUtils.containsBlankNodes(sm2);
			restrictedSolMaps.add(sm2);
		}

		return restrictedSolMaps;
	}

}
