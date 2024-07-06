package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;

public abstract class BaseForExecOpBindJoinSPARQL extends BaseForExecOpBindJoinWithRequestOps<SPARQLGraphPattern, SPARQLEndpoint>
{
	protected final List<Var> varsInSubQuery;

	public BaseForExecOpBindJoinSPARQL( final TriplePattern query,
	                                    final SPARQLEndpoint fm,
	                                    final boolean useOuterJoinSemantics,
	                                    final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, QueryPatternUtils.getVariablesInPattern(query), collectExceptions);
		varsInSubQuery = new ArrayList<>(varsInPatternForFM);
	}

	public BaseForExecOpBindJoinSPARQL( final BGP query,
	                                    final SPARQLEndpoint fm,
	                                    final boolean useOuterJoinSemantics,
	                                    final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, QueryPatternUtils.getVariablesInPattern(query), collectExceptions);
		varsInSubQuery = new ArrayList<>(varsInPatternForFM);
	}

	public BaseForExecOpBindJoinSPARQL( final SPARQLGraphPattern query,
	                                    final SPARQLEndpoint fm,
	                                    final boolean useOuterJoinSemantics,
	                                    final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, QueryPatternUtils.getVariablesInPattern(query), collectExceptions);
		varsInSubQuery = new ArrayList<>(varsInPatternForFM);
	}
}
