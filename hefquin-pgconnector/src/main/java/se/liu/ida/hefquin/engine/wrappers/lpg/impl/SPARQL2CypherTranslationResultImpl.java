package se.liu.ida.hefquin.engine.wrappers.lpg.impl;

import java.util.Map;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.wrappers.lpg.SPARQL2CypherTranslationResult;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.CypherVar;

public class SPARQL2CypherTranslationResultImpl implements SPARQL2CypherTranslationResult
{
	protected final CypherQuery q;
	protected final Map<CypherVar, Var> vm;

	public SPARQL2CypherTranslationResultImpl( final CypherQuery q, final Map<CypherVar, Var> vm ) {
		assert q != null;
		assert vm != null;

		this.q = q;
		this.vm = vm;
	}

	@Override
	public CypherQuery getCypherQuery() { return q; }

	@Override
	public Map<CypherVar, Var> getVariablesMapping() { return vm; }
}
