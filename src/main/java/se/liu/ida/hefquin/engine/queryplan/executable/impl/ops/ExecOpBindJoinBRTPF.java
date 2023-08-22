package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.utils.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class ExecOpBindJoinBRTPF extends BaseForExecOpBindJoinWithRequestOps<TriplePattern,BRTPFServer>
{
    protected final Set<Var> varsInTP;

    public ExecOpBindJoinBRTPF( final TriplePattern tp,
                                final BRTPFServer fm,
                                final boolean useOuterJoinSemantics,
                                final boolean collectExceptions ) {
        super(tp, fm, useOuterJoinSemantics, collectExceptions );

        varsInTP = QueryPatternUtils.getVariablesInPattern(tp);
    }

	@Override
	protected Pair<List<SolutionMapping>, List<SolutionMapping>> extractUnjoinableInputSMs( final Iterable<SolutionMapping> solMaps ) {
		return extractUnjoinableInputSMs(solMaps, varsInTP);
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> inputSolMaps ) {
		final Set<SolutionMapping> restrictedSMs = restrictSolMaps(inputSolMaps, varsInTP);

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
				restrictedTP = QueryPatternUtils.applySolMapToTriplePattern(sm, query);
			}
			catch ( final VariableByBlankNodeSubstitutionException e ) {
				// This exception should not happen because the set of solution
				// mappings given to this function should not have blank nodes
				// for the join variables.
				e.printStackTrace();

				return null;
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
