package se.liu.ida.hefquin.jenaext.sparql.syntax;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;

/**
 * This class extends {@link ElementService} to represent SERVICE clauses
 * that use PARAMS(...), which is a HeFQUIN-specific extension to SERVICE.
 */
public class ElementServiceWithParams extends ElementService
{
	protected final List<Var> paramVars;

	/**
	 * @param n - the service node, an IRI or a variable
	 * @param el - represents the graph pattern inside the SERVICE clause
	 * @param silent - {@code true} if the SERVICE clause has the SILENT keyword
	 * @param paramVars - the variables listed inside PARAMS(...)
	 */
	public ElementServiceWithParams( final Node n,
	                                 final Element el,
	                                 final boolean silent,
	                                 final List<Var> paramVars ) {
		super(n, el, silent);

		assert paramVars != null;
		this.paramVars = paramVars;
	}

	/**
	 * Returns the variables listed inside the PARAMS(...) part of the
	 * SERVICE clause.
	 *
	 * @return the variables from the PARAMS(...) part
	 */
	public List<Var> getParamVars() {
		return paramVars;
	}

}
