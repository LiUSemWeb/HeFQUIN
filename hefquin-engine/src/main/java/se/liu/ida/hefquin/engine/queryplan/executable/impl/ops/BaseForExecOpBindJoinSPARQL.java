package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;

/**
 * A base class for all variations of the bind join algorithm that use
 * some form of SPARQL requests.
 */
public abstract class BaseForExecOpBindJoinSPARQL extends BaseForExecOpBindJoinWithRequestOps<SPARQLGraphPattern, SPARQLEndpoint>
{
	protected final List<Var> varsInSubQuery;

	public BaseForExecOpBindJoinSPARQL( final SPARQLGraphPattern p,
	                                    final SPARQLEndpoint fm,
	                                    final boolean useOuterJoinSemantics,
	                                    final boolean collectExceptions ) {
		super(p, fm, useOuterJoinSemantics, p.getAllMentionedVariables(), collectExceptions);
		varsInSubQuery = new ArrayList<>(varsInPatternForFM);
	}
}
