package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class ExecOpBindJoinBRTPF extends ExecOpGenericBindJoinWithRequestOps<TriplePattern,BRTPFServer>
{
    protected final Set<Var> varsInTP;

    public ExecOpBindJoinBRTPF( final TriplePattern tp, final BRTPFServer fm ) {
        super(tp, fm);

        varsInTP = QueryPatternUtils.getVariablesInPattern(tp);
    }

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> inputSolMaps ) {
		final Set<SolutionMapping> restrictedSolMaps = restrictSolMaps(inputSolMaps, varsInTP);

		if ( restrictedSolMaps.isEmpty() ) {
			return null;
		}

		final BindingsRestrictedTriplePatternRequest req = new BindingsRestrictedTriplePatternRequestImpl( (TriplePattern) query, restrictedSolMaps );
		return new ExecOpRequestBRTPF(req, fm);
	}


	// ---- helper functions ---------

	public static Set<SolutionMapping> restrictSolMaps( final Iterable<SolutionMapping> inputSolMaps,
	                                                    final Set<Var> joinVars ) {
		return restrictSolMaps(inputSolMaps, joinVars, null);
	}

	public static Set<SolutionMapping> restrictSolMaps( final Iterable<SolutionMapping> inputSolMaps,
	                                                    final Set<Var> joinVars,
	                                                    final List<SolutionMapping> unjoinableInputSMs ) {
		final Set<SolutionMapping> restrictedSolMaps = new HashSet<>();
		for ( final SolutionMapping sm : inputSolMaps ) {
			final SolutionMapping sm2 = SolutionMappingUtils.restrict(sm, joinVars);
			if ( ! SolutionMappingUtils.containsBlankNodes(sm2) ) {
				restrictedSolMaps.add(sm2);
			}
			else if ( unjoinableInputSMs != null ){
				unjoinableInputSMs.add(sm);
			}
		}

		return restrictedSolMaps;
	}

}
