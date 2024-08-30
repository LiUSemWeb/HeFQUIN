package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression;

import java.util.Objects;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherExpressionVisitor;

/**
 * Represents an expression to obtain an array of all the labels
 * of the LPG node that is bound to a given query variable.
 */
public class AllLabelsExpression implements CypherExpression
{
	protected final CypherVar var;

	public AllLabelsExpression( final CypherVar var ) {
		assert var != null;
		this.var = var;
	}

	@Override
	public Set<CypherVar> getVars() {
		return Set.of(var);
	}

	@Override
	public void visit( final CypherExpressionVisitor visitor ) {
		var.visit(visitor);
		visitor.visitLabels(this);
	}

	@Override
	public boolean equals( final Object o ) {
		if ( this == o ) return true;
		if ( o == null || getClass() != o.getClass() ) return false;
		final FirstLabelExpression that = (FirstLabelExpression) o;
		return var.equals(that.var);
	}

	@Override
	public int hashCode() {
		return Objects.hash(var);
	}

	@Override
	public String toString() {
		return "LABELS(" + var + ")";
	}

}
