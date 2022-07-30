package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BindingsRestrictedTriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

public class ExecOpOuterBindJoinBRTPF extends ExecOpGenericOuterBindJoinWithRequestOps<TriplePattern,BRTPFServer>
{
    protected final Set<Var> varsInTP;

    public ExecOpOuterBindJoinBRTPF( final TriplePattern tp, final BRTPFServer fm ) {
        super(tp, fm);

        varsInTP = QueryPatternUtils.getVariablesInPattern(tp);
    }

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> inputSolMaps,
	                                                               final List<SolutionMapping> unjoinableInputSMs ) {
		final Set<SolutionMapping> restrictedSMs = ExecOpBindJoinBRTPF.restrictSolMaps(inputSolMaps, varsInTP, unjoinableInputSMs);

		if ( restrictedSMs.isEmpty() ) {
			return null;
		}

		final BindingsRestrictedTriplePatternRequest req = new BindingsRestrictedTriplePatternRequestImpl( (TriplePattern) query, restrictedSMs );
		return new ExecOpRequestBRTPF(req, fm);
	}

}
