package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;

import java.util.HashSet;
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
		final BindingsRestrictedTriplePatternRequest req = createRequest(inputSolMaps);
		return new ExecOpRequestBRTPF(req, fm);
	}

	protected BindingsRestrictedTriplePatternRequest createRequest( final Iterable<SolutionMapping> inputSolMaps ) {
		final Set<SolutionMapping> restrictedSolMaps = new HashSet<>();
		for ( final SolutionMapping sm : inputSolMaps ) {
			restrictedSolMaps.add( SolutionMappingUtils.restrict(sm, varsInTP) );
		}
		return new BindingsRestrictedTriplePatternRequestImpl( (TriplePattern) query, restrictedSolMaps );
	}

}
