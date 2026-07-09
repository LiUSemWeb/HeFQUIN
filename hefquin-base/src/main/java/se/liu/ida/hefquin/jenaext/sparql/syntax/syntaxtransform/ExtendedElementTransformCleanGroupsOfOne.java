package se.liu.ida.hefquin.jenaext.sparql.syntax.syntaxtransform;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;

import se.liu.ida.hefquin.jenaext.sparql.syntax.ElementServiceWithParams;
import se.liu.ida.hefquin.jenaext.sparql.syntax.ElementServiceWithValues;

public class ExtendedElementTransformCleanGroupsOfOne extends ElementTransformCleanGroupsOfOne
{
	@Override
	public Element transform( final ElementService el,
	                          final Node serviceNode,
	                          final Element subElmt ) {
		if (    ! alwaysCopy
		     && el.getServiceNode() == serviceNode
		     && el.getElement() == subElmt )
			return el;

		if ( el instanceof ElementServiceWithParams elParams )
			return new ElementServiceWithParams( serviceNode,
			                                     subElmt,
			                                     el.getSilent(),
			                                     elParams.getParamVars() );

		if ( el instanceof ElementServiceWithValues elValues ) {
			if ( ! serviceNode.isVariable() ) throw new IllegalArgumentException();

			return new ElementServiceWithValues( Var.alloc(serviceNode),
			                                     subElmt,
			                                     el.getSilent(),
			                                     elValues.getPossibleValues() );
		}

		return new ElementService( serviceNode, subElmt, el.getSilent() );
	}

}
