package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.utils.ExpectedVariablesUtils;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

/**
 * A multiway left join corresponds to a sequence of SPARQL OPTIONAL clauses.
 * Hence, it is not to be confused with nested OPTIONAL clauses (which would,
 * instead, be captured as multiple nested multiway left joins).
 */
public class LogicalOpMultiwayLeftJoin extends BaseForLogicalOps implements NaryLogicalOp
{
	protected static final LogicalOpMultiwayLeftJoin singletonWithoutReduction = new LogicalOpMultiwayLeftJoin(false);
	protected static final LogicalOpMultiwayLeftJoin singletonThatMayReduce  = new LogicalOpMultiwayLeftJoin(true);

	public static LogicalOpMultiwayLeftJoin getInstance( final boolean mayReduce ) {
		return mayReduce ? singletonThatMayReduce : singletonWithoutReduction;
	}

	/**
	 * Returns the singleton instance of {@link LogicalOpMultiwayLeftJoin} that does <em>not</em>
	 * reduce duplicates.
	 *
	 * <p>This is equivalent to calling {@link #getInstance(boolean)} with the argument
	 * {@code false}.
	 *
	 * @return the singleton instance that does not reduce duplicates
	 */
	public static LogicalOpMultiwayLeftJoin getInstance() {
		return singletonWithoutReduction;
	}

	protected LogicalOpMultiwayLeftJoin( final boolean mayReduce ) {
		super( mayReduce );
	}

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
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

	@Override
	public boolean equals( final Object o ) {
		return o instanceof LogicalOpMultiwayLeftJoin oo
		    && oo.mayReduce == mayReduce;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "mlj";
	}
}
