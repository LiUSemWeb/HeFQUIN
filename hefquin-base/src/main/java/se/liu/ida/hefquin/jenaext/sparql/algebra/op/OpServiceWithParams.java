package se.liu.ida.hefquin.jenaext.sparql.algebra.op;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpServiceWithParams extends OpService
{
	protected final List<Var> paramVars;

	public OpServiceWithParams( final Node serviceNode,
	                            final Op subOp,
	                            final boolean silent,
	                            final List<Var> paramVars ) {
		super(serviceNode, subOp, silent);

		assert paramVars != null;
		this.paramVars = paramVars;
	}

	public List<Var> getParamVars() {
		return paramVars;
	}

	@Override
	public Op1 copy( final Op newOp ) {
		return new OpServiceWithParams( getService(), newOp, getSilent(), paramVars );
	}

	@Override
	public boolean equalTo( final Op other, final NodeIsomorphismMap labelMap ) {
		if ( super.equalTo(other, labelMap) == false )
			return false;

		return    other instanceof OpServiceWithParams owp
		       && owp.paramVars.equals(paramVars);
	}
}
