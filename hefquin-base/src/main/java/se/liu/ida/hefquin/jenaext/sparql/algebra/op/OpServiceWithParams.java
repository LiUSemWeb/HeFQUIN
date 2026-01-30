package se.liu.ida.hefquin.jenaext.sparql.algebra.op;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/**
 * This class extends {@link OpService} to represent SERVICE clauses that
 * use PARAMS(...), which is a HeFQUIN-specific extension to SERVICE.
 */
public class OpServiceWithParams extends OpService
{
	protected final Map<String, Var> paramVars;

	/**
	 * @param n - the service node, an IRI or a variable
	 * @param subOp - represents the graph pattern inside the SERVICE clause
	 * @param silent - {@code true} if the SERVICE clause has the SILENT keyword
	 * @param paramVars - the variables listed inside PARAMS(...)
	 */
	public OpServiceWithParams( final Node n,
	                            final Op subOp,
	                            final boolean silent,
	                            final Map<String, Var> paramVars ) {
		super(n, subOp, silent);

		assert paramVars != null;
		this.paramVars = paramVars;
	}

	/**
	 * Returns the variables listed inside the PARAMS(...) part of the
	 * SERVICE clause.
	 *
	 * @return the variables from the PARAMS(...) part
	 */
	public Map<String, Var> getParamVars() {
		return paramVars;
	}

	@Override
	public Op1 copy( final Op newOp ) {
		return new OpServiceWithParams( getService(), newOp, getSilent(), paramVars );
	}

	@Override
	public boolean equalTo( final Op other, final NodeIsomorphismMap labelMap ) {
		if ( ! super.equalTo(other, labelMap) )
			return false;

		return    other instanceof OpServiceWithParams owp
		       && owp.paramVars.equals(paramVars);
	}
}
