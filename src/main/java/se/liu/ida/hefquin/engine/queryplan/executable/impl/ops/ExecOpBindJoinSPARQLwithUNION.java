package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ExecOpBindJoinSPARQLwithUNION extends ExecOpGenericBindJoin<TriplePattern,SPARQLEndpoint> {

	private Set<Var> varsInTP;

	public ExecOpBindJoinSPARQLwithUNION( final TriplePattern query, final SPARQLEndpoint fm) {
		super(query, fm);
		varsInTP = QueryPatternUtils.getVariablesInPattern(query);
	}

	@Override
	protected Iterable<? extends SolutionMapping> fetchSolutionMappings( final Iterable<SolutionMapping> solMaps,
			final ExecutionContext execCxt) {
		final Op op = createUnion(solMaps);
		final SPARQLGraphPattern pattern = new SPARQLGraphPatternImpl(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		final SolMapsResponse response = execCxt.getFederationAccessMgr().performRequest(request, fm);
		return response.getSolutionMappings();
	}

	protected Op createUnion(final Iterable<SolutionMapping> solMaps) {
		return null;
	}

}
