package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.utils.Pair;

public abstract class BaseForExecOpBindJoinSPARQL extends BaseForExecOpBindJoinWithRequestOps<SPARQLGraphPattern, SPARQLEndpoint>
{
	protected final List<Var> varsInSubQuery;

	public BaseForExecOpBindJoinSPARQL( final TriplePattern query,
	                                    final SPARQLEndpoint fm,
	                                    final boolean useOuterJoinSemantics,
	                                    final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
		varsInSubQuery = new ArrayList<>( QueryPatternUtils.getVariablesInPattern(query) );
	}

	public BaseForExecOpBindJoinSPARQL( final BGP query,
	                                    final SPARQLEndpoint fm,
	                                    final boolean useOuterJoinSemantics,
	                                    final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
		varsInSubQuery = new ArrayList<>( QueryPatternUtils.getVariablesInPattern(query) );
	}

	public BaseForExecOpBindJoinSPARQL( final SPARQLGraphPattern query,
	                                    final SPARQLEndpoint fm,
	                                    final boolean useOuterJoinSemantics,
	                                    final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
		varsInSubQuery = new ArrayList<>( QueryPatternUtils.getVariablesInPattern(query) );
	}

	@Override
	protected Pair<List<SolutionMapping>, List<SolutionMapping>> extractUnjoinableInputSMs( final Iterable<SolutionMapping> solMaps ) {
		return extractUnjoinableInputSMs(solMaps, varsInSubQuery);
	}

}
