package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.utils.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class ExecOpBindJoinBRTPF extends BaseForExecOpBindJoinWithRequestOps<TriplePattern,BRTPFServer>
{
    protected final Set<Var> varsInTP;

    public ExecOpBindJoinBRTPF( final TriplePattern tp, final BRTPFServer fm, final boolean useOuterJoinSemantics ) {
        super(tp, fm, useOuterJoinSemantics);

        varsInTP = QueryPatternUtils.getVariablesInPattern(tp);
    }

	@Override
	protected Pair<List<SolutionMapping>, List<SolutionMapping>> extractUnjoinableInputSMs( final Iterable<SolutionMapping> solMaps ) {
		return extractUnjoinableInputSMs(solMaps, varsInTP);
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> inputSolMaps ) {
		final Set<SolutionMapping> restrictedSMs = restrictSolMaps(inputSolMaps, varsInTP);

		if ( restrictedSMs.isEmpty() ) {
			return null;
		}

		final BindingsRestrictedTriplePatternRequest req = new BindingsRestrictedTriplePatternRequestImpl( (TriplePattern) query, restrictedSMs );
		return new ExecOpRequestBRTPF(req, fm);
	}


	// ---- helper functions ---------

	public static Set<SolutionMapping> restrictSolMaps( final Iterable<SolutionMapping> inputSolMaps,
	                                                    final Set<Var> joinVars ) {
		final Set<SolutionMapping> restrictedSolMaps = new HashSet<>();
		for ( final SolutionMapping sm : inputSolMaps ) {
			final SolutionMapping sm2 = SolutionMappingUtils.restrict(sm, joinVars);
			assert ! SolutionMappingUtils.containsBlankNodes(sm2);
			restrictedSolMaps.add(sm2);
		}

		return restrictedSolMaps;
	}

}
