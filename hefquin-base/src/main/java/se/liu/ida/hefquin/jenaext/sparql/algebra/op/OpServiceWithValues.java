package se.liu.ida.hefquin.jenaext.sparql.algebra.op;

import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/**
 * Represents the combination of a SERVICE clause that has a variable as
 * service node, together with a set of possible service URIs to be used
 * for that variable.
 */
public class OpServiceWithValues extends OpService
{
	protected final Set<Node> values;

	/**
	 * @param n - the service node, must be a variable
	 * @param subOp - represents the graph pattern inside the SERVICE clause
	 * @param silent - {@code true} if the SERVICE clause has the SILENT keyword
	 * @param values- set of possible URIs for the given variable
	 */
	public OpServiceWithValues( final Node n,
	                            final Op subOp,
	                            final boolean silent,
	                            final Set<Node> values ) {
		super(n, subOp, silent);

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

	@Override
	public Op1 copy( final Op newOp ) {
		return new OpServiceWithValues( getService(), newOp, getSilent(), values );
	}

	@Override
	public boolean equalTo( final Op other, final NodeIsomorphismMap labelMap ) {
		if ( ! super.equalTo(other, labelMap) )
			return false;

		return    other instanceof OpServiceWithValues owv
		       && owv.values.equals(values);
	}

}
