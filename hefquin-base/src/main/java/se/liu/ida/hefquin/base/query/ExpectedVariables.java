package se.liu.ida.hefquin.base.query;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

/**
 * Implementations of this interface represent sets of variables that can
 * be expected in the solution mappings produced for query patterns and by
 * query operators.
 */
public interface ExpectedVariables
{
	/**
	 * Returns the variables that are guaranteed to be bound in
	 * every solution mapping produced by the operator for which
	 * this {@link ExpectedVariables} object was created.
	 */
	Set<Var> getCertainVariables();

	/**
	 * Returns the variables that may be bound in solution mappings
	 * produced by the operator for which this {@link ExpectedVariables}
	 * object was created, but that are not guaranteed to be bound in
	 * every such solution mapping.
	 */
	Set<Var> getPossibleVariables();
}
