package se.liu.ida.hefquin.jenaext.sparql.syntax;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;

public class ElementServiceWithParams extends ElementService
{
	protected final List<Var> paramVars;

	public ElementServiceWithParams( final Node n,
	                                 final Element el,
	                                 final boolean silent,
	                                 final List<Var> paramVars ) {
		super(n, el, silent);

		assert paramVars != null;
		this.paramVars = paramVars;
	}

	public List<Var> getParamVars() {
		return paramVars;
	}

}
