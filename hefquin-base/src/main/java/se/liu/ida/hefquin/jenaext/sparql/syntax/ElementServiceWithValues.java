package se.liu.ida.hefquin.jenaext.sparql.syntax;

import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;

/**
 * Represents the combination of a SERVICE clause that has a variable as
 * service node, together with a set of possible service URIs to be used
 * for that variable.
 */
public class ElementServiceWithValues extends ElementService
{
	protected final Set<Node> values;

	/**
	 * @param v - the variable that is the service node of this SERVICE clause
	 * @param el - represents the graph pattern inside the SERVICE clause
	 * @param silent - {@code true} if the SERVICE clause has the SILENT keyword
	 * @param values- set of possible URIs for the given variable
	 */
	public ElementServiceWithValues( final Var v,
	                                 final Element el,
	                                 final boolean silent,
	                                 final Set<Node> values ) {
		super(v, el, silent);

		assert values != null;
		this.values = values;
	}

	/**
	 * Returns the set of possible URIs for the variable of this SERVICE clause.
	 *
	 * @return the set of possible service URIs
	 */
	public Set<Node> getPossibleValues() {
		return values;
	}
}
