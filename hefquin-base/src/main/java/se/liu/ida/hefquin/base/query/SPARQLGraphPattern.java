package se.liu.ida.hefquin.base.query;

import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.jenaext.sparql.syntax.ElementUtils;

/**
 * This interface represents any kind of SPARQL query pattern.
 */
public interface SPARQLGraphPattern extends Query
{
	/**
	 * Returns a set of all triple patterns that are contained
	 * within this graph pattern.
	 */
	Set<TriplePattern> getAllMentionedTPs();

	/**
	 * Returns the set of all variables mentioned in this graph pattern, except
	 * for the variables that occur only in expressions (in FILTER or in BIND).
	 */
	Set<Var> getAllMentionedVariables();

	/**
	 * Returns the variables that are guaranteed to be bound in
	 * every solution mapping produced for this graph pattern.
	 */
	Set<Var> getCertainVariables();

	/**
	 * Returns the variables that may be bound in solution mappings
	 * produced for this graph pattern, but that are not guaranteed
	 * to be bound in every such solution mapping.
	 */
	Set<Var> getPossibleVariables();

	/**
	 * Returns the sets of variables that can be expected in the
	 * solution mappings produced for this graph pattern. It holds
	 * that the {@link ExpectedVariables#getCertainVariables()} and
	 * the {@link ExpectedVariables#getPossibleVariables()} methods
	 * of the returned object return the same sets as returned by
	 * {@link #getCertainVariables()} and {@link #getPossibleVariables()},
	 * respectively.
	 */
	default ExpectedVariables getExpectedVariables() {
		final Set<Var> c = getCertainVariables();
		final Set<Var> p = getPossibleVariables();
		return new ExpectedVariables() {
			@Override public Set<Var> getPossibleVariables() { return p; }
			@Override public Set<Var> getCertainVariables() { return c; }
		};
	}

	/**
	 * Returns the number of times any variable is mentioned in this graph
	 * pattern (if the same variable is mentioned multiple times, then each
	 * of these mentions is counted), but ignores variable mentions in
	 * expressions (in FILTER or in BIND).
	 */
	int getNumberOfVarMentions();

	/**
	 * Returns the number of times any RDF term is mentioned in this graph
	 * pattern (if the same term is mentioned multiple times, then each of
	 * these mentions is counted), but ignores terms mentions in expressions
	 * (in FILTER or in BIND).
	 */
	int getNumberOfTermMentions();

	/**
	 * Applies the given solution mapping to this graph pattern and returns
	 * the resulting graph pattern in which all occurrences of the variables
	 * bound by the given solution mapping are replaced by the RDF terms that
	 * the solution mappings assigns to these variables.
	 *
	 * @throws VariableByBlankNodeSubstitutionException if one of the variables
	 *                                                  would be replaced by a
	 *                                                  blank node
	 */
	SPARQLGraphPattern applySolMapToGraphPattern( SolutionMapping sm ) throws VariableByBlankNodeSubstitutionException;

	/**
	 * Merges this graph pattern with the given graph pattern, using join
	 * semantics, and returns the resulting, merged pattern.
	 */
	SPARQLGraphPattern mergeWith( SPARQLGraphPattern other );

	/**
	 * Merges this graph pattern with FILTERS that use the given expressions
	 * and returns the resulting, merged pattern.
	 */
	default SPARQLGraphPattern mergeWith( final ExprList exprs ) {
		final Element elmtThis = QueryPatternUtils.convertToJenaElement(this);
		final Element elmtMerged = ElementUtils.merge(exprs, elmtThis);
		return new GenericSPARQLGraphPatternImpl1(elmtMerged);
	}

	/**
	 * Merges this graph pattern with BIND clauses that use the given
	 * expressions and returns the resulting, merged pattern.
	 */
	default SPARQLGraphPattern mergeWith( final VarExprList exprs ) {
		final Element elmtThis = QueryPatternUtils.convertToJenaElement(this);
		final Element elmtMerged = ElementUtils.merge(exprs, elmtThis);
		return new GenericSPARQLGraphPatternImpl1(elmtMerged);
	}
}
