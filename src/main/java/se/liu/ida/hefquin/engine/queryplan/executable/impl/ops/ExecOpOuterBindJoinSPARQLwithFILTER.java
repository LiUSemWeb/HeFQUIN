package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

public class ExecOpOuterBindJoinSPARQLwithFILTER extends ExecOpGenericOuterBindJoinWithRequestOps<SPARQLGraphPattern, SPARQLEndpoint>
{
	protected final Set<Var> varsInSubQuery;

	public ExecOpOuterBindJoinSPARQLwithFILTER( final TriplePattern query,
	                                            final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInSubQuery = QueryPatternUtils.getVariablesInPattern(query);
	}

	public ExecOpOuterBindJoinSPARQLwithFILTER( final BGP query,
	                                            final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInSubQuery = QueryPatternUtils.getVariablesInPattern(query);
	}

	public ExecOpOuterBindJoinSPARQLwithFILTER( final SPARQLGraphPattern query,
	                                            final SPARQLEndpoint fm ) {
		super(query, fm);
		varsInSubQuery = QueryPatternUtils.getVariablesInPattern(query);
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final Iterable<SolutionMapping> solMaps,
	                                                               final List<SolutionMapping> unjoinableInputSMs ) {
		final Op op = ExecOpBindJoinSPARQLwithFILTER.createFilter(solMaps, query, varsInSubQuery, unjoinableInputSMs);
		if ( op == null ) {
			return null;
		}

		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl2(op);
		final SPARQLRequest request = new SPARQLRequestImpl(pattern);
		return new ExecOpRequestSPARQL(request, fm);
	}
}
