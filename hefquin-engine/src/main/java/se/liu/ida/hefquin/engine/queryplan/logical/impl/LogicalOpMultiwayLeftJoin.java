package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlanOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

/**
 * A multiway left join corresponds to a sequence of SPARQL OPTIONAL clauses.
 * Hence, it is not to be confused with nested OPTIONAL clauses (which would,
 * instead, be captured as multiple nested multiway left joins).
 */
public class LogicalOpMultiwayLeftJoin extends BaseForQueryPlanOperator implements NaryLogicalOp
{
	protected static LogicalOpMultiwayLeftJoin singleton = new LogicalOpMultiwayLeftJoin();

	public static LogicalOpMultiwayLeftJoin getInstance() { return singleton; }

	protected LogicalOpMultiwayLeftJoin() {}

	@Override
	public ExpectedVariables getExpectedVariables( final ExpectedVariables... inputVars ) {
		final Set<Var> certainVars = inputVars[0].getCertainVariables();
		final Set<Var> possibleVars = ExpectedVariablesUtils.unionOfAllVariables(inputVars);
		possibleVars.removeAll(certainVars);

		return new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() { return certainVars; }
			@Override public Set<Var> getPossibleVariables() { return possibleVars; }
		};
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpMultiwayLeftJoin; 
	}

	@Override
	public int hashCode(){
		return Objects.hash( this.getClass().getName() );
	}

	@Override
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public String toString(){
		return "> mlj ";
	}

}
